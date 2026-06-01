package com.ecommerce.filter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebFilter(urlPatterns = "/api/*")
@Component
public class JwtAuthFilter implements Filter {

    private static final List<String> WHITELIST = Arrays.asList(
            "/api/auth/",
            "/api/public/",
            "/api/products",
            "/api/reviews",
            "/api/banners",
            "/api/announcements",
            "/api/coupons",
            "/api/flashsales",
            "/api/admin/categories"
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();
        String method = req.getMethod();

        // Allow uploads and servlet/jsp paths
        if (path.startsWith("/uploads/") || path.startsWith("/servlet/") || path.startsWith("/jsp/")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Check whitelist
        boolean isWhitelisted = false;
        for (String wl : WHITELIST) {
            if (path.startsWith(wl)) {
                // GET for public resources is always allowed
                // POST for auth endpoints is allowed
                if ("GET".equalsIgnoreCase(method) || path.startsWith("/api/auth/") || path.startsWith("/api/public/")) {
                    isWhitelisted = true;
                    break;
                }
            }
        }

        if (isWhitelisted) {
            chain.doFilter(request, response);
            return;
        }

        // Authenticate
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(resp, 401, "请先登录");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            sendError(resp, 401, "登录已过期，请重新登录");
            return;
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            sendError(resp, 401, "用户不存在");
            return;
        }
        if ("disabled".equals(user.getStatus())) {
            sendError(resp, 401, "账号已被禁用");
            return;
        }

        // Admin check for /api/admin/*
        if (path.startsWith("/api/admin/")) {
            if (!"admin".equals(user.getRole())) {
                sendError(resp, 403, "无管理员权限");
                return;
            }
        }

        req.setAttribute("userId", userId);
        req.setAttribute("user", user);
        chain.doFilter(request, response);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
