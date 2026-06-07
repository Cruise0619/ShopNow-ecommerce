package com.ecommerce.controller;

import com.ecommerce.entity.User;
import com.ecommerce.service.ChatService;
import com.ecommerce.service.UserService;
import com.ecommerce.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public ChatController(ChatService chatService, JwtUtil jwtUtil, UserService userService) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping("/conversations")
    public Map<String, Object> conversations(@RequestHeader("Authorization") String auth) {
        Integer userId = normalizeUserId(auth);
        List<Map<String, Object>> list = chatService.getConversations(userId);
        return ok(list);
    }

    @GetMapping("/messages/{otherUserId}")
    public Map<String, Object> messages(@RequestHeader("Authorization") String auth,
                                        @PathVariable Integer otherUserId) {
        Integer userId = normalizeUserId(auth);
        chatService.markRead(userId, otherUserId);
        List<Map<String, Object>> list = chatService.getMessages(userId, otherUserId);
        return ok(list);
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestHeader("Authorization") String auth,
                                    @RequestBody Map<String, Object> body) {
        Integer fromUserId = extractUserId(auth);
        User user = userService.findById(fromUserId);
        String fromName = user != null ? user.getUsername() : "未知用户";
        boolean isAdmin = user != null && "admin".equals(user.getRole());

        Integer toUserId = body.get("toUserId") != null
                ? Integer.parseInt(body.get("toUserId").toString()) : 0;
        String content = body.get("content") != null ? body.get("content").toString() : "";
        if (content.trim().isEmpty()) {
            return error("消息内容不能为空");
        }

        // Normalize: all admin operations use virtual userId=0
        Integer effectiveFrom = isAdmin ? 0 : fromUserId;
        String effectiveName = isAdmin ? "客服" : fromName;

        Map<String, Object> msg = chatService.sendMessage(effectiveFrom, effectiveName, toUserId, content);
        return ok(msg);
    }

    @GetMapping("/unread")
    public Map<String, Object> unread(@RequestHeader("Authorization") String auth) {
        Integer userId = normalizeUserId(auth);
        int count = chatService.getTotalUnread(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return ok(data);
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", msg);
        return result;
    }

    private Integer extractUserId(String auth) {
        String token = auth.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    private Integer normalizeUserId(String auth) {
        Integer userId = extractUserId(auth);
        User user = userService.findById(userId);
        // Admin users always use virtual ID 0 for chat
        if (user != null && "admin".equals(user.getRole())) {
            return 0;
        }
        return userId;
    }
}
