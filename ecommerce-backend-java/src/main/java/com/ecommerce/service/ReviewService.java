package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.ReviewMapper;
import com.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    public Page<Review> list(Integer productId, int page, int pageSize) {
        QueryWrapper<Review> qw = new QueryWrapper<Review>()
                .eq("product_id", productId)
                .orderByDesc("created_at");
        Page<Review> result = reviewMapper.selectPage(new Page<>(page, pageSize), qw);
        for (Review r : result.getRecords()) {
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                user.setPasswordHash(null);
                r.setUser(user);
            }
        }
        return result;
    }

    public Page<Review> adminList(int page, int pageSize, Integer productId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        if (productId != null) {
            qw.eq("product_id", productId);
        }
        qw.orderByDesc("created_at");
        Page<Review> result = reviewMapper.selectPage(new Page<>(page, pageSize), qw);
        for (Review r : result.getRecords()) {
            User user = userMapper.selectById(r.getUserId());
            if (user != null) {
                user.setPasswordHash(null);
                r.setUser(user);
            }
        }
        return result;
    }

    public Review create(Review review) {
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());
        reviewMapper.insert(review);
        return review;
    }
}
