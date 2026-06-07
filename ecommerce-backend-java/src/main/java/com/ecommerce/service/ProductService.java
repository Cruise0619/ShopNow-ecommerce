package com.ecommerce.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Favorite;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.mapper.FavoriteMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserMapper;

@Service
public class ProductService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    public Page<Product> list(Integer page, Integer pageSize, Integer categoryId,
                               String keyword, String sort, String tags) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("status", "on");

        // Include subcategories
        if (categoryId != null) {
            List<Integer> catIds = new ArrayList<>();
            catIds.add(categoryId);
            List<Category> subCats = categoryMapper.selectList(
                    new QueryWrapper<Category>().eq("parent_id", categoryId));
            for (Category sc : subCats) {
                catIds.add(sc.getId());
            }
            qw.in("category_id", catIds);
        }

        if (keyword != null && !keyword.isEmpty()) {
            qw.like("name", keyword);
        }

        // Filter by tags (JSON array in MySQL)
        if (tags != null && !tags.isEmpty()) {
            String[] tagArr = tags.split(",");
            for (String tag : tagArr) {
                qw.apply("JSON_CONTAINS(tags, {0})", "\"" + tag.trim() + "\"");
            }
        }

        // Sort
        if ("sales".equals(sort)) {
            qw.orderByDesc("sales");
        } else if ("price_asc".equals(sort)) {
            qw.orderByAsc("price");
        } else if ("price_desc".equals(sort)) {
            qw.orderByDesc("price");
        } else {
            qw.orderByDesc("created_at");
        }

        Page<Product> result = productMapper.selectPage(new Page<>(page, pageSize), qw);

        // Batch-fill category for each product
        for (Product p : result.getRecords()) {
            if (p.getCategoryId() != null) {
                p.setCategory(categoryMapper.selectById(p.getCategoryId()));
            }
        }

        return result;
    }

    public Product detail(Integer id) {
        // 先从缓存获取
        Product cachedProduct = redisService.getCachedProduct(Long.valueOf(id), Product.class);
        if (cachedProduct != null) {
            // 缓存中存在，直接返回
            return cachedProduct;
        }

        // 缓存中不存在，从数据库获取
        Product product = productMapper.selectById(id);
        if (product == null) return null;

        product.setCategory(categoryMapper.selectById(product.getCategoryId()));

        // Top 10 reviews
        QueryWrapper<Review> rq = new QueryWrapper<Review>()
                .eq("product_id", id)
                .orderByDesc("created_at")
                .last("LIMIT 10");
        List<Review> reviews = reviewMapper.selectList(rq);
        for (Review r : reviews) {
            r.setUser(userMapper.selectById(r.getUserId()));
        }
        product.setReviews(reviews);
        product.setAvgRating(reviewMapper.avgRating(id));
        product.setReviewCount(reviewMapper.countByProductId(id));

        // 存入缓存
        redisService.cacheProduct(Long.valueOf(id), product);

        return product;
    }

    public Product findById(Integer id) {
        return productMapper.selectById(id);
    }

    public Page<Product> adminList(int page, int pageSize, String keyword, Integer categoryId, String status) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("name", keyword);
        }
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");
        Page<Product> result = productMapper.selectPage(new Page<>(page, pageSize), qw);
        for (Product p : result.getRecords()) {
            if (p.getCategoryId() != null) {
                p.setCategory(categoryMapper.selectById(p.getCategoryId()));
            }
        }
        return result;
    }

    public void delete(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // Delete uploaded image files from disk
        deleteImageFiles(product.getImages());

        // Clean up related data
        cartItemMapper.delete(new QueryWrapper<CartItem>().eq("product_id", id));
        favoriteMapper.delete(new QueryWrapper<Favorite>().eq("product_id", id));
        reviewMapper.delete(new QueryWrapper<Review>().eq("product_id", id));

        // Note: order_items intentionally preserved for order history

        productMapper.deleteById(id);
    }

    private void deleteImageFiles(List<String> images) {
        if (images == null || images.isEmpty()) return;
        for (String imageUrl : images) {
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                String filename = imageUrl.substring("/uploads/".length());
                File file = new File(uploadPath, filename);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}
