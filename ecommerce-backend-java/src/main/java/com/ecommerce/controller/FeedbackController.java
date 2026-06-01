package com.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Feedback;
import com.ecommerce.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Page<Feedback> result = feedbackService.listByUser(userId, page, page_size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<Feedback> create(@RequestBody Map<String, String> body,
                                         HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Feedback fb = new Feedback();
        fb.setUserId(userId);
        fb.setContent(body.get("content"));
        fb.setStatus("pending");
        return ApiResponse.ok("提交成功", feedbackService.create(fb));
    }
}
