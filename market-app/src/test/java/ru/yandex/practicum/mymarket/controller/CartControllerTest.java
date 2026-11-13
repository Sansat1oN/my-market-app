package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractControllerMockTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartControllerTest extends AbstractControllerMockTest {

    @Test
    void getCart_returnsItemsAndTotal() {
        List<ItemDto> items = List.of(item(1L, 2), item(2L, 1));
        when(cartService.findItems()).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotal(items)).thenReturn(300L);

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Товар 1"));
                    assertTrue(html.contains("Товар 2"));
                    assertTrue(html.contains("Итого: 300 руб."));
                });
    }

    @Test
    void getCart_emptyCart_returnsPageWithoutItemsAndTotal() {
        when(cartService.findItems()).thenReturn(Flux.empty());
        when(cartService.calculateTotal(List.of())).thenReturn(0L);

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
    void changeCountOnCart_returnsCartWithNewCount() {
        List<ItemDto> items = List.of(item(1L, 3));
        when(cartService.changeCount(1L, CartAction.PLUS)).thenReturn(Mono.empty());
        when(cartService.findItems()).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotal(items)).thenReturn(300L);

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("<span>3</span>"));
                    assertTrue(html.contains("Итого: 300 руб."));
                });

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    @Test
    void changeCountOnCart_delete_callsServiceWithDelete() {
        when(cartService.changeCount(1L, CartAction.DELETE)).thenReturn(Mono.empty());
        when(cartService.findItems()).thenReturn(Flux.empty());

        webTestClient.post()
                .uri("/cart/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "DELETE"))
                .exchange()
                .expectStatus().isOk();

        verify(cartService).changeCount(1L, CartAction.DELETE);
    }

    private ItemDto item(long id, int count) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, count);
    }
}
