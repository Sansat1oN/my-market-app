package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    private static final int ROW_SIZE = 3;
    private static final ItemDto STUB = new ItemDto(-1, null, null, null, 0, 0);

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;

    public ItemService(ItemRepository itemRepository, CartItemRepository cartItemRepository) {
        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Mono<Page<ItemDto>> findItems(String search, SortType sort, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));
        Flux<Item> items;
        Mono<Long> total;
        if (search == null || search.isBlank()) {
            items = itemRepository.findAllBy(pageable);
            total = itemRepository.count();
        } else {
            items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
            total = itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);
        }
        Mono<Map<Long, Integer>> counts = cartItemRepository.findAll()
                .collectMap(CartItem::getItemId, CartItem::getCount);
        return Mono.zip(items.collectList(), total, counts)
                .map(result -> {
                    List<ItemDto> content = new ArrayList<>();
                    for (Item item : result.getT1()) {
                        content.add(toDto(item, result.getT3().getOrDefault(item.getId(), 0)));
                    }
                    return new PageImpl<>(content, pageable, result.getT2());
                });
    }

    public Mono<ItemDto> findItem(long id) {
        Mono<Item> item = itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Товар не найден: " + id)));
        Mono<Integer> count = cartItemRepository.findByItemId(id)
                .map(CartItem::getCount)
                .defaultIfEmpty(0);
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

    private Sort toSort(SortType sort) {
        return switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by("title", "id");
            case PRICE -> Sort.by("price", "id");
        };
    }

    private ItemDto toDto(Item item, int count) {
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), count);
    }
}
