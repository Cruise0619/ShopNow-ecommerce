package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    public User findByEmail(String email) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
    }

    public User findById(Integer id) {
        return userMapper.selectById(id);
    }

    public User create(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole("user");
        user.setStatus("active");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userMapper.insert(user);
        return user;
    }

    public void updatePassword(Integer userId, String newPassword) {
        User user = userMapper.selectById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public Page<User> page(int page, int pageSize, String keyword) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("role", "user");
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("username", keyword).or().like("phone", keyword).or().like("email", keyword));
        }
        qw.orderByDesc("id");
        return userMapper.selectPage(new Page<>(page, pageSize), qw);
    }
}
