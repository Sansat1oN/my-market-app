package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({"3, 2, 1", "2, 2, 0", "1, 2, 0"})
    void removePurchasedItems_preservesOnlyRemainingCount(int currentCount, int purchasedCount, int remainingCount) {
        cartItemRepository.save(new CartItem(1L, currentCount)).block();
        cartItemRepository.save(new CartItem(2L, 1)).block();

        cartItemRepository.subtractPurchasedCount(1L, purchasedCount)
                .then(cartItemRepository.deleteEmptyItem(1L)).block();

        CartItem remaining = cartItemRepository.findByItemId(1L).block();
        if (remainingCount == 0) {
            assertNull(remaining);
        } else {
            assertEquals(remainingCount, remaining.getCount());
        }
        assertEquals(1, cartItemRepository.findByItemId(2L).block().getCount());
    }

    @Test
    void removePurchasedItems_alreadyDeletedItem_doesNothing() {
        assertEquals(0, cartItemRepository.subtractPurchasedCount(1L, 2).block());
        assertEquals(0, cartItemRepository.deleteEmptyItem(1L).block());
    }
}
