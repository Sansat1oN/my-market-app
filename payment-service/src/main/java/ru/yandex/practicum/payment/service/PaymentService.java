package ru.yandex.practicum.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final long initialBalance;
    private final ConcurrentHashMap<String, Long> balances = new ConcurrentHashMap<>();

    public PaymentService(@Value("${payment.balance}") long initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Mono<Long> getBalance(String username) {
        return Mono.fromSupplier(() -> balances.getOrDefault(username, initialBalance));
    }

    public Mono<Long> pay(String username, long amount) {
        return Mono.fromCallable(() -> balances.compute(username, (key, balance) -> {
            long current = balance == null ? initialBalance : balance;
            if (current < amount) {
                throw new InsufficientFundsException("Недостаточно средств: баланс " + current + ", сумма " + amount);
            }
            return current - amount;
        }));
    }
}
