package ru.yandex.practicum.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.PaymentsApi;
import ru.yandex.practicum.payment.domain.Balance;
import ru.yandex.practicum.payment.domain.ErrorResponse;
import ru.yandex.practicum.payment.domain.PaymentRequest;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;
import ru.yandex.practicum.payment.service.PaymentService;

@RestController
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<Balance>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(balance -> ResponseEntity.ok(new Balance(balance)));
    }

    @Override
    public Mono<ResponseEntity<Balance>> createPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
                .flatMap(request -> paymentService.pay(request.getAmount()))
                .map(balance -> ResponseEntity.ok(new Balance(balance)));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }
}
