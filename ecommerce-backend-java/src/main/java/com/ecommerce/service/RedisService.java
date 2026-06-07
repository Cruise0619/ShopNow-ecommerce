package com.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 缓存键前缀
    public static final String PRODUCT_CACHE_PREFIX = "product:";
    public static final String CART_CACHE_PREFIX = "cart:";
    public static final String FLASH_SALE_STOCK_PREFIX = "flash_sale:stock:";
    public static final String FLASH_SALE_LOCK_PREFIX = "flash_sale:lock:";
    public static final String CATEGORY_CACHE_PREFIX = "category:";
    public static final String BANNER_CACHE_PREFIX = "banner:";

    // 通用操作
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("Redis set failed for key={}: {}", key, e.getMessage());
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis set failed for key={}: {}", key, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            log.warn("Redis get failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis exists failed for key={}: {}", key, e.getMessage());
            return false;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete failed for key={}: {}", key, e.getMessage());
        }
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis expire failed for key={}: {}", key, e.getMessage());
        }
    }

    // 原子操作 - 递减
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.warn("Redis decrement failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    // 原子操作 - 递增
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.warn("Redis increment failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    // Hash操作
    public void hSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.warn("Redis hSet failed for key={}: {}", key, e.getMessage());
        }
    }

    public Object hGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.warn("Redis hGet failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    public void hDelete(String key, String hashKey) {
        try {
            redisTemplate.opsForHash().delete(key, hashKey);
        } catch (Exception e) {
            log.warn("Redis hDelete failed for key={}: {}", key, e.getMessage());
        }
    }

    public boolean hExists(String key, String hashKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, hashKey));
        } catch (Exception e) {
            log.warn("Redis hExists failed for key={}: {}", key, e.getMessage());
            return false;
        }
    }

    // 商品缓存操作
    public String getProductKey(Long productId) {
        return PRODUCT_CACHE_PREFIX + productId;
    }

    public void cacheProduct(Long productId, Object product) {
        String key = getProductKey(productId);
        set(key, product, 10, TimeUnit.MINUTES);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCachedProduct(Long productId, Class<T> clazz) {
        String key = getProductKey(productId);
        return (T) get(key);
    }

    public void invalidateProductCache(Long productId) {
        String key = getProductKey(productId);
        delete(key);
    }

    // 购物车缓存操作
    public String getCartKey(Long userId) {
        return CART_CACHE_PREFIX + userId;
    }

    // 秒杀库存操作
    public String getFlashSaleStockKey(Long flashSaleId) {
        return FLASH_SALE_STOCK_PREFIX + flashSaleId;
    }

    public String getFlashSaleLockKey(Long flashSaleId, Long userId) {
        return FLASH_SALE_LOCK_PREFIX + flashSaleId + ":" + userId;
    }

    private static final String DECREMENT_STOCK_LUA =
            "local stock = redis.call('GET', KEYS[1])\n" +
            "if not stock then return -1 end\n" +
            "stock = tonumber(stock)\n" +
            "if stock < tonumber(ARGV[1]) then return 0 end\n" +
            "return redis.call('DECRBY', KEYS[1], ARGV[1])";

    public boolean tryDecrementStock(Long flashSaleId, int quantity) {
        try {
            String key = getFlashSaleStockKey(flashSaleId);
            Long result = redisTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(DECREMENT_STOCK_LUA, Long.class),
                    java.util.Collections.singletonList(key),
                    String.valueOf(quantity)
            );
            return result != null && result > 0;
        } catch (Exception e) {
            log.warn("Redis tryDecrementStock failed for flashSaleId={}: {}", flashSaleId, e.getMessage());
            return false;
        }
    }
}
