package ru.yandex.practicum.mymarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.controller.CartController;
import ru.yandex.practicum.mymarket.controller.ImageController;
import ru.yandex.practicum.mymarket.controller.ItemController;
import ru.yandex.practicum.mymarket.controller.OrderController;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ImageService;
import ru.yandex.practicum.mymarket.service.ItemService;
import ru.yandex.practicum.mymarket.service.OrderService;

@WebFluxTest({ItemController.class, CartController.class, OrderController.class, ImageController.class})
public abstract class AbstractControllerMockTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected ItemService itemService;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;

    @MockitoBean
    protected ImageService imageService;
}
