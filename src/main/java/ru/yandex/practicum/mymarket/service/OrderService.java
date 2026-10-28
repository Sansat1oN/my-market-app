package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public Flux<OrderDto> findOrders() {
        return orderRepository.findAll()
                .concatMap(this::toDto);
    }

    @Transactional(readOnly = true)
    public Mono<OrderDto> findOrder(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Заказ не найден: " + id)))
                .flatMap(this::toDto);
    }

    @Transactional
    public Mono<Long> buy() {
        return cartItemRepository.findAll()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new EmptyCartException("Корзина пуста"));
                    }
                    return orderRepository.save(new Order())
                            .flatMap(order -> saveOrderItems(order, cartItems)
                                    .then(cartItemRepository.deleteAll())
                                    .thenReturn(order.getId()));
                });
    }

    private Flux<OrderItem> saveOrderItems(Order order, List<CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .concatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .flatMap(item -> orderItemRepository.save(
                                new OrderItem(order.getId(), item.getId(), item.getPrice(), cartItem.getCount()))));
    }

    private Mono<OrderDto> toDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .concatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                        .map(item -> new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(),
                                orderItem.getPrice(), orderItem.getCount())))
                .collectList()
                .map(items -> {
                    long totalSum = 0;
                    for (ItemDto item : items) {
                        totalSum += item.price() * item.count();
                    }
                    return new OrderDto(order.getId(), items, totalSum);
                });
    }
}
