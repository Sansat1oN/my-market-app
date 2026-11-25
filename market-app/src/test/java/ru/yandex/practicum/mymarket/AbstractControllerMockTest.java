package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.config.SecurityConfiguration;
import ru.yandex.practicum.mymarket.controller.CartController;
import ru.yandex.practicum.mymarket.controller.ImageController;
import ru.yandex.practicum.mymarket.controller.ItemController;
import ru.yandex.practicum.mymarket.controller.OrderController;
import ru.yandex.practicum.mymarket.repository.CustomerRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.CheckoutService;
import ru.yandex.practicum.mymarket.service.ImageService;
import ru.yandex.practicum.mymarket.service.ItemService;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.PaymentService;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest({ItemController.class, CartController.class, OrderController.class, ImageController.class})
@Import(SecurityConfiguration.class)
public abstract class AbstractControllerMockTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    private CustomerRepository customerRepository;

    @BeforeEach
    void signIn() {
        webTestClient = webTestClient.mutateWith(mockUser("buyer1")).mutateWith(csrf());
    }

    @MockitoBean
    protected ItemService itemService;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;

    @MockitoBean
    protected CheckoutService checkoutService;

    @MockitoBean
    protected PaymentService paymentService;

    @MockitoBean
    protected ImageService imageService;
}
