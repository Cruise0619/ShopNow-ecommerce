package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Message;
import com.ecommerce.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ApiResponse<List<Message>> list(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        return ApiResponse.ok(messageService.getConversation(userId));
    }

    @PostMapping
    public ApiResponse<Message> send(@RequestBody Map<String, String> body,
                                      HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.error(400, "消息内容不能为空");
        }
        Message msg = messageService.send(userId, messageService.getAdminId(), content);
        return ApiResponse.ok(msg);
    }
}
