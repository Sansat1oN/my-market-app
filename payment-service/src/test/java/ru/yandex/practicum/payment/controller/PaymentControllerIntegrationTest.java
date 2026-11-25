package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.payment.AbstractTest;
import ru.yandex.practicum.payment.domain.Balance;
import ru.yandex.practicum.payment.domain.PaymentRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentControllerIntegrationTest extends AbstractTest {

    @Test
    void createPayment_subtractsAmountOnlyFromBuyersBalance() {
        long before = currentBalance("buyer1");
        long otherBalance = currentBalance("buyer2");

        webTestClient.post()
                .uri("/payments?username=buyer1")
                .bodyValue(new PaymentRequest(100L))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(before - 100);

        assertEquals(before - 100, currentBalance("buyer1"));
        assertEquals(otherBalance, currentBalance("buyer2"));
    }

    @Test
    void createPayment_moreThanBalance_returnsConflictAndKeepsBalance() {
        long before = currentBalance("buyer1");

        webTestClient.post()
                .uri("/payments?username=buyer1")
                .bodyValue(new PaymentRequest(before + 1))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> assertTrue(((String) message).startsWith("Недостаточно средств")));

        assertEquals(before, currentBalance("buyer1"));
    }

    private long currentBalance(String username) {
        return webTestClient.get()
                .uri("/balance?username={username}", username)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .returnResult()
                .getResponseBody()
                .getBalance();
    }
}
