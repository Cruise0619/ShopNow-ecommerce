package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import com.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Map<String, String> codeStore = new ConcurrentHashMap<>();
    private final Map<String, Long> codeExpire = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/send-code")
    public ApiResponse<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ApiResponse.error(400, "邮箱不能为空");
        }
        User existing = userService.findByEmail(email);
        if (existing != null) {
            return ApiResponse.error(400, "该邮箱已被注册");
        }
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, code);
        codeExpire.put(email, System.currentTimeMillis() + 5 * 60 * 1000);
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        return ApiResponse.ok("验证码已发送", data);
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String phone = body.get("phone");
        String code = body.get("code");

        if (username == null || email == null || password == null) {
            return ApiResponse.error(400, "请填写完整信息");
        }

        User emailUser = userService.findByEmail(email);
        if (emailUser != null) {
            return ApiResponse.error(400, "该邮箱已被注册");
        }

        String storedCode = codeStore.get(email);
        Long expire = codeExpire.get(email);
        if (storedCode == null || expire == null || System.currentTimeMillis() > expire) {
            return ApiResponse.error(400, "验证码已过期");
        }
        if (!storedCode.equals(code)) {
            return ApiResponse.error(400, "验证码错误");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password);
        if (phone != null) user.setPhone(phone);
        user = userService.create(user);

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("uid", user.getUid());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("avatar", user.getAvatar());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userData);
        return ApiResponse.ok("注册成功", result);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userService.findByUsername(username);
        if (user == null || "disabled".equals(user.getStatus())) {
            return ApiResponse.error(400, "账号不存在或已被禁用");
        }
        if (!userService.verifyPassword(user, password)) {
            return ApiResponse.error(400, "密码错误");
        }

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("uid", user.getUid());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("avatar", user.getAvatar());
        userData.put("role", user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userData);
        return ApiResponse.ok("登录成功", result);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.error(400, "该邮箱未注册");
        }
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, code);
        codeExpire.put(email, System.currentTimeMillis() + 5 * 60 * 1000);
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        return ApiResponse.ok("验证码已发送", data);
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("password");

        String storedCode = codeStore.get(email);
        Long expire = codeExpire.get(email);
        if (storedCode == null || expire == null || System.currentTimeMillis() > expire) {
            return ApiResponse.error(400, "验证码已过期");
        }
        if (!storedCode.equals(code)) {
            return ApiResponse.error(400, "验证码错误");
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }
        userService.updatePassword(user.getId(), newPassword);
        return ApiResponse.ok("密码重置成功", null);
    }
}
