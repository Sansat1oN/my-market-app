package ru.yandex.practicum.mymarket.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.PaymentResult;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentServiceIntegrationTest extends AbstractTest {

    private static final MockWebServer SERVER = new MockWebServer();
    private static final MockWebServer KEYCLOAK = new MockWebServer();

    static {
        try {
            SERVER.start();
            KEYCLOAK.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReactiveOAuth2AuthorizedClientService authorizedClients;

    @BeforeEach
    void clearCachedToken() {
        authorizedClients.removeAuthorizedClient("market-app", "market-app").block();
    }

    @DynamicPropertySource
    static void paymentServiceProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", () -> SERVER.url("/").toString());
        registry.add("spring.security.oauth2.client.provider.market-app.token-uri", () -> KEYCLOAK.url("/token").toString());
        registry.add("spring.security.oauth2.client.registration.market-app.client-secret", () -> "test-secret");
    }

    @AfterAll
    static void stopServer() throws IOException {
        SERVER.shutdown();
        KEYCLOAK.shutdown();
    }

    @Test
    void getBalance_returnsBalance() throws InterruptedException {
        enqueueToken();
        enqueueJson(200, "{\"balance\":1000}");

        PaymentResult result = paymentService.getBalance("buyer1").block();

        assertEquals(new PaymentResult(PaymentStatus.SUCCESS, 1000L), result);
        assertClientCredentialsRequest();
        RecordedRequest request = takePaymentRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/balance?username=buyer1", request.getPath());
    }

    @Test
    void pay_returnsRemainingBalance() throws InterruptedException {
        enqueueToken();
        enqueueJson(200, "{\"balance\":900}");

        PaymentResult result = paymentService.pay("buyer2", 100L).block();

        assertEquals(new PaymentResult(PaymentStatus.SUCCESS, 900L), result);
        assertClientCredentialsRequest();
        RecordedRequest request = takePaymentRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/payments?username=buyer2", request.getPath());
        assertEquals("{\"amount\":100}", request.getBody().readUtf8());
    }

    @Test
    void pay_insufficientFunds_returnsInsufficientFunds() throws InterruptedException {
        enqueueToken();
        enqueueJson(409, "{\"message\":\"Недостаточно средств\"}");

        PaymentResult result = paymentService.pay("buyer1", 100L).block();

        assertEquals(new PaymentResult(PaymentStatus.INSUFFICIENT_FUNDS, null), result);
        assertClientCredentialsRequest();
        takePaymentRequest();
    }

    @Test
    void getBalance_serviceUnavailable_returnsUnavailable() throws InterruptedException {
        enqueueToken();
        enqueueJson(503, "");

        PaymentResult result = paymentService.getBalance("buyer1").block();

        assertEquals(new PaymentResult(PaymentStatus.UNAVAILABLE, null), result);
        assertClientCredentialsRequest();
        takePaymentRequest();
    }

    @Test
    void pay_whenTokenRequestFails_returnsUnavailableWithoutSendingPayment() throws InterruptedException {
        KEYCLOAK.enqueue(new MockResponse().setResponseCode(401)
                .setHeader("Content-Type", "application/json").setBody("{\"error\":\"invalid_client\"}"));
        int before = SERVER.getRequestCount();

        assertEquals(new PaymentResult(PaymentStatus.UNAVAILABLE, null), paymentService.pay("buyer1", 100).block());
        assertClientCredentialsRequest();
        assertEquals(before, SERVER.getRequestCount());
    }

    private void enqueueToken() {
        KEYCLOAK.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"test-token\",\"token_type\":\"Bearer\",\"expires_in\":300}"));
    }

    private void assertClientCredentialsRequest() throws InterruptedException {
        RecordedRequest request = KEYCLOAK.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/token", request.getPath());
        assertEquals(okhttp3.Credentials.basic("market-app", "test-secret"), request.getHeader("Authorization"));
        assertEquals("grant_type=client_credentials", request.getBody().readUtf8());
    }

    private RecordedRequest takePaymentRequest() throws InterruptedException {
        RecordedRequest request = SERVER.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("Bearer test-token", request.getHeader("Authorization"));
        return request;
    }

    private void enqueueJson(int status, String body) {
        SERVER.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }
}
