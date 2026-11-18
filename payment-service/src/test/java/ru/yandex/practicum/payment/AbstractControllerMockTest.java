package ru.yandex.practicum.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.payment.controller.PaymentController;
import ru.yandex.practicum.payment.service.PaymentService;

@WebFluxTest(PaymentController.class)
public abstract class AbstractControllerMockTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected PaymentService paymentService;
}
