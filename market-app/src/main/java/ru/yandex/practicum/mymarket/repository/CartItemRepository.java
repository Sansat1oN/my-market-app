package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Flux<CartItem> findAllByUsername(String username);

    Mono<CartItem> findByUsernameAndItemId(String username, long itemId);

    @Modifying
    @Query("UPDATE cart_items SET count = GREATEST(count - :count, 0) WHERE username = :username AND item_id = :itemId")
    Mono<Integer> subtractPurchasedCount(String username, long itemId, int count);

    @Modifying
    @Query("DELETE FROM cart_items WHERE username = :username AND item_id = :itemId AND count = 0")
    Mono<Integer> deleteEmptyItem(String username, long itemId);
}
