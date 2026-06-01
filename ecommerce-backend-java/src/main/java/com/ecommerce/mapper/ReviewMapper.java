package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT AVG(rating) FROM reviews WHERE product_id = #{productId}")
    Double avgRating(@Param("productId") Integer productId);

    @Select("SELECT COUNT(*) FROM reviews WHERE product_id = #{productId}")
    Integer countByProductId(@Param("productId") Integer productId);
}
