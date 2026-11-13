package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.CartItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CartItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void findByItemId_returnsCartItem() {
        cartItemRepository.save(new CartItem(1L, 2)).block();

        CartItem cartItem = cartItemRepository.findByItemId(1L).block();

        assertEquals(1L, cartItem.getItemId());
        assertEquals(2, cartItem.getCount());
    }

    @Test
    void findByItemId_itemNotInCart_returnsEmpty() {
        assertNull(cartItemRepository.findByItemId(1L).block());
    }
}
