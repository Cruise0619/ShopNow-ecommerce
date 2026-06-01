package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Coupon;
import com.ecommerce.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public ApiResponse<List<Coupon>> list() {
        return ApiResponse.ok(couponService.listActive());
    }

    @PostMapping("/{id}/claim")
    public ApiResponse<?> claim(@PathVariable Integer id, HttpServletRequest req) {
        couponService.claim(id);
        return ApiResponse.ok(null);
    }
}
