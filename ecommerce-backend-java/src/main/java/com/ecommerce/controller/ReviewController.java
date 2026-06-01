package com.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Review;
import com.ecommerce.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam Integer product_id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size) {
        Page<Review> result = reviewService.list(product_id, page, page_size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<Review> create(
            @RequestParam Integer productId,
            @RequestParam(required = false) Integer orderId,
            @RequestParam Integer rating,
            @RequestParam String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest req) throws IOException {
        Integer userId = (Integer) req.getAttribute("userId");
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String ext = getExt(file.getOriginalFilename());
                    String filename = System.currentTimeMillis() + "-" + new Random().nextInt(10000) + ext;
                    file.transferTo(new File(uploadPath, filename));
                    imageUrls.add("/uploads/" + filename);
                }
            }
            review.setImages(imageUrls);
        }

        return ApiResponse.ok(reviewService.create(review));
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
