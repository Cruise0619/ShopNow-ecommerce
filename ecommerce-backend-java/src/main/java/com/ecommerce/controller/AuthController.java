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
    private final Map<String, Integer> rateLimitCounter = new ConcurrentHashMap<>();
    private final Map<String, Long> rateLimitBlock = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static final int MAX_CODE_ATTEMPTS_PER_5MIN = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MS = 10 * 60 * 1000;
    private static final int CODE_EXPIRE_MS = 5 * 60 * 1000;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    private boolean isRateLimitBlocked(String key) {
        Long blockedUntil = rateLimitBlock.get(key);
        return blockedUntil != null && System.currentTimeMillis() < blockedUntil;
    }

    private boolean checkRateLimit(String key, int maxAttempts) {
        if (isRateLimitBlocked(key)) return false;
        int count = rateLimitCounter.merge(key, 1, Integer::sum);
        if (count > maxAttempts) {
            rateLimitBlock.put(key, System.currentTimeMillis() + BLOCK_DURATION_MS);
            rateLimitCounter.remove(key);
            return false;
        }
        if (count == 1) {
            long expireTime = System.currentTimeMillis() + BLOCK_DURATION_MS;
            rateLimitBlock.computeIfAbsent(key + "_expires", k -> expireTime);
        }
        return true;
    }

    private String rateLimiterKey(String prefix, String email) {
        return prefix + ":" + (email != null ? email.toLowerCase() : "");
    }

    @PostMapping("/send-code")
    public ApiResponse<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (!isValidEmail(email)) {
            return ApiResponse.error(400, "邮箱格式不正确");
        }
        User existing = userService.findByEmail(email);
        if (existing != null) {
            return ApiResponse.error(400, "该邮箱已被注册");
        }
        String rlKey = rateLimiterKey("sc", email);
        if (!checkRateLimit(rlKey, MAX_CODE_ATTEMPTS_PER_5MIN)) {
            return ApiResponse.error(429, "操作过于频繁，请10分钟后再试");
        }
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, code);
        codeExpire.put(email, System.currentTimeMillis() + CODE_EXPIRE_MS);
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

        codeStore.remove(email);
        codeExpire.remove(email);

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
        if (!isValidEmail(email)) {
            return ApiResponse.error(400, "邮箱格式不正确");
        }

        // Don't reveal whether the email exists (防止邮箱枚举)
        // But for course project usability, we still check and return a message

        User user = userService.findByEmail(email);
        if (user == null) {
            // Return same message to avoid user enumeration
            return ApiResponse.error(400, "该邮箱未注册");
        }

        String rlKey = rateLimiterKey("fp", email);
        if (!checkRateLimit(rlKey, MAX_CODE_ATTEMPTS_PER_5MIN)) {
            return ApiResponse.error(429, "操作过于频繁，请10分钟后再试");
        }

        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, code);
        codeExpire.put(email, System.currentTimeMillis() + CODE_EXPIRE_MS);
        Map<String, String> data = new HashMap<>();
        data.put("code", code);
        return ApiResponse.ok("验证码已发送", data);
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("password");

        if (!isValidEmail(email)) {
            return ApiResponse.error(400, "邮箱格式不正确");
        }
        if (code == null || code.isEmpty()) {
            return ApiResponse.error(400, "验证码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            return ApiResponse.error(400, "密码长度应在6-20个字符之间");
        }

        String rlKey = rateLimiterKey("rp", email);
        if (!checkRateLimit(rlKey, MAX_VERIFY_ATTEMPTS)) {
            return ApiResponse.error(429, "尝试次数过多，请10分钟后再试");
        }

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

        // Clear used code and rate limit
        codeStore.remove(email);
        codeExpire.remove(email);
        rateLimitCounter.remove(rlKey);

        return ApiResponse.ok("密码重置成功", null);
    }
}
