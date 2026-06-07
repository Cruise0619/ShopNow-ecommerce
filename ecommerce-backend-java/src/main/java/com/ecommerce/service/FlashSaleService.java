package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.FlashSale;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.FlashSaleMapper;
import com.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleService {

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisService redisService;

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
            // 获取 Redis 中的实时库存
            fs.setStock(getFlashSaleStock(fs.getId()));
        }
        return list;
    }

    public FlashSale getById(Integer id) {
        return flashSaleMapper.selectById(id);
    }

    public boolean checkFlashSaleValid(Integer flashSaleId, Integer userId) {
        FlashSale fs = flashSaleMapper.selectById(flashSaleId);
        if (fs == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        Date now = new Date();
        if (now.before(fs.getStartTime())) {
            throw new BusinessException("秒杀活动尚未开始");
        }
        if (now.after(fs.getEndTime())) {
            throw new BusinessException("秒杀活动已结束");
        }
        if (!"active".equals(fs.getStatus())) {
            throw new BusinessException("秒杀活动已暂停");
        }

        return true;
    }

    public int getFlashSaleStock(Integer flashSaleId) {
        String stockKey = redisService.getFlashSaleStockKey(Long.valueOf(flashSaleId));
        Object stockObj = redisService.get(stockKey);
        if (stockObj != null) {
            return Integer.parseInt(stockObj.toString());
        }

        FlashSale fs = flashSaleMapper.selectById(flashSaleId);
        if (fs != null) {
            return fs.getStock();
        }
        return 0;
    }

    public boolean tryDeductStock(Integer flashSaleId, Integer userId, int quantity) {
        String lockKey = redisService.getFlashSaleLockKey(Long.valueOf(flashSaleId), Long.valueOf(userId));

        if (Boolean.TRUE.equals(redisService.exists(lockKey))) {
            throw new BusinessException("您已经参与过本次秒杀");
        }

        String stockKey = redisService.getFlashSaleStockKey(Long.valueOf(flashSaleId));

        if (Boolean.FALSE.equals(redisService.exists(stockKey))) {
            FlashSale fs = flashSaleMapper.selectById(flashSaleId);
            if (fs == null) {
                throw new BusinessException("秒杀活动不存在");
            }
            redisService.set(stockKey, fs.getStock());
        }

        boolean success = redisService.tryDecrementStock(Long.valueOf(flashSaleId), quantity);
        if (!success) {
            return false;
        }

        redisService.set(lockKey, "1", 24, TimeUnit.HOURS);
        return true;
    }

    public void initFlashSaleStock(Integer flashSaleId) {
        FlashSale fs = flashSaleMapper.selectById(flashSaleId);
        if (fs != null) {
            String stockKey = redisService.getFlashSaleStockKey(Long.valueOf(flashSaleId));
            redisService.set(stockKey, fs.getStock());
        }
    }

    public void initAllFlashSaleStock() {
        List<FlashSale> activeSales = listActive();
        for (FlashSale fs : activeSales) {
            initFlashSaleStock(fs.getId());
        }
    }
}
