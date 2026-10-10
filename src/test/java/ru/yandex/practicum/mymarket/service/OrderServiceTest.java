package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void findOrders_returnsOrdersWithTotalSum() {
        Order order = order(7L);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDto> orders = orderService.findOrders();

        assertEquals(List.of(orderDto(7L)), orders);
    }

    @Test
    void findOrder_returnsOrderWithTotalSum() {
        Order order = order(7L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        assertEquals(orderDto(7L), orderService.findOrder(7L));
    }

    @Test
    void findOrder_unknownOrder_throwsNotFoundException() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.findOrder(100L));
    }

    @Test
    void buy_createsOrderFromCartAndClearsCart() {
        Item item = mock(Item.class);
        when(item.getPrice()).thenReturn(100L);
        when(cartItemRepository.findAll()).thenReturn(List.of(new CartItem(item, 2)));
        Order saved = mock(Order.class);
        when(saved.getId()).thenReturn(5L);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        long id = orderService.buy();

        assertEquals(5L, id);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        List<OrderItem> orderItems = captor.getValue().getItems();
        assertEquals(1, orderItems.size());
        assertSame(item, orderItems.get(0).getItem());
        assertEquals(100L, orderItems.get(0).getPrice());
        assertEquals(2, orderItems.get(0).getCount());
        verify(cartItemRepository).deleteAll();
    }

    private Order order(long id) {
        Item milk = mock(Item.class);
        when(milk.getId()).thenReturn(1L);
        when(milk.getTitle()).thenReturn("Молоко");
        when(milk.getDescription()).thenReturn("Описание");
        when(milk.getImgPath()).thenReturn("images/milk.jpg");
        Item kefir = mock(Item.class);
        when(kefir.getId()).thenReturn(2L);
        when(kefir.getTitle()).thenReturn("Кефир");
        when(kefir.getDescription()).thenReturn("Описание");
        when(kefir.getImgPath()).thenReturn("images/kefir.jpg");
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        when(order.getItems()).thenReturn(List.of(
                new OrderItem(order, milk, 100L, 2),
                new OrderItem(order, kefir, 50L, 3)));
        return order;
    }

    private OrderDto orderDto(long id) {
        return new OrderDto(id, List.of(
                new ItemDto(1L, "Молоко", "Описание", "images/milk.jpg", 100L, 2),
                new ItemDto(2L, "Кефир", "Описание", "images/kefir.jpg", 50L, 3)), 350L);
    }
}
