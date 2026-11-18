package ru.yandex.practicum.mymarket.dto;

public record CheckoutResult(PaymentStatus status, Long orderId) {
}
