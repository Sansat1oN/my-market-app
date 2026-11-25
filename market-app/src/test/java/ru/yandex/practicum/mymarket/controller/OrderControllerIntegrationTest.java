package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.dto.PaymentResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.PaymentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerIntegrationTest extends AbstractBuyerControllerTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void buy_clearsOnlyCurrentBuyersCart() {
        WebTestClient secondBuyer = signInAs("buyer2");
        fillCart();
        allowPayment();
        secondBuyer.post().uri("/items/1")
                .body(BodyInserters.fromFormData("action", "PLUS"))
                .exchange().expectStatus().isOk();

        webTestClient.post().uri("/buy").exchange().expectStatus().is3xxRedirection();

        verify(paymentService).pay(195L);
        assertEquals(List.of(), cartService.findItems("buyer1").collectList().block());
        secondBuyer.get().uri("/items/1").exchange().expectStatus().isOk()
                .expectBody(String.class).value(html -> assertTrue(html.contains("<span>1</span>")));
        when(paymentService.getBalance()).thenReturn(Mono.just(new PaymentResult(PaymentStatus.SUCCESS, 805L)));
        secondBuyer.get().uri("/cart/items").exchange().expectStatus().isOk()
                .expectBody(String.class).value(html -> assertTrue(html.contains("Итого: 50 руб.")));
    }

    @Test
    void buy_createsOrderAndClearsCart() {
        fillCart();
        allowPayment();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        List<OrderDto> orders = orderService.findOrders().collectList().block();
        assertEquals(1, orders.size());
        assertEquals(List.of(milk(), kefir()), orders.get(0).items());
        assertEquals(195L, orders.get(0).totalSum());
        assertEquals(List.of(), cartService.findItems("buyer1").collectList().block());
    }

    @ParameterizedTest
    @CsvSource({"PLUS, 50", "MINUS, 50", "DELETE, 50", "PLUS, 150"})
    void buy_cartOrPriceChangesDuringPayment_createsOrderFromPaidSnapshot(CartAction action, long newPrice) {
        cartService.changeCount("buyer1", 1L, CartAction.PLUS).block();
        List<ItemDto> paidItems = cartService.findItems("buyer1").collectList().block();
        when(paymentService.pay(50L)).thenReturn(Mono.defer(() ->
                cartService.changeCount("buyer1", 1L, action)
                        .then(databaseClient.sql("update items set price = :price where id = 1")
                                .bind("price", newPrice).then())
                        .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, 950L))));

        try {
            webTestClient.post()
                    .uri("/buy")
                    .exchange()
                    .expectStatus().is3xxRedirection();

            List<OrderDto> orders = orderService.findOrders().collectList().block();
            assertEquals(1, orders.size());
            assertEquals(paidItems, orders.get(0).items());
            assertEquals(50L, orders.get(0).totalSum());
            verify(paymentService).pay(50L);
            List<ItemDto> remainingItems = cartService.findItems("buyer1").collectList().block();
            assertEquals(action == CartAction.PLUS ? 1 : 0, remainingItems.size());
            if (action == CartAction.PLUS) {
                assertEquals(1, remainingItems.get(0).count());
            }
        } finally {
            databaseClient.sql("update items set price = 50 where id = 1").then().block();
        }
    }

    @Test
    void buy_anotherItemAddedDuringPayment_keepsItInCart() {
        cartService.changeCount("buyer1", 1L, CartAction.PLUS).block();
        List<ItemDto> paidItems = cartService.findItems("buyer1").collectList().block();
        when(paymentService.pay(50L)).thenReturn(Mono.defer(() ->
                cartService.changeCount("buyer1", 2L, CartAction.PLUS)
                        .thenReturn(new PaymentResult(PaymentStatus.SUCCESS, 950L))));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        List<OrderDto> orders = orderService.findOrders().collectList().block();
        assertEquals(1, orders.size());
        assertEquals(paidItems, orders.get(0).items());
        assertEquals(50L, orders.get(0).totalSum());
        assertEquals(List.of(kefir()), cartService.findItems("buyer1").collectList().block());
        verify(paymentService).pay(50L);
    }

    @Test
    void buy_redirectsToNewOrder() {
        fillCart();
        allowPayment();

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

    @ParameterizedTest
    @CsvSource({
            "INSUFFICIENT_FUNDS, SUCCESS, 100, true, Оплата не прошла: недостаточно средств. Заказ не оформлен.",
            "INSUFFICIENT_FUNDS, SUCCESS, 1000, false, Оплата не прошла: недостаточно средств. Заказ не оформлен.",
            "UNAVAILABLE, UNAVAILABLE, 0, true, Оплата не прошла: сервис платежей недоступен. Заказ не оформлен.",
            "UNAVAILABLE, SUCCESS, 1000, false, Оплата не прошла: сервис платежей недоступен. Заказ не оформлен."
    })
    void buy_failedPayment_showsErrorAfterRedirectAndKeepsCart(PaymentStatus paymentStatus,
                                                               PaymentStatus balanceStatus, long balance,
                                                               boolean disabled, String message) {
        fillCart();
        when(paymentService.pay(195L)).thenReturn(Mono.just(
                new PaymentResult(paymentStatus, null)));
        when(paymentService.getBalance()).thenReturn(Mono.just(new PaymentResult(balanceStatus, balance)));

        String url = webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items?paymentStatus=" + paymentStatus)
                .returnResult(Void.class)
                .getResponseHeaders().getLocation().toString();

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains(message));
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Кефир ПРОСТОКВАШИНО"));
                    assertTrue(html.contains("Итого: 195 руб."));
                    assertEquals(disabled, html.contains("disabled=\"disabled\""));
                });

        assertEquals(List.of(), orderService.findOrders().collectList().block());
        assertEquals(List.of(milk(), kefir()), cartService.findItems("buyer1").collectList().block());
    }

    @Test
    void getOrders_returnsOrders() {
        fillCart();
        long id = orderService.buy("buyer1", cartService.findItems("buyer1").collectList().block()).block();

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
        long id = orderService.buy("buyer1", cartService.findItems("buyer1").collectList().block()).block();

        webTestClient.get()
                .uri("/orders/{id}?newOrder=true", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Поздравляем"));
                    assertTrue(html.contains("Заказ №" + id));
                    for (ItemDto item : List.of(milk(), kefir())) {
                        assertTrue(html.contains("src=\"/" + item.imgPath() + "\""));
                        assertTrue(html.contains(item.title()));
                        assertTrue(html.contains(item.price() + " руб."));
                        assertTrue(html.contains(item.count() + " шт."));
                    }
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

    private void allowPayment() {
        when(paymentService.pay(195L)).thenReturn(Mono.just(new PaymentResult(PaymentStatus.SUCCESS, 805L)));
    }

    private void fillCart() {
        cartService.changeCount("buyer1", 1L, CartAction.PLUS).block();
        cartService.changeCount("buyer1", 1L, CartAction.PLUS).block();
        cartService.changeCount("buyer1", 2L, CartAction.PLUS).block();
    }

    private ItemDto milk() {
        return new ItemDto(1L, "Молоко ЛЕНТА пастеризованное", "Молоко питьевое пастеризованное 2,5%, 900 мл",
                "images/milk.jpg", 50L, 2);
    }

    private ItemDto kefir() {
        return new ItemDto(2L, "Кефир ПРОСТОКВАШИНО", "Кефир 2,5%, без змж, 930 г", "images/kefir.jpg", 95L, 1);
    }
}
