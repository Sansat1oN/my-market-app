package ru.yandex.practicum.mymarket.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import ru.yandex.practicum.mymarket.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisConfigurationIntegrationTest extends AbstractTest {

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void cachePut_storesJsonValueWithTtl() {
        Cache cache = redisCacheManager.getCache("test");

        cache.put("key", "value");

        assertEquals("value", cache.get("key", String.class));
        assertEquals("\"value\"", redisTemplate.opsForValue().get("test::key"));
        Long ttl = redisTemplate.getExpire("test::key");
        assertTrue(ttl > 0 && ttl <= 120);
        cache.clear();
    }
}
