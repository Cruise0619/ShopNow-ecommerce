package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.Banner;
import com.ecommerce.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    public List<Banner> listActive() {
        return bannerMapper.selectList(new QueryWrapper<Banner>()
                .eq("status", "on")
                .orderByAsc("sort_order"));
    }

    public List<Banner> listAll() {
        return bannerMapper.selectList(new QueryWrapper<Banner>().orderByAsc("sort_order"));
    }
}
