package com.ecommerce.listener;

import com.ecommerce.service.FlashSaleService;
import com.ecommerce.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RedisInitRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RedisInitRunner.class);

    private final FlashSaleService flashSaleService;
    private final RedisService redisService;

    public RedisInitRunner(FlashSaleService flashSaleService, RedisService redisService) {
        this.flashSaleService = flashSaleService;
        this.redisService = redisService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            logger.info("Initializing Redis cache...");

            // Clear all object caches to prevent stale data without type info
            redisService.delete(redisService.getCartKey(0L));
            redisService.delete(redisService.getCartKey(1L));
            redisService.delete(redisService.getCartKey(2L));
            // Also clear prefix-based keys (cart:, product:, category:, banner:)
            // Individual cart cache entries are cleared above; products/banners/categories
            // are repopulated on first request

            flashSaleService.initAllFlashSaleStock();

            logger.info("Redis cache initialization completed successfully");
        } catch (Exception e) {
            logger.warn("Redis is not available, running without cache: {}", e.getMessage());
        }
    }
}
