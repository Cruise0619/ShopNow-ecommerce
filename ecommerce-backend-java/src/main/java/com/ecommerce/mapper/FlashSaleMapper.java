package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.FlashSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FlashSaleMapper extends BaseMapper<FlashSale> {

    @Update("UPDATE flash_sales SET sold = sold + #{quantity} WHERE id = #{id} AND sold + #{quantity} <= stock")
    int deductStock(@Param("id") Integer id, @Param("quantity") int quantity);

    @Update("UPDATE flash_sales SET sold = GREATEST(sold - #{quantity}, 0) WHERE id = #{id}")
    int restoreStock(@Param("id") Integer id, @Param("quantity") int quantity);
}
