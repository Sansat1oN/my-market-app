package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractControllerMockTest;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class OrderControllerTest extends AbstractControllerMockTest {

    @Test
    void getOrders_returnsOrders() {
        when(orderService.findOrders()).thenReturn(Flux.just(order(1L), order(2L)));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Заказ №1"));
                    assertTrue(html.contains("Заказ №2"));
                    assertTrue(html.contains("Товар 1 (2 шт.) 200 руб."));
                    assertTrue(html.contains("Сумма: 200 руб."));
                });
    }

    @Test
    void getOrder_withoutNewOrder_returnsOrderWithoutCongratulation() {
        when(orderService.findOrder(1L)).thenReturn(Mono.just(order(1L)));

        webTestClient.get()
                .uri("/orders/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Заказ №1"));
                    assertTrue(html.contains("Сумма: 200 руб."));
                    assertFalse(html.contains("Поздравляем"));
                });
    }

    @Test
    void getOrder_newOrder_returnsOrderWithCongratulation() {
        when(orderService.findOrder(1L)).thenReturn(Mono.just(order(1L)));

        webTestClient.get()
                .uri("/orders/{id}?newOrder=true", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Заказ №1"));
                    assertTrue(html.contains("Поздравляем"));
                });
    }

    @Test
    void getOrder_orderNotFound_404() {
        when(orderService.findOrder(100L)).thenReturn(Mono.error(new NotFoundException("Заказ не найден: 100")));

        webTestClient.get()
                .uri("/orders/{id}", 100L)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void buy_redirectsToNewOrder() {
        when(orderService.buy()).thenReturn(Mono.just(5L));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/5?newOrder=true");
    }

    @Test
    void buy_emptyCart_redirectsToCart() {
        when(orderService.buy()).thenReturn(Mono.error(new EmptyCartException("Корзина пуста")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");
    }

    private OrderDto order(long id) {
        List<ItemDto> items = List.of(new ItemDto(1L, "Товар 1", "Описание", "images/1.jpg", 100L, 2));
        return new OrderDto(id, items, 200L);
    }
}
