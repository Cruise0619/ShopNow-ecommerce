package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@TableName(value = "orders", autoResultMap = true)
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer userId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> addressSnapshot;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private Integer couponId;
    private BigDecimal paymentAmount;
    private String status;
    private String paymentMethod;
    private Date paymentTime;
    private Date shippingTime;
    private Date receiveTime;
    private String trackingNo;
    private String payToken;
    private String remark;
    private Date createdAt;
    private Date updatedAt;

    @TableField(exist = false)
    private List<OrderItem> items;

    @TableField(exist = false)
    private User user;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Map<String, Object> getAddressSnapshot() { return addressSnapshot; }
    public void setAddressSnapshot(Map<String, Object> addressSnapshot) { this.addressSnapshot = addressSnapshot; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Integer getCouponId() { return couponId; }
    public void setCouponId(Integer couponId) { this.couponId = couponId; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Date getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Date paymentTime) { this.paymentTime = paymentTime; }
    public Date getShippingTime() { return shippingTime; }
    public void setShippingTime(Date shippingTime) { this.shippingTime = shippingTime; }
    public Date getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Date receiveTime) { this.receiveTime = receiveTime; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getPayToken() { return payToken; }
    public void setPayToken(String payToken) { this.payToken = payToken; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
