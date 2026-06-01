package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<Map<String, Object>> get(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        User user = userService.findById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        return ApiResponse.ok(data);
    }

    @PutMapping
    public ApiResponse<?> update(@RequestBody Map<String, String> body, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        User user = userService.findById(userId);
        if (body.containsKey("username")) user.setUsername(body.get("username"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        userService.update(user);
        return ApiResponse.ok(null);
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody Map<String, String> body,
                                          HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        String oldPassword = body.get("old_password");
        String newPassword = body.get("new_password");

        User user = userService.findById(userId);
        if (!userService.verifyPassword(user, oldPassword)) {
            return ApiResponse.error(400, "原密码错误");
        }
        userService.updatePassword(userId, newPassword);
        return ApiResponse.ok(null);
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                                          HttpServletRequest req) throws IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        String ext = getExt(file.getOriginalFilename());
        String filename = System.currentTimeMillis() + "-" + new Random().nextInt(10000) + ext;
        File dest = new File(uploadPath, filename);
        file.transferTo(dest);

        String avatarUrl = "/uploads/" + filename;
        User user = userService.findById(userId);
        user.setAvatar(avatarUrl);
        userService.update(user);

        Map<String, String> data = new HashMap<>();
        data.put("avatar", avatarUrl);
        return ApiResponse.ok(data);
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
