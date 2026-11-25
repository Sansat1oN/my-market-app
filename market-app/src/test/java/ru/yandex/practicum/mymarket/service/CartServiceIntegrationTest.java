package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartServiceIntegrationTest extends AbstractTest {

    @Autowired
    private CartService cartService;

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
        databaseClient.sql("insert into cart_items (username, item_id, count) values ('buyer1', 1000, 2)")
                .then()
                .block();
    }

    @AfterEach
    void tearDown() {
        clean().block();
    }

    @Test
    void findItems_returnsItemFromCache() {
        itemCacheService.findById(1000L).block();
        databaseClient.sql("update items set title = 'Изменённый товар' where id = 1000").then().block();

        ItemDto item = cartService.findItems("buyer1").single().block();

        assertEquals("Тестовый товар", item.title());
        assertEquals(2, item.count());
        assertEquals(123L, item.price());
    }

    private Mono<Void> clean() {
        return databaseClient.sql("delete from cart_items where item_id = 1000").then()
                .then(databaseClient.sql("delete from items where id = 1000").then())
                .then(redisTemplate.delete("item:1000").then());
    }
}
