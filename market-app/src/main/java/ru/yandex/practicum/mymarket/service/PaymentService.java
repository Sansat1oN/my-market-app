package ru.yandex.practicum.mymarket.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.client.domain.Balance;
import ru.yandex.practicum.mymarket.client.domain.PaymentRequest;
import ru.yandex.practicum.mymarket.dto.PaymentResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;

@Service
public class PaymentService {

    private final PaymentsApi paymentsApi;

    public PaymentService(PaymentsApi paymentsApi) {
        this.paymentsApi = paymentsApi;
    }

    public Mono<PaymentResult> getBalance() {
        return paymentsApi.getBalance()
                .map(this::success)
                .onErrorResume(error -> Mono.just(unavailable()));
    }

    public Mono<PaymentResult> pay(long amount) {
        return paymentsApi.createPayment(new PaymentRequest().amount(amount))
                .map(this::success)
                .onErrorResume(WebClientResponseException.class, error -> {
                    if (error.getStatusCode() == HttpStatus.CONFLICT) {
                        return Mono.just(new PaymentResult(PaymentStatus.INSUFFICIENT_FUNDS, null));
                    }
                    return Mono.just(unavailable());
                })
                .onErrorResume(error -> Mono.just(unavailable()));
    }

    private PaymentResult success(Balance balance) {
        return new PaymentResult(PaymentStatus.SUCCESS, balance.getBalance());
    }

    private PaymentResult unavailable() {
        return new PaymentResult(PaymentStatus.UNAVAILABLE, null);
    }
}
