package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @org.apache.ibatis.annotations.Update(
        "UPDATE coupons SET used = used + 1 WHERE id = #{id} AND used < total")
    int claimAtomic(@org.apache.ibatis.annotations.Param("id") Integer id);

    @org.apache.ibatis.annotations.Update(
        "UPDATE coupons SET used = GREATEST(used - 1, 0) WHERE id = #{id} AND used > 0")
    int restoreAtomic(@org.apache.ibatis.annotations.Param("id") Integer id);
}
