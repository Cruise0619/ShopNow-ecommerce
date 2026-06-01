package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Favorite;
import com.ecommerce.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<List<Favorite>> list(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        return ApiResponse.ok(favoriteService.list(userId));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Map<String, Object>> toggle(@PathVariable Integer productId,
                                                    HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        return ApiResponse.ok(favoriteService.toggle(userId, productId));
    }
}
