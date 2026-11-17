package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CheckoutResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;

import java.util.List;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    public CheckoutService(CartService cartService, PaymentService paymentService, OrderService orderService) {
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    public Mono<CheckoutResult> buy() {
        return cartService.findItems()
                .collectList()
                .map(List::copyOf)
                .flatMap(items -> {
                    if (items.isEmpty()) {
                        return Mono.error(new EmptyCartException("Корзина пуста"));
                    }
                    return paymentService.pay(cartService.calculateTotal(items))
                            .flatMap(payment -> {
                                if (payment.status() == PaymentStatus.SUCCESS) {
                                    return orderService.buy(items)
                                            .map(id -> new CheckoutResult(PaymentStatus.SUCCESS, id));
                                }
                                return Mono.just(new CheckoutResult(payment.status(), null));
                            });
                });
    }
}
