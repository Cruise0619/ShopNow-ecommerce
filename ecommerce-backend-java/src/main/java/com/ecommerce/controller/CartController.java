package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItem>> list(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        return ApiResponse.ok(cartService.list(userId));
    }

    @PostMapping
    public ApiResponse<CartItem> add(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Integer productId = (Integer) body.get("product_id");
        String skuSpec = (String) body.get("sku_spec");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : 1;
        return ApiResponse.ok(cartService.add(userId, productId, skuSpec, quantity));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Integer id,
                                  @RequestBody Map<String, Object> body,
                                  HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : null;
        Boolean selected = body.get("selected") != null ? (Boolean) body.get("selected") : null;
        cartService.update(id, userId, quantity, selected);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<?> removeBatch(@RequestBody Map<String, Object> body,
                                       HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        cartService.removeBatch(ids, userId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(@PathVariable Integer id,
                                  HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        cartService.remove(id, userId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/select-all")
    public ApiResponse<?> selectAll(@RequestBody Map<String, Boolean> body, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        cartService.selectAll(userId, body.get("selected"));
        return ApiResponse.ok(null);
    }
}
