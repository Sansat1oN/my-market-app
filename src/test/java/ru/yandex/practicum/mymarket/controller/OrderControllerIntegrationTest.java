package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderControllerIntegrationTest extends AbstractTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Test
    void buy_createsOrderAndClearsCart() {
        fillCart();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        List<OrderDto> orders = orderService.findOrders().collectList().block();
        assertEquals(1, orders.size());
        assertEquals(List.of(milk(), kefir()), orders.get(0).items());
        assertEquals(195L, orders.get(0).totalSum());
        assertEquals(List.of(), cartService.findItems().collectList().block());
    }

    @Test
    void buy_redirectsToNewOrder() {
        fillCart();

        String url = webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .returnResult(Void.class)
                .getResponseHeaders().getLocation().toString();

        long id = orderService.findOrders().blockFirst().id();
        assertEquals("/orders/" + id + "?newOrder=true", url);
    }

    @Test
    void buy_emptyCart_redirectsToCartWithoutOrder() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        assertEquals(List.of(), orderService.findOrders().collectList().block());
    }

    @Test
    void getOrders_returnsOrders() {
        fillCart();
        long id = orderService.buy().block();

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Заказ №" + id));
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное (2 шт.) 100 руб."));
                    assertTrue(html.contains("Кефир ПРОСТОКВАШИНО (1 шт.) 95 руб."));
                    assertTrue(html.contains("Сумма: 195 руб."));
                });
    }

    @Test
    void getOrder_newOrder_returnsOrderWithCongratulation() {
        fillCart();
        long id = orderService.buy().block();

        webTestClient.get()
                .uri("/orders/{id}?newOrder=true", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Поздравляем"));
                    assertTrue(html.contains("Заказ №" + id));
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Сумма: 195 руб."));
                });
    }

    @Test
    void getOrder_orderNotFound_404() {
        webTestClient.get()
                .uri("/orders/{id}", 100000L)
                .exchange()
                .expectStatus().isNotFound();
    }

    private void fillCart() {
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(2L, CartAction.PLUS).block();
    }

    private ItemDto milk() {
        return new ItemDto(1L, "Молоко ЛЕНТА пастеризованное", "Молоко питьевое пастеризованное 2,5%, 900 мл",
                "images/milk.jpg", 50L, 2);
    }

    private ItemDto kefir() {
        return new ItemDto(2L, "Кефир ПРОСТОКВАШИНО", "Кефир 2,5%, без змж, 930 г", "images/kefir.jpg", 95L, 1);
    }
}
