package com.ecommerce.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.ProductMapper;

@Service
public class CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisService redisService;

    public List<CartItem> list(Integer userId) {
        // 先从 Redis 获取
        String cartKey = redisService.getCartKey(Long.valueOf(userId));
        List<CartItem> cachedItems = redisService.get(cartKey, List.class);
        
        if (cachedItems != null && !cachedItems.isEmpty()) {
            // 缓存命中，补充商品信息
            for (CartItem item : cachedItems) {
                Product p = productMapper.selectById(item.getProductId());
                if (p != null && "on".equals(p.getStatus())) {
                    p.setCategory(null);
                    item.setProduct(p);
                }
            }
            return cachedItems;
        }

        // 缓存未命中，从数据库获取
        QueryWrapper<CartItem> qw = new QueryWrapper<CartItem>()
                .eq("user_id", userId)
                .orderByDesc("created_at");
        List<CartItem> items = cartItemMapper.selectList(qw);
        // Filter out items with deleted/off-shelf products and attach product info
        List<CartItem> valid = new ArrayList<>();
        for (CartItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p == null || "off".equals(p.getStatus())) continue;
            p.setCategory(null);
            item.setProduct(p);
            valid.add(item);
        }

        // 缓存到 Redis，有效期 1 小时
        redisService.set(cartKey, valid, 1, java.util.concurrent.TimeUnit.HOURS);
        
        return valid;
    }

    @Transactional
    public CartItem add(Integer userId, Integer productId, String skuSpec, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || "off".equals(product.getStatus())) {
            throw new BusinessException("商品不存在或已下架");
        }

        // Check if same product+sku already in cart
        QueryWrapper<CartItem> qw = new QueryWrapper<CartItem>()
                .eq("user_id", userId)
                .eq("product_id", productId);
        if (skuSpec != null) {
            qw.eq("sku_spec", skuSpec);
        } else {
            qw.isNull("sku_spec");
        }
        CartItem existing = cartItemMapper.selectOne(qw);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new BusinessException("库存不足，购物车已有 " + existing.getQuantity() + " 件，当前库存：" + product.getStock());
            }
            existing.setQuantity(newQty);
            existing.setUpdatedAt(new Date());
            cartItemMapper.updateById(existing);
            return existing;
        }

        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }

        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setSkuSpec(skuSpec);
        item.setQuantity(quantity);
        item.setSelected(true);
        item.setCreatedAt(new Date());
        item.setUpdatedAt(new Date());
        cartItemMapper.insert(item);

        // 删除缓存
        redisService.delete(redisService.getCartKey(Long.valueOf(userId)));

        return item;
    }

    public void update(Integer id, Integer userId, Integer quantity, Boolean selected) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) throw new BusinessException("购物车项不存在");
        if (!item.getUserId().equals(userId)) throw new BusinessException("无权操作此购物车项");
        if (quantity != null) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null && p.getStock() < quantity) {
                throw new BusinessException("库存不足，当前库存：" + p.getStock());
            }
            item.setQuantity(quantity);
        }
        if (selected != null) item.setSelected(selected);
        item.setUpdatedAt(new Date());
        cartItemMapper.updateById(item);

        // 删除缓存
        redisService.delete(redisService.getCartKey(Long.valueOf(userId)));
    }

    public void remove(Integer id, Integer userId) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null) throw new BusinessException("购物车项不存在");
        if (!item.getUserId().equals(userId)) throw new BusinessException("无权操作此购物车项");
        cartItemMapper.deleteById(id);

        // 删除缓存
        redisService.delete(redisService.getCartKey(Long.valueOf(userId)));
    }

    public void removeBatch(List<Integer> ids, Integer userId) {
        for (Integer id : ids) {
            remove(id, userId);
        }
    }

    public void selectAll(Integer userId, Boolean selected) {
        List<CartItem> items = cartItemMapper.selectList(
                new QueryWrapper<CartItem>().eq("user_id", userId));
        for (CartItem item : items) {
            item.setSelected(selected);
            cartItemMapper.updateById(item);
        }

        // 删除缓存
        redisService.delete(redisService.getCartKey(Long.valueOf(userId)));
    }
}
