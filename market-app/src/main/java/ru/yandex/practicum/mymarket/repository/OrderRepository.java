package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    Flux<Order> findAllByUsername(String username);

    Mono<Order> findByUsernameAndId(String username, long id);
}
