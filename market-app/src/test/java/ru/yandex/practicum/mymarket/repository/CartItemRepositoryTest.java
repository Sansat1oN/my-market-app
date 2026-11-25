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
    void findByUsernameAndItemId_returnsCartItem() {
        cartItemRepository.save(new CartItem("buyer1", 1L, 2)).block();

        CartItem cartItem = cartItemRepository.findByUsernameAndItemId("buyer1", 1L).block();

        assertEquals(1L, cartItem.getItemId());
        assertEquals(2, cartItem.getCount());
    }

    @Test
    void findByUsernameAndItemId_itemNotInCart_returnsEmpty() {
        assertNull(cartItemRepository.findByUsernameAndItemId("buyer1", 1L).block());
    }

    @ParameterizedTest
    @CsvSource({"3, 2, 1", "2, 2, 0", "1, 2, 0"})
    void removePurchasedItems_preservesOnlyRemainingCount(int currentCount, int purchasedCount, int remainingCount) {
        cartItemRepository.save(new CartItem("buyer1", 1L, currentCount)).block();
        cartItemRepository.save(new CartItem("buyer1", 2L, 1)).block();

        cartItemRepository.subtractPurchasedCount("buyer1", 1L, purchasedCount)
                .then(cartItemRepository.deleteEmptyItem("buyer1", 1L)).block();

        CartItem remaining = cartItemRepository.findByUsernameAndItemId("buyer1", 1L).block();
        if (remainingCount == 0) {
            assertNull(remaining);
        } else {
            assertEquals(remainingCount, remaining.getCount());
        }
        assertEquals(1, cartItemRepository.findByUsernameAndItemId("buyer1", 2L).block().getCount());
    }

    @Test
    void removePurchasedItems_alreadyDeletedItem_doesNothing() {
        assertEquals(0, cartItemRepository.subtractPurchasedCount("buyer1", 1L, 2).block());
        assertEquals(0, cartItemRepository.deleteEmptyItem("buyer1", 1L).block());
    }
}
