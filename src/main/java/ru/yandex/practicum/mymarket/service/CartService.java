package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public CartService(CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    public Flux<ItemDto> findItems() {
        return cartItemRepository.findAll()
                .concatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(),
                                item.getPrice(), cartItem.getCount())));
    }

    public long calculateTotal(List<ItemDto> items) {
        long total = 0;
        for (ItemDto item : items) {
            total += item.price() * item.count();
        }
        return total;
    }

    @Transactional
    public Mono<Void> changeCount(long itemId, CartAction action) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(cartItem -> changeCount(cartItem, action).thenReturn(cartItem))
                .switchIfEmpty(Mono.defer(() -> addItem(itemId, action)))
                .then();
    }

    private Mono<Void> changeCount(CartItem cartItem, CartAction action) {
        switch (action) {
            case PLUS -> cartItem.setCount(cartItem.getCount() + 1);
            case MINUS -> cartItem.setCount(cartItem.getCount() - 1);
            case DELETE -> cartItem.setCount(0);
        }
        if (cartItem.getCount() == 0) {
            return cartItemRepository.delete(cartItem);
        }
        return cartItemRepository.save(cartItem).then();
    }

    private Mono<CartItem> addItem(long itemId, CartAction action) {
        if (action != CartAction.PLUS) {
            return Mono.empty();
        }
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new NotFoundException("Товар не найден: " + itemId)))
                .flatMap(item -> cartItemRepository.save(new CartItem(item.getId(), 1)));
    }
}
