package ru.yandex.practicum.mymarket.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.PaymentResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentServiceIntegrationTest extends AbstractTest {

    private static final MockWebServer SERVER = new MockWebServer();

    static {
        try {
            SERVER.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void paymentServiceProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", () -> SERVER.url("/").toString());
    }

    @AfterAll
    static void stopServer() throws IOException {
        SERVER.shutdown();
    }

    @Test
    void getBalance_returnsBalance() throws InterruptedException {
        enqueueJson(200, "{\"balance\":1000}");

        PaymentResult result = paymentService.getBalance("buyer1").block();

        assertEquals(new PaymentResult(PaymentStatus.SUCCESS, 1000L), result);
        RecordedRequest request = SERVER.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/balance?username=buyer1", request.getPath());
    }

    @Test
    void pay_returnsRemainingBalance() throws InterruptedException {
        enqueueJson(200, "{\"balance\":900}");

        PaymentResult result = paymentService.pay("buyer2", 100L).block();

        assertEquals(new PaymentResult(PaymentStatus.SUCCESS, 900L), result);
        RecordedRequest request = SERVER.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/payments?username=buyer2", request.getPath());
        assertEquals("{\"amount\":100}", request.getBody().readUtf8());
    }

    @Test
    void pay_insufficientFunds_returnsInsufficientFunds() throws InterruptedException {
        enqueueJson(409, "{\"message\":\"Недостаточно средств\"}");

        PaymentResult result = paymentService.pay("buyer1", 100L).block();

        assertEquals(new PaymentResult(PaymentStatus.INSUFFICIENT_FUNDS, null), result);
        SERVER.takeRequest();
    }

    @Test
    void getBalance_serviceUnavailable_returnsUnavailable() throws InterruptedException {
        enqueueJson(503, "");

        PaymentResult result = paymentService.getBalance("buyer1").block();

        assertEquals(new PaymentResult(PaymentStatus.UNAVAILABLE, null), result);
        SERVER.takeRequest();
    }

    private void enqueueJson(int status, String body) {
        SERVER.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
