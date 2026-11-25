package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import ru.yandex.practicum.mymarket.dto.CheckoutResult;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.PaymentResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutService checkoutService;

    @Test
    void buy_successfulPayment_createsOrderAfterPayment() {
        List<ItemDto> items = fillCart();
        when(paymentService.pay("buyer1", 200L)).thenReturn(Mono.just(new PaymentResult(PaymentStatus.SUCCESS, 800L)));
        when(orderService.buy("buyer1", items)).thenReturn(Mono.just(5L));

        assertEquals(new CheckoutResult(PaymentStatus.SUCCESS, 5L), checkoutService.buy("buyer1").block());

        InOrder inOrder = inOrder(paymentService, orderService);
        inOrder.verify(paymentService).pay("buyer1", 200L);
        inOrder.verify(orderService).buy("buyer1", items);
    }

    @Test
    void buy_insufficientFunds_doesNotCreateOrder() {
        fillCart();
        when(paymentService.pay("buyer1", 200L)).thenReturn(Mono.just(
                new PaymentResult(PaymentStatus.INSUFFICIENT_FUNDS, null)));

        assertEquals(new CheckoutResult(PaymentStatus.INSUFFICIENT_FUNDS, null), checkoutService.buy("buyer1").block());

        verifyNoInteractions(orderService);
    }

    @Test
    void buy_paymentServiceUnavailable_doesNotCreateOrder() {
        fillCart();
        when(paymentService.pay("buyer1", 200L)).thenReturn(Mono.just(new PaymentResult(PaymentStatus.UNAVAILABLE, null)));

        assertEquals(new CheckoutResult(PaymentStatus.UNAVAILABLE, null), checkoutService.buy("buyer1").block());

        verifyNoInteractions(orderService);
    }

    @Test
    void overlappingPurchasesOfSameCartChargeOnce() {
        List<ItemDto> items = fillCart();
        Sinks.One<PaymentResult> payment = Sinks.one();
        when(paymentService.pay("buyer1", 200L)).thenReturn(payment.asMono());
        when(orderService.buy("buyer1", items)).thenReturn(Mono.just(5L));

        var first = checkoutService.buy("buyer1").toFuture();
        var second = checkoutService.buy("buyer1").toFuture();
        payment.tryEmitValue(new PaymentResult(PaymentStatus.SUCCESS, 800L));

        assertEquals(new CheckoutResult(PaymentStatus.SUCCESS, 5L), first.join());
        assertEquals(first.join(), second.join());
        verify(paymentService).pay("buyer1", 200L);
        verify(orderService).buy("buyer1", items);
    }

    @Test
    void buy_emptyCart_doesNotPayOrCreateOrder() {
        when(cartService.findItems("buyer1")).thenReturn(Flux.empty());

        assertThrows(EmptyCartException.class, () -> checkoutService.buy("buyer1").block());

        verifyNoInteractions(paymentService, orderService);
    }

    private List<ItemDto> fillCart() {
        List<ItemDto> items = List.of(new ItemDto(1L, "Товар 1", "Описание", "images/1.jpg", 100L, 2));
        when(cartService.findItems("buyer1")).thenReturn(Flux.fromIterable(items));
        when(cartService.calculateTotal(items)).thenReturn(200L);
        return items;
    }
}
