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

@TableName(value = "products", autoResultMap = true)
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private String detail;
    private Integer categoryId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> specs;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private String status;
    private Integer sales;
    private Boolean isNew;
    private Boolean isHot;
    private Boolean isPromotion;
    private Date createdAt;
    private Date updatedAt;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private List<Review> reviews;

    @TableField(exist = false)
    private Double avgRating;

    @TableField(exist = false)
    private Integer reviewCount;

    public String getCoverImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    public String getImage() {
        return getCoverImage();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<Map<String, Object>> getSpecs() { return specs; }
    public void setSpecs(List<Map<String, Object>> specs) { this.specs = specs; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }
    public Boolean getIsNew() { return isNew; }
    public void setIsNew(Boolean isNew) { this.isNew = isNew; }
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    public Boolean getIsPromotion() { return isPromotion; }
    public void setIsPromotion(Boolean isPromotion) { this.isPromotion = isPromotion; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
}
