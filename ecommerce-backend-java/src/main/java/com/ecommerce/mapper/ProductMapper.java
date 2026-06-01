package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT p.*, c.name AS category_name FROM products p " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE p.category_id IN (${categoryIds}) AND p.status = 'on' " +
            "ORDER BY ${sortSql}")
    List<Product> selectWithCategory(@Param("categoryIds") String categoryIds,
                                     @Param("sortSql") String sortSql);

    @Update("UPDATE products SET stock = stock - #{quantity}, sales = sales + #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Integer id, @Param("quantity") int quantity);

    @Update("UPDATE products SET stock = stock + #{quantity}, sales = sales - #{quantity} " +
            "WHERE id = #{id}")
    int restoreStock(@Param("id") Integer id, @Param("quantity") int quantity);
}
