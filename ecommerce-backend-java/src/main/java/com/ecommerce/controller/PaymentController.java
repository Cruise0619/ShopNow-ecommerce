package com.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/public/orders")
public class PaymentController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/{payToken}")
    public ApiResponse<Map<String, Object>> getOrder(@PathVariable String payToken) {
        Order order = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("pay_token", payToken));
        if (order == null) {
            return ApiResponse.error(404, "支付链接无效");
        }
        if ("paid".equals(order.getStatus()) || "pending_shipment".equals(order.getStatus())
                || "shipped".equals(order.getStatus()) || "completed".equals(order.getStatus())) {
            return ApiResponse.error(400, "该订单已支付");
        }
        if ("cancelled".equals(order.getStatus()) || "refunding".equals(order.getStatus())) {
            return ApiResponse.error(400, "该订单已取消");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (OrderItem oi : items) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            Map<String, Object> snapshot = oi.getProductSnapshot();
            itemMap.put("name", snapshot != null ? snapshot.get("name") : "");
            itemMap.put("images", snapshot != null ? snapshot.get("images") : "");
            itemMap.put("specs", snapshot != null ? snapshot.get("specs") : "");
            itemMap.put("price", oi.getPrice());
            itemMap.put("quantity", oi.getQuantity());
            itemList.add(itemMap);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("totalAmount", order.getTotalAmount());
        data.put("discountAmount", order.getDiscountAmount());
        data.put("paymentAmount", order.getPaymentAmount());
        data.put("paymentMethod", order.getPaymentMethod());
        data.put("status", order.getStatus());
        data.put("items", itemList);

        return ApiResponse.ok(data);
    }

    @PutMapping("/{payToken}/pay")
    @Transactional
    public ApiResponse<?> pay(@PathVariable String payToken) {
        Order order = orderMapper.selectOne(
                new QueryWrapper<Order>().eq("pay_token", payToken));
        if (order == null) {
            return ApiResponse.error(404, "支付链接无效");
        }
        if (!"pending_payment".equals(order.getStatus())) {
            if ("pending_shipment".equals(order.getStatus()) || "shipped".equals(order.getStatus())
                    || "completed".equals(order.getStatus())) {
                return ApiResponse.error(400, "该订单已支付");
            }
            return ApiResponse.error(400, "当前订单状态不可支付");
        }

        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", order.getId()).eq("status", "pending_payment");
        uw.set("status", "pending_shipment");
        uw.set("payment_time", new Date());
        int affected = orderMapper.update(null, uw);
        if (affected == 0) {
            return ApiResponse.error(400, "支付失败，订单状态已变更");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("orderId", order.getId());
        return ApiResponse.ok("支付成功", data);
    }
}
