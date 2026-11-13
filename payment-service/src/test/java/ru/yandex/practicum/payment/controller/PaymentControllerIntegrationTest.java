package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.payment.AbstractTest;
import ru.yandex.practicum.payment.domain.Balance;
import ru.yandex.practicum.payment.domain.PaymentRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentControllerIntegrationTest extends AbstractTest {

    @Test
    void createPayment_subtractsAmountFromBalance() {
        long before = currentBalance();

        webTestClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(100L))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(before - 100);

        assertEquals(before - 100, currentBalance());
    }

    @Test
    void createPayment_moreThanBalance_returnsConflictAndKeepsBalance() {
        long before = currentBalance();

        webTestClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(before + 1))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").value(message -> assertTrue(((String) message).startsWith("Недостаточно средств")));

        assertEquals(before, currentBalance());
    }

    private long currentBalance() {
        return webTestClient.get()
                .uri("/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .returnResult()
                .getResponseBody()
                .getBalance();
    }
}
