package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void save_savesOrderWithItems() {
        Order order = new Order();
        order.addItem(entityManager.find(Item.class, 1L), 50L, 2);
        order.addItem(entityManager.find(Item.class, 2L), 95L, 1);
        Long id = orderRepository.save(order).getId();
        entityManager.flush();
        entityManager.clear();

        Order saved = orderRepository.findById(id).orElseThrow();

        assertEquals(2, saved.getItems().size());
        long totalSum = 0;
        for (OrderItem orderItem : saved.getItems()) {
            totalSum += orderItem.getPrice() * orderItem.getCount();
        }
        assertEquals(195L, totalSum);
    }
}
