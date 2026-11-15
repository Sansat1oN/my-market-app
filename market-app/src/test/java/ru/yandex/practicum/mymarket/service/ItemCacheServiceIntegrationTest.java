package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCacheServiceIntegrationTest extends AbstractTest {

    private static final long TEST_ITEM_ID = 1000L;

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        clean().block();
        databaseClient.sql("insert into items (id, title, description, img_path, price) "
                        + "values (1000, 'Тестовый товар', 'Описание', 'images/test.jpg', 123)")
                .then()
                .block();
    }

    @AfterEach
    void tearDown() {
        clean().block();
    }

    @Test
    void findById_afterCacheMiss_loadsFromDatabaseAndUsesCache() {
        Item loaded = itemCacheService.findById(TEST_ITEM_ID).block();
        databaseClient.sql("delete from items where id = 1000").then().block();

        Item cached = itemCacheService.findById(TEST_ITEM_ID).block();

        assertEquals(loaded.getId(), cached.getId());
        assertEquals(loaded.getTitle(), cached.getTitle());
        assertEquals(loaded.getDescription(), cached.getDescription());
        assertEquals(loaded.getImgPath(), cached.getImgPath());
        assertEquals(loaded.getPrice(), cached.getPrice());
        long ttl = redisTemplate.getExpire("item:1000").block().toSeconds();
        assertTrue(ttl > 0 && ttl <= 120);
    }

    private Mono<Void> clean() {
        return databaseClient.sql("delete from items where id = 1000").then()
                .then(redisTemplate.delete("item:1000").then());
    }
}
