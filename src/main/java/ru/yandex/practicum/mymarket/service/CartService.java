package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public CartService(CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void changeCount(long itemId, CartAction action) {
        Optional<CartItem> found = cartItemRepository.findByItemId(itemId);
        if (found.isEmpty()) {
            if (action == CartAction.PLUS) {
                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new NotFoundException("Товар не найден: " + itemId));
                cartItemRepository.save(new CartItem(item, 1));
            }
            return;
        }
        CartItem cartItem = found.get();
        switch (action) {
            case PLUS -> cartItem.setCount(cartItem.getCount() + 1);
            case MINUS -> cartItem.setCount(cartItem.getCount() - 1);
            case DELETE -> cartItem.setCount(0);
        }
        if (cartItem.getCount() == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItemRepository.save(cartItem);
        }
    }
}
