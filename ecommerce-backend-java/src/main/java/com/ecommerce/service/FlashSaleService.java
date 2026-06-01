package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.FlashSaleMapper;
import com.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FlashSaleService {

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<FlashSale> listActive() {
        Date now = new Date();
        QueryWrapper<FlashSale> qw = new QueryWrapper<FlashSale>()
                .le("start_time", now)
                .ge("end_time", now)
                .eq("status", "active")
                .orderByAsc("end_time");
        List<FlashSale> list = flashSaleMapper.selectList(qw);
        for (FlashSale fs : list) {
            Product p = productMapper.selectById(fs.getProductId());
            if (p != null) {
                p.setCategory(null);
                fs.setProduct(p);
            }
        }
        return list;
    }
}
