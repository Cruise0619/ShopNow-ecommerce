package com.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@TableName(value = "order_items", autoResultMap = true)
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;
    private Integer productId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> productSnapshot;

    private BigDecimal price;
    private Integer quantity;
    private Date createdAt;
    private Date updatedAt;

    @TableField(exist = false)
    private Product product;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Map<String, Object> getProductSnapshot() { return productSnapshot; }
    public void setProductSnapshot(Map<String, Object> productSnapshot) { this.productSnapshot = productSnapshot; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
