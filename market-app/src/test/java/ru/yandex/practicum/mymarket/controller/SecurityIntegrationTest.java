package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.reactive.function.BodyInserters;

class SecurityIntegrationTest extends AbstractBuyerControllerTest {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/items", "/items/1", "/images/milk.jpg"})
    void guestCanViewCatalog(String path) {
        guest.get().uri(path).exchange().expectStatus().isOk();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/cart/items", "/orders", "/orders/1"})
    void privatePageRedirectsGuestToLogin(String path) {
        guest.get().uri(path).exchange().expectStatus().is3xxRedirection().expectHeader().location("/login");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/items", "/items/1", "/cart/items", "/buy"})
    void privateActionRequiresLoginEvenWithCsrfToken(String path) {
        openLoginForm().post().uri(path).exchange()
                .expectStatus().is3xxRedirection().expectHeader().location("/login");
    }

    @Test
    void wrongPasswordIsRejected() {
        openLoginForm().post().uri("/login")
                .body(BodyInserters.fromFormData("username", "buyer1").with("password", "wrong"))
                .exchange().expectStatus().is3xxRedirection().expectHeader().location("/login?error");
    }

    @Test
    void buyerCannotBuyWithoutCsrfToken() {
        webTestClient.mutate().defaultHeaders(headers -> headers.remove("X-CSRF-TOKEN")).build()
                .post().uri("/buy").exchange().expectStatus().isForbidden();
    }

    @Test
    void logoutClosesAccessToOrders() {
        webTestClient.get().uri("/orders").exchange().expectStatus().isOk();
        webTestClient.post().uri("/logout").exchange().expectStatus().is3xxRedirection();
        webTestClient.get().uri("/orders").exchange()
                .expectStatus().is3xxRedirection().expectHeader().location("/login");
    }
}
