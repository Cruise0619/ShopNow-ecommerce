package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.Address;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Coupon;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.AddressMapper;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CouponMapper;
import com.ecommerce.mapper.FlashSaleMapper;
import com.ecommerce.mapper.OrderItemMapper;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.util.OrderNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    public Page<Order> list(Integer userId, String status, int page, int pageSize) {
        QueryWrapper<Order> qw = new QueryWrapper<Order>()
                .eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");
        Page<Order> result = orderMapper.selectPage(new Page<>(page, pageSize), qw);
        for (Order order : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            for (OrderItem item : items) {
                Product p = productMapper.selectById(item.getProductId());
                if (p != null) p.setCategory(null);
                item.setProduct(p);
            }
            order.setItems(items);
        }
        return result;
    }

    @Transactional
    public Order create(Integer userId, Integer addressId, String paymentMethod,
                         String remark, List<Integer> cartIds, Integer couponId,
                         Integer productId, Integer quantity, String specs) {
        // Validate address
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        boolean isDirectBuy = productId != null;

        // Get cart items (or create virtual cart item for direct buy)
        List<CartItem> cartItems;
        if (isDirectBuy) {
            // Direct purchase: create a virtual cart item from product info
            Product p = productMapper.selectById(productId);
            if (p == null) throw new BusinessException("商品不存在");
            if ("off".equals(p.getStatus())) throw new BusinessException("商品「" + p.getName() + "」已下架");
            if (quantity == null || quantity < 1) quantity = 1;

            CartItem virtual = new CartItem();
            virtual.setUserId(userId);
            virtual.setProductId(productId);
            virtual.setQuantity(quantity);
            virtual.setSkuSpec(specs);
            cartItems = List.of(virtual);
        } else if (cartIds != null && !cartIds.isEmpty()) {
            cartItems = cartItemMapper.selectBatchIds(cartIds);
            for (CartItem ci : cartItems) {
                if (!ci.getUserId().equals(userId)) {
                    throw new BusinessException("购物车项不属于当前用户");
                }
            }
        } else {
            cartItems = cartItemMapper.selectList(
                    new QueryWrapper<CartItem>().eq("user_id", userId).eq("selected", true));
        }

        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // Calculate total and resolve flash sale pricing
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = productMapper.selectById(ci.getProductId());
            if (p == null) throw new BusinessException("商品不存在，请刷新购物车");
            if ("off".equals(p.getStatus())) throw new BusinessException("商品「" + p.getName() + "」已下架，请删除后重试");

            FlashSale flashSale = findActiveFlashSale(ci.getProductId());
            BigDecimal itemPrice = flashSale != null ? flashSale.getFlashPrice() : p.getPrice();
            total = total.add(itemPrice.multiply(new BigDecimal(ci.getQuantity())));
        }

        // Create order
        Order order = new Order();
        order.setOrderNo(OrderNoUtil.generateOrderNo());
        order.setUserId(userId);

        Map<String, Object> addrSnapshot = new HashMap<>();
        addrSnapshot.put("receiver", address.getReceiver());
        addrSnapshot.put("phone", address.getPhone());
        addrSnapshot.put("province", address.getProvince());
        addrSnapshot.put("city", address.getCity());
        addrSnapshot.put("district", address.getDistrict());
        addrSnapshot.put("detail", address.getDetail());
        order.setAddressSnapshot(addrSnapshot);

        order.setTotalAmount(total);

        // Apply coupon discount if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponId != null) {
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) throw new BusinessException("优惠券不存在");
            if (!"active".equals(coupon.getStatus())) throw new BusinessException("优惠券不可用");
            Date now = new Date();
            if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
                throw new BusinessException("优惠券不在有效期内");
            }
            if (coupon.getMinAmount() != null && total.compareTo(coupon.getMinAmount()) < 0) {
                throw new BusinessException("订单金额未达到优惠券使用门槛");
            }
            if ("percent".equals(coupon.getType())) {
                discountAmount = total.multiply(coupon.getValue()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            } else {
                discountAmount = coupon.getValue();
            }
            if (discountAmount.compareTo(total) > 0) {
                discountAmount = total;
            }
            int claimed = couponMapper.claimAtomic(couponId);
            if (claimed == 0) throw new BusinessException("优惠券已被领完");
        }
        order.setDiscountAmount(discountAmount);
        order.setCouponId(couponId);
        order.setPaymentAmount(total.subtract(discountAmount));
        order.setPayToken(UUID.randomUUID().toString());
        order.setStatus("pending_payment");
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "alipay");
        order.setRemark(remark);
        orderMapper.insert(order);

        // Create order items and deduct stock atomically at creation time
        for (CartItem ci : cartItems) {
            Product p = productMapper.selectById(ci.getProductId());
            FlashSale flashSale = findActiveFlashSale(ci.getProductId());

            // Atomic stock deduction
            if (flashSale != null) {
                int fsAffected = flashSaleMapper.deductStock(flashSale.getId(), ci.getQuantity());
                if (fsAffected == 0) throw new BusinessException("秒杀商品「" + p.getName() + "」已售罄");
            }
            int prodAffected = productMapper.deductStock(ci.getProductId(), ci.getQuantity());
            if (prodAffected == 0) {
                throw new BusinessException("商品「" + p.getName() + "」库存不足，当前库存：" + p.getStock());
            }

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(ci.getProductId());
            oi.setPrice(flashSale != null ? flashSale.getFlashPrice() : p.getPrice());
            oi.setQuantity(ci.getQuantity());

            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("name", p.getName());
            snapshot.put("images", p.getImages());
            snapshot.put("specs", ci.getSkuSpec());
            if (flashSale != null) {
                snapshot.put("flashSaleId", flashSale.getId());
            }
            oi.setProductSnapshot(snapshot);
            orderItemMapper.insert(oi);
        }

        // Delete cart items (skip for direct buy — no real cart items to delete)
        if (!isDirectBuy) {
            for (CartItem ci : cartItems) {
                cartItemMapper.deleteById(ci.getId());
            }
        }

        return order;
    }

    public Order detail(Integer id, Integer userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) return null;
        if (userId != null && !order.getUserId().equals(userId)) return null;

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        for (OrderItem item : items) {
            Product p = productMapper.selectById(item.getProductId());
            if (p != null) p.setCategory(null);
            item.setProduct(p);
        }
        order.setItems(items);
        return order;
    }

    @Transactional
    public void cancel(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权操作此订单");
        if (!"pending_payment".equals(order.getStatus()) && !"pending_shipment".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }
        // Restore stock (was deducted at creation time for both statuses)
        restoreOrderStockAndCoupon(orderId);
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", orderId).in("status", "pending_payment", "pending_shipment");
        uw.set("status", "cancelled");
        int affected = orderMapper.update(null, uw);
        if (affected == 0) throw new BusinessException("取消失败，订单状态已变更");
    }

    @Transactional
    public void confirmReceive(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权操作此订单");
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", orderId).eq("status", "shipped");
        uw.set("status", "completed");
        uw.set("receive_time", new Date());
        int affected = orderMapper.update(null, uw);
        if (affected == 0) throw new BusinessException("当前订单状态不可确认收货");
    }

    @Transactional
    public void pay(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权操作此订单");
        if (!"pending_payment".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可支付");
        }

        // Stock was already deducted at order creation.
        // Only perform atomic status transition here to prevent double payment.
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", orderId).eq("status", "pending_payment");
        uw.set("status", "pending_shipment");
        uw.set("payment_time", new Date());
        int affected = orderMapper.update(null, uw);
        if (affected == 0) throw new BusinessException("支付失败，订单状态已变更");
    }

    @Transactional
    public void refund(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException("无权操作此订单");
        if (!"completed".equals(order.getStatus())) {
            throw new BusinessException("仅已完成订单可申请退款");
        }
        if (order.getPaymentTime() != null) {
            long diff = System.currentTimeMillis() - order.getPaymentTime().getTime();
            if (diff > 7L * 24 * 60 * 60 * 1000) {
                throw new BusinessException("已超过7天退款期限");
            }
        }
        restoreOrderStockAndCoupon(orderId);
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", orderId).eq("status", "completed");
        uw.set("status", "refunding");
        int affected = orderMapper.update(null, uw);
        if (affected == 0) throw new BusinessException("申请退款失败，订单状态已变更");
    }

    private FlashSale findActiveFlashSale(Integer productId) {
        Date now = new Date();
        List<FlashSale> list = flashSaleMapper.selectList(
                new QueryWrapper<FlashSale>()
                        .eq("product_id", productId)
                        .eq("status", "active")
                        .le("start_time", now)
                        .ge("end_time", now));
        return list.isEmpty() ? null : list.get(0);
    }

    private void restoreOrderStockAndCoupon(Integer orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        for (OrderItem oi : items) {
            productMapper.restoreStock(oi.getProductId(), oi.getQuantity());
            // Restore flash sale stock if this item was purchased via flash sale
            Map<String, Object> snapshot = oi.getProductSnapshot();
            if (snapshot != null && snapshot.get("flashSaleId") != null) {
                Integer flashSaleId = (Integer) snapshot.get("flashSaleId");
                flashSaleMapper.restoreStock(flashSaleId, oi.getQuantity());
            }
        }
        Order order = orderMapper.selectById(orderId);
        if (order != null && order.getCouponId() != null) {
            couponMapper.restoreAtomic(order.getCouponId());
        }
    }
}
