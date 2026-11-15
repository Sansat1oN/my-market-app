package ru.yandex.practicum.mymarket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.time.Duration;
import java.util.List;

@Service
public class ItemCacheService {

    private static final String ITEMS_KEY = "items";
    private static final String ITEM_KEY_PREFIX = "item:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public ItemCacheService(ReactiveRedisTemplate<String, String> redisTemplate, ItemRepository itemRepository,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    public Mono<List<Item>> findAll() {
        return redisTemplate.opsForValue().get(ITEMS_KEY)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.<List<Item>>readValue(json,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class))))
                .switchIfEmpty(Mono.defer(this::loadItems));
    }

    public Mono<Item> findById(long id) {
        return redisTemplate.opsForValue().get(ITEM_KEY_PREFIX + id)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, Item.class)))
                .switchIfEmpty(Mono.defer(() -> loadItem(id)));
    }

    private Mono<List<Item>> loadItems() {
        return itemRepository.findAll().collectList()
                .flatMap(items -> Mono.fromCallable(() -> objectMapper.writeValueAsString(items))
                        .flatMap(json -> redisTemplate.opsForValue().set(ITEMS_KEY, json, CACHE_TTL))
                        .thenReturn(items));
    }

    private Mono<Item> loadItem(long id) {
        return itemRepository.findById(id)
                .flatMap(item -> Mono.fromCallable(() -> objectMapper.writeValueAsString(item))
                        .flatMap(json -> redisTemplate.opsForValue().set(ITEM_KEY_PREFIX + id, json, CACHE_TTL))
                        .thenReturn(item));
    }
}
