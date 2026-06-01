package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashsales")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping
    public ApiResponse<List<FlashSale>> list() {
        return ApiResponse.ok(flashSaleService.listActive());
    }
}
