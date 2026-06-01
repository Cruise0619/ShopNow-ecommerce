package com.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false) String status,
            HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Page<Order> result = orderService.list(userId, status, page, page_size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("page_size", page_size);
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Integer addressId = (Integer) body.get("address_id");
        String paymentMethod = (String) body.get("payment_method");
        String remark = (String) body.get("remark");
        Integer couponId = body.get("coupon_id") != null ? (Integer) body.get("coupon_id") : null;
        @SuppressWarnings("unchecked")
        List<Integer> cartIds = (List<Integer>) body.get("cart_ids");
        Integer productId = (Integer) body.get("product_id");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : null;
        String specs = (String) body.get("specs");
        Order order = orderService.create(userId, addressId, paymentMethod, remark, cartIds, couponId,
                productId, quantity, specs);
        Map<String, Object> data = new HashMap<>();
        data.put("id", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("payToken", order.getPayToken());
        data.put("totalAmount", order.getTotalAmount());
        data.put("paymentAmount", order.getPaymentAmount());
        data.put("discountAmount", order.getDiscountAmount());
        return ApiResponse.ok(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<Order> detail(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        Order order = orderService.detail(id, userId);
        if (order == null) {
            return ApiResponse.error(404, "订单不存在");
        }
        return ApiResponse.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        orderService.cancel(userId, id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<?> confirmReceive(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        orderService.confirmReceive(userId, id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<?> pay(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        orderService.pay(userId, id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/refund")
    public ApiResponse<?> refund(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        orderService.refund(userId, id);
        return ApiResponse.ok(null);
    }
}
