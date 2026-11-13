package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.AbstractControllerMockTest;
import ru.yandex.practicum.payment.domain.PaymentRequest;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest extends AbstractControllerMockTest {

    @Test
    void getBalance_returnsBalance() {
        when(paymentService.getBalance()).thenReturn(Mono.just(1000L));

        webTestClient.get()
                .uri("/balance")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.balance").isEqualTo(1000);
    }

    @Test
    void createPayment_returnsRestOfBalance() {
        when(paymentService.pay(300L)).thenReturn(Mono.just(700L));

        webTestClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(300L))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.balance").isEqualTo(700);
    }

    @Test
    void createPayment_insufficientFunds_returnsConflict() {
        when(paymentService.pay(800L)).thenReturn(Mono.error(new InsufficientFundsException("Недостаточно средств")));

        webTestClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(800L))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Недостаточно средств");
    }

    @Test
    void createPayment_withoutAmount_returnsBadRequest() {
        webTestClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest();

        verify(paymentService, never()).pay(anyLong());
    }

    @Test
    void createPayment_zeroAmount_returnsBadRequest() {
        webTestClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(0L))
                .exchange()
                .expectStatus().isBadRequest();

        verify(paymentService, never()).pay(anyLong());
    }
}
