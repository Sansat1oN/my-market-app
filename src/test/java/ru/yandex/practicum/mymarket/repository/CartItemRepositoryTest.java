package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void findByItemId_returnsCartItem() {
        Item item = entityManager.find(Item.class, 1L);
        cartItemRepository.save(new CartItem(item, 2));
        entityManager.flush();
        entityManager.clear();

        CartItem cartItem = cartItemRepository.findByItemId(1L).orElseThrow();

        assertEquals(2, cartItem.getCount());
        assertEquals("Молоко ЛЕНТА пастеризованное", cartItem.getItem().getTitle());
    }

    @Test
    void findByItemId_itemNotInCart_returnsEmpty() {
        assertTrue(cartItemRepository.findByItemId(1L).isEmpty());
    }
}
