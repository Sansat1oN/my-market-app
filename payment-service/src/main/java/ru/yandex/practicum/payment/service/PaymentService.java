package ru.yandex.practicum.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {

    private final AtomicLong balance;

    public PaymentService(@Value("${payment.balance}") long balance) {
        this.balance = new AtomicLong(balance);
    }

    public Mono<Long> getBalance() {
        return Mono.fromSupplier(balance::get);
    }

    public Mono<Long> pay(long amount) {
        return Mono.fromCallable(() -> balance.updateAndGet(current -> {
            if (current < amount) {
                throw new InsufficientFundsException("Недостаточно средств: баланс " + current + ", сумма " + amount);
            }
            return current - amount;
        }));
    }
}
