package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.practicum.mymarket.AbstractTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemControllerIntegrationTest extends AbstractTest {

    @Test
    void getItems_withoutParams_returnsFirstPage() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Масса творожная РОСТАГРОЭКСПОРТ Особая"));
                    assertFalse(html.contains("Йогурт TEOS Греческий натуральный"));
                    assertTrue(html.contains("<option value=\"NO\" selected=\"selected\">"));
                    assertTrue(html.contains("Страница: 1"));
                    assertFalse(html.contains("&larr;"));
                    assertTrue(html.contains("&rarr;"));
                });
    }

    @Test
    void getItems_search_returnsFoundItems() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("search", "кефир")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Кефир ПРОСТОКВАШИНО"));
                    assertFalse(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("value=\"кефир\""));
                    assertFalse(html.contains("&rarr;"));
                });
    }

    @Test
    void getItems_sortByPrice_returnsCheapestItems() {
        webTestClient.get()
                .uri("/items?sort=PRICE&pageSize=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("Йогурт TEOS Греческий натуральный"));
                    assertFalse(html.contains("Кефир ПРОСТОКВАШИНО"));
                    assertTrue(html.contains("<option value=\"PRICE\" selected=\"selected\">"));
                    assertTrue(html.contains("&rarr;"));
                });
    }

    @Test
    void changeCountOnItems_plus_addsItemToCart() {
        webTestClient.post()
                .uri("/items")
                .body(BodyInserters.fromFormData("id", "1")
                        .with("search", "")
                        .with("sort", "NO")
                        .with("pageNumber", "1")
                        .with("pageSize", "10")
                        .with("action", "PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items?search=&sort=NO&pageNumber=1&pageSize=10");

        webTestClient.get()
                .uri("/items/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> assertTrue(html.contains("<span>1</span>")));
    }

    @Test
    void changeCountOnItem_plusAndMinus_changesCount() {
        webTestClient.post()
                .uri("/items/{id}", 1L)
                .body(BodyInserters.fromFormData("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertTrue(html.contains("Молоко ЛЕНТА пастеризованное"));
                    assertTrue(html.contains("<span>1</span>"));
                });

        webTestClient.post()
                .uri("/items/{id}", 1L)
                .body(BodyInserters.fromFormData("action", "MINUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> assertTrue(html.contains("<span>0</span>")));
    }

    @Test
    void getItem_itemNotFound_404() {
        webTestClient.get()
                .uri("/items/{id}", 100L)
                .exchange()
                .expectStatus().isNotFound();
    }
}
