package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.Feedback;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.FeedbackMapper;
import com.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private UserMapper userMapper;

    public Feedback create(Feedback feedback) {
        feedback.setStatus("pending");
        feedback.setCreatedAt(new Date());
        feedback.setUpdatedAt(new Date());
        feedbackMapper.insert(feedback);
        return feedback;
    }

    public Page<Feedback> listByUser(Integer userId, int page, int pageSize) {
        QueryWrapper<Feedback> qw = new QueryWrapper<Feedback>()
                .eq("user_id", userId)
                .orderByDesc("created_at");
        return feedbackMapper.selectPage(new Page<>(page, pageSize), qw);
    }

    public Page<Feedback> adminList(int page, int pageSize) {
        QueryWrapper<Feedback> qw = new QueryWrapper<Feedback>().orderByDesc("created_at");
        Page<Feedback> result = feedbackMapper.selectPage(new Page<>(page, pageSize), qw);
        for (Feedback f : result.getRecords()) {
            User user = userMapper.selectById(f.getUserId());
            if (user != null) {
                user.setPasswordHash(null);
                f.setUser(user);
            }
        }
        return result;
    }

    public void reply(Integer id, String reply) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb != null) {
            fb.setReply(reply);
            fb.setStatus("replied");
            feedbackMapper.updateById(fb);
        }
    }
}
