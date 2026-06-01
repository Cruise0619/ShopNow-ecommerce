package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.Favorite;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.FavoriteMapper;
import com.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<Favorite> list(Integer userId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<Favorite>()
                .eq("user_id", userId)
                .orderByDesc("created_at");
        List<Favorite> favs = favoriteMapper.selectList(qw);
        for (Favorite f : favs) {
            Product p = productMapper.selectById(f.getProductId());
            if (p != null) p.setCategory(null);
            f.setProduct(p);
        }
        return favs;
    }

    public Map<String, Object> toggle(Integer userId, Integer productId) {
        QueryWrapper<Favorite> qw = new QueryWrapper<Favorite>()
                .eq("user_id", userId)
                .eq("product_id", productId);
        Favorite existing = favoriteMapper.selectOne(qw);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("favorited", false);
            return result;
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        fav.setCreatedAt(new Date());
        fav.setUpdatedAt(new Date());
        favoriteMapper.insert(fav);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", true);
        return result;
    }
}
