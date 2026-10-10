package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findOrders() {
        List<OrderDto> orders = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            orders.add(toDto(order));
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public OrderDto findOrder(long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ не найден: " + id));
        return toDto(order);
    }

    @Transactional
    public long buy() {
        Order order = new Order();
        for (CartItem cartItem : cartItemRepository.findAll()) {
            Item item = cartItem.getItem();
            order.addItem(item, item.getPrice(), cartItem.getCount());
        }
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAll();
        return saved.getId();
    }

    private OrderDto toDto(Order order) {
        List<ItemDto> items = new ArrayList<>();
        long totalSum = 0;
        for (OrderItem orderItem : order.getItems()) {
            Item item = orderItem.getItem();
            items.add(new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(),
                    orderItem.getPrice(), orderItem.getCount()));
            totalSum += orderItem.getPrice() * orderItem.getCount();
        }
        return new OrderDto(order.getId(), items, totalSum);
    }
}
