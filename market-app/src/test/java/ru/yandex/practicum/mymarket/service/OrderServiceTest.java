package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void findOrders_returnsOrdersWithTotalSum() {
        Order order = order(7L);
        when(orderRepository.findAll()).thenReturn(Flux.just(order));

        List<OrderDto> orders = orderService.findOrders().collectList().block();

        assertEquals(List.of(orderDto(7L)), orders);
    }

    @Test
    void findOrder_returnsOrderWithTotalSum() {
        Order order = order(7L);
        when(orderRepository.findById(7L)).thenReturn(Mono.just(order));

        assertEquals(orderDto(7L), orderService.findOrder(7L).block());
    }

    @Test
    void findOrder_unknownOrder_throwsNotFoundException() {
        when(orderRepository.findById(100L)).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> orderService.findOrder(100L).block());
    }

    @Test
    void buy_createsOrderFromCartAndClearsCart() {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(1L);
        when(item.getPrice()).thenReturn(100L);
        Order saved = mock(Order.class);
        when(saved.getId()).thenReturn(5L);
        when(cartItemRepository.findAll()).thenReturn(Flux.just(new CartItem(1L, 2)));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(saved));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        Long id = orderService.buy().block();

        assertEquals(5L, id);
        ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getOrderId());
        assertEquals(1L, captor.getValue().getItemId());
        assertEquals(100L, captor.getValue().getPrice());
        assertEquals(2, captor.getValue().getCount());
        verify(cartItemRepository).deleteAll();
    }

    @Test
    void buy_emptyCart_throwsEmptyCartException() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        assertThrows(EmptyCartException.class, () -> orderService.buy().block());

        verify(orderRepository, never()).save(any(Order.class));
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
        when(itemRepository.findById(1L)).thenReturn(Mono.just(milk));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(kefir));
        when(orderItemRepository.findByOrderId(id)).thenReturn(Flux.just(
                new OrderItem(id, 1L, 100L, 2),
                new OrderItem(id, 2L, 50L, 3)));
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        return order;
    }

    private OrderDto orderDto(long id) {
        return new OrderDto(id, List.of(
                new ItemDto(1L, "Молоко", "Описание", "images/milk.jpg", 100L, 2),
                new ItemDto(2L, "Кефир", "Описание", "images/kefir.jpg", 50L, 3)), 350L);
    }
}
