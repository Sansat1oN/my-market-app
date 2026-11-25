package ru.yandex.practicum.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment.controller.PaymentController;
import ru.yandex.practicum.payment.config.SecurityConfiguration;
import ru.yandex.practicum.payment.service.PaymentService;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(PaymentController.class)
@Import(SecurityConfiguration.class)
public abstract class AbstractControllerMockTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected PaymentService paymentService;

    @BeforeEach
    void authorizeMarketApp() {
        webTestClient = webTestClient.mutateWith(mockJwt().jwt(jwt -> jwt.claim("azp", "market-app")));
    }
}
