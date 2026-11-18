package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void save_savesOrderWithItems() {
        Long id = orderRepository.save(new Order()).block().getId();
        orderItemRepository.save(new OrderItem(id, 1L, 50L, 2)).block();
        orderItemRepository.save(new OrderItem(id, 2L, 95L, 1)).block();

        List<OrderItem> items = orderItemRepository.findByOrderId(id).collectList().block();

        assertNotNull(orderRepository.findById(id).block());
        assertEquals(2, items.size());
        long totalSum = 0;
        for (OrderItem orderItem : items) {
            totalSum += orderItem.getPrice() * orderItem.getCount();
        }
        assertEquals(195L, totalSum);
    }
}
