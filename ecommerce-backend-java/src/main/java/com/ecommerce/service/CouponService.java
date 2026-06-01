package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CouponService {

    @Autowired
    private CouponMapper couponMapper;

    public List<Coupon> listActive() {
        Date now = new Date();
        return couponMapper.selectList(new QueryWrapper<Coupon>()
                .le("start_time", now)
                .ge("end_time", now)
                .eq("status", "active"));
    }

    public List<Coupon> listAll() {
        return couponMapper.selectList(new QueryWrapper<Coupon>().orderByDesc("created_at"));
    }

    public void claim(Integer id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || !"active".equals(coupon.getStatus())) {
            throw new BusinessException("优惠券不可用");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            throw new BusinessException("优惠券不在有效期内");
        }
        int affected = couponMapper.claimAtomic(id);
        if (affected == 0) {
            throw new BusinessException("优惠券已被领完");
        }
    }
}
