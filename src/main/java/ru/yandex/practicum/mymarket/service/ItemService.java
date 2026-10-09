package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.HashMap;
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

    public Page<ItemDto> findItems(String search, SortType sort, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));
        Page<Item> items;
        if (search == null || search.isBlank()) {
            items = itemRepository.findAll(pageable);
        } else {
            items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        }
        Map<Long, Integer> counts = new HashMap<>();
        for (CartItem cartItem : cartItemRepository.findAll()) {
            counts.put(cartItem.getItem().getId(), cartItem.getCount());
        }
        return items.map(item -> toDto(item, counts.getOrDefault(item.getId(), 0)));
    }

    public ItemDto findItem(long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар не найден: " + id));
        int count = cartItemRepository.findByItemId(id)
                .map(CartItem::getCount)
                .orElse(0);
        return toDto(item, count);
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
