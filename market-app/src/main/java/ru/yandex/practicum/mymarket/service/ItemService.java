package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ItemService {

    private static final int ROW_SIZE = 3;
    private static final ItemDto STUB = new ItemDto(-1, null, null, null, 0, 0);

    private final CartItemRepository cartItemRepository;
    private final ItemCacheService itemCacheService;

    public ItemService(CartItemRepository cartItemRepository, ItemCacheService itemCacheService) {
        this.cartItemRepository = cartItemRepository;
        this.itemCacheService = itemCacheService;
    }

    public Mono<Page<ItemDto>> findItems(String username, String search, SortType sort, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Mono<List<Item>> items = itemCacheService.findAll()
                .map(allItems -> allItems.stream()
                        .filter(item -> matches(item, search))
                        .sorted(toComparator(sort))
                        .toList());
        Mono<Map<Long, Integer>> counts = username == null ? Mono.just(Map.of())
                : cartItemRepository.findAllByUsername(username).collectMap(CartItem::getItemId, CartItem::getCount);
        return Mono.zip(items, counts)
                .map(result -> {
                    List<Item> foundItems = result.getT1();
                    int from = Math.min((pageNumber - 1) * pageSize, foundItems.size());
                    int to = Math.min(from + pageSize, foundItems.size());
                    List<ItemDto> content = new ArrayList<>();
                    for (Item item : foundItems.subList(from, to)) {
                        content.add(toDto(item, result.getT2().getOrDefault(item.getId(), 0)));
                    }
                    return new PageImpl<>(content, pageable, foundItems.size());
                });
    }

    public Mono<ItemDto> findItem(String username, long id) {
        Mono<Item> item = itemCacheService.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Товар не найден: " + id)));
        Mono<Integer> count = username == null ? Mono.just(0)
                : cartItemRepository.findByUsernameAndItemId(username, id).map(CartItem::getCount).defaultIfEmpty(0);
        return Mono.zip(item, count)
                .map(result -> toDto(result.getT1(), result.getT2()));
    }

    public List<List<ItemDto>> splitIntoRows(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ROW_SIZE) {
            List<ItemDto> row = new ArrayList<>(items.subList(i, Math.min(i + ROW_SIZE, items.size())));
            while (row.size() < ROW_SIZE) {
                row.add(STUB);
            }
            rows.add(row);
        }
        return rows;
    }

    private boolean matches(Item item, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String query = search.toLowerCase(Locale.ROOT);
        return item.getTitle().toLowerCase(Locale.ROOT).contains(query)
                || item.getDescription().toLowerCase(Locale.ROOT).contains(query);
    }

    private Comparator<Item> toComparator(SortType sort) {
        return switch (sort) {
            case NO -> Comparator.comparing(Item::getId);
            case ALPHA -> Comparator.comparing(Item::getTitle).thenComparing(Item::getId);
            case PRICE -> Comparator.comparingLong(Item::getPrice).thenComparing(Item::getId);
        };
    }

    private ItemDto toDto(Item item, int count) {
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), count);
    }
}
