package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.service.CartService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartControllerIntegrationTest extends AbstractTest {

    @Autowired
    private CartService cartService;

    @Test
    void getCart_emptyCart_returnsPageWithoutItemsAndTotal() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("card-title"));
                    assertFalse(html.contains("Итого"));
                });
    }

    @Test
    void getCart_returnsItemsAndTotal() {
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(2L, CartAction.PLUS).block();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Кефир ПРОСТОКВАШИНО"));
                    assertTrue(html.contains("<span>2</span>"));
                    assertTrue(html.contains("<span>1</span>"));
                    assertTrue(html.contains("Итого: 195 руб."));
                });
    }

    @Test
    void changeCountOnCart_plus_increasesCount() {
        cartService.changeCount(1L, CartAction.PLUS).block();

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<span>2</span>"));
                    assertTrue(html.contains("Итого: 100 руб."));
                });
    }

    @Test
    void changeCountOnCart_minusLastItem_removesItemFromCart() {
        cartService.changeCount(1L, CartAction.PLUS).block();

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "MINUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertFalse(html.contains("Итого"));
                });
    }

    @Test
    void changeCountOnCart_delete_removesItemFromCart() {
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(1L, CartAction.PLUS).block();
        cartService.changeCount(2L, CartAction.PLUS).block();

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "DELETE"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertFalse(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Кефир ПРОСТОКВАШИНО"));
                    assertTrue(html.contains("Итого: 95 руб."));
                });
    }
}
