package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractControllerMockTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemControllerTest extends AbstractControllerMockTest {

    @Test
    void getItems_withoutParams_usesDefaults() {
        List<ItemDto> items = List.of(item(1L, 0));
        when(itemService.findItems(null, SortType.NO, 1, 5))
                .thenReturn(Mono.just(new PageImpl<>(items, PageRequest.of(0, 5), 1)));
        when(itemService.splitIntoRows(items)).thenReturn(List.of(List.of(item(1L, 0), stub(), stub())));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Товар 1"));
                    assertTrue(html.contains("<option value=\"NO\" selected=\"selected\">"));
                    assertTrue(html.contains("<option value=\"5\" selected=\"selected\">"));
                    assertTrue(html.contains("Страница: 1"));
                    assertFalse(html.contains("&larr;"));
                    assertFalse(html.contains("&rarr;"));
                });
    }

    @Test
    void getItems_rootWithParams_returnsRequestedPage() {
        List<ItemDto> items = List.of(item(3L, 0), item(4L, 2));
        when(itemService.findItems("мол", SortType.PRICE, 2, 2))
                .thenReturn(Mono.just(new PageImpl<>(items, PageRequest.of(1, 2), 7)));
        when(itemService.splitIntoRows(items)).thenReturn(List.of(List.of(item(3L, 0), item(4L, 2), stub())));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/")
                        .queryParam("search", "мол")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "2")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Товар 3"));
                    assertTrue(html.contains("Товар 4"));
                    assertTrue(html.contains("value=\"мол\""));
                    assertTrue(html.contains("<option value=\"PRICE\" selected=\"selected\">"));
                    assertTrue(html.contains("Страница: 2"));
                    assertTrue(html.contains("&larr;"));
                    assertTrue(html.contains("&rarr;"));
                });
    }

    @Test
    void changeCountOnItems_redirectsWithParams() {
        when(cartService.changeCount(1L, CartAction.PLUS)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("search", "milk")
                        .with("sort", "ALPHA")
                        .with("pageNumber", "2")
                        .with("pageSize", "10")
                        .with("action", "PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items?search=milk&sort=ALPHA&pageNumber=2&pageSize=10");

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    @Test
    void changeCountOnItems_withoutSearch_redirectsWithoutSearch() {
        when(cartService.changeCount(1L, CartAction.MINUS)).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("sort", "NO")
                        .with("pageNumber", "1")
                        .with("pageSize", "5")
                        .with("action", "MINUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items?sort=NO&pageNumber=1&pageSize=5");

        verify(cartService).changeCount(1L, CartAction.MINUS);
    }

    @Test
    void getItem_returnsItem() {
        when(itemService.findItem(1L)).thenReturn(Mono.just(item(1L, 2)));

        webTestClient.get()
                .uri("/items/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Товар 1"));
                    assertTrue(html.contains("<span>2</span>"));
                });
    }

    @Test
    void getItem_itemNotFound_404() {
        when(itemService.findItem(100L)).thenReturn(Mono.error(new NotFoundException("Товар не найден: 100")));

        webTestClient.get()
                .uri("/items/{id}", 100L)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void changeCountOnItem_returnsItemWithNewCount() {
        when(cartService.changeCount(1L, CartAction.PLUS)).thenReturn(Mono.empty());
        when(itemService.findItem(1L)).thenReturn(Mono.just(item(1L, 3)));

        webTestClient.post()
                .uri("/items/{id}", 1L)
                .body(BodyInserters.fromFormData("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Товар 1"));
                    assertTrue(html.contains("<span>3</span>"));
                });

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    private ItemDto item(long id, int count) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, count);
    }

    private ItemDto stub() {
        return new ItemDto(-1, null, null, null, 0, 0);
    }
}
