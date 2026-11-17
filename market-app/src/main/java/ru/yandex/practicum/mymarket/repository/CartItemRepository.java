package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByItemId(Long itemId);

    @Modifying
    @Query("UPDATE cart_items SET count = GREATEST(count - :count, 0) WHERE item_id = :itemId")
    Mono<Integer> subtractPurchasedCount(long itemId, int count);

    @Modifying
    @Query("DELETE FROM cart_items WHERE item_id = :itemId AND count = 0")
    Mono<Integer> deleteEmptyItem(long itemId);
}
