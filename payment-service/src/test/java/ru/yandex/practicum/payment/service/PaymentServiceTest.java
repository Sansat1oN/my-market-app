package ru.yandex.practicum.payment.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService(1000);

    @Test
    void getBalance_returnsInitialBalance() {
        assertEquals(1000L, paymentService.getBalance("buyer1").block());
    }

    @Test
    void pay_subtractsAmountAndReturnsRest() {
        assertEquals(700L, paymentService.pay("buyer1", 300).block());
        assertEquals(700L, paymentService.getBalance("buyer1").block());
    }

    @Test
    void pay_wholeBalance_leavesZero() {
        assertEquals(0L, paymentService.pay("buyer1", 1000).block());
    }

    @Test
    void pay_usesEachBuyersOwnFunds() {
        assertEquals(0L, paymentService.pay("buyer1", 1000).block());
        assertThrows(InsufficientFundsException.class, () -> paymentService.pay("buyer1", 1).block());

        assertEquals(900L, paymentService.pay("buyer2", 100).block());
        assertEquals(0L, paymentService.getBalance("buyer1").block());
    }

    @Test
    void pay_moreThanBalance_throwsAndKeepsBalance() {
        assertThrows(InsufficientFundsException.class, () -> paymentService.pay("buyer1", 1001).block());
        assertEquals(1000L, paymentService.getBalance("buyer1").block());
    }
}
