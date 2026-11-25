package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.practicum.mymarket.AbstractTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractBuyerControllerTest extends AbstractTest {

    private static final Pattern CSRF_FIELD = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"");

    @Autowired
    protected WebTestClient guest;

    protected WebTestClient webTestClient;

    @BeforeEach
    void signIn() {
        WebTestClient loginForm = openLoginForm();
        var login = loginForm.post().uri("/login")
                .body(BodyInserters.fromFormData("username", "buyer1").with("password", "buyer1"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/")
                .returnResult(Void.class);
        String session = login.getResponseCookies().getFirst("SESSION").getValue();
        WebTestClient buyer = guest.mutate().defaultCookie("SESSION", session).build();
        String form = buyer.get().uri("/items/1").exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        webTestClient = buyer.mutate().defaultHeader("X-CSRF-TOKEN", csrfToken(form)).build();
    }

    protected WebTestClient openLoginForm() {
        EntityExchangeResult<String> page = guest.get().uri("/login").exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult();
        String session = page.getResponseCookies().getFirst("SESSION").getValue();
        return guest.mutate().defaultCookie("SESSION", session)
                .defaultHeader("X-CSRF-TOKEN", csrfToken(page.getResponseBody())).build();
    }

    private String csrfToken(String form) {
        Matcher field = CSRF_FIELD.matcher(form);
        assertTrue(field.find(), "В форме должен быть CSRF-токен");
        return field.group(1);
    }
}
