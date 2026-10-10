package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class OrderControllerIntegrationTest extends AbstractTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Test
    void buy_createsOrderAndClearsCart() throws Exception {
        fillCart();

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection());

        List<OrderDto> orders = orderService.findOrders();
        assertEquals(1, orders.size());
        assertEquals(List.of(milk(), kefir()), orders.get(0).items());
        assertEquals(195L, orders.get(0).totalSum());
        assertEquals(List.of(), cartService.findItems());
    }

    @Test
    void buy_redirectsToNewOrder() throws Exception {
        fillCart();

        String url = mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        long id = orderService.findOrders().get(0).id();
        assertEquals("/orders/" + id + "?newOrder=true", url);
    }

    @Test
    void getOrders_returnsOrders() throws Exception {
        fillCart();
        long id = orderService.buy();

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(new OrderDto(id, List.of(milk(), kefir()), 195L))));
    }

    @Test
    void getOrder_newOrder_returnsOrderAndTrue() throws Exception {
        fillCart();
        long id = orderService.buy();

        mockMvc.perform(get("/orders/{id}", id)
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", new OrderDto(id, List.of(milk(), kefir()), 195L)))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void getOrder_orderNotFound_404() throws Exception {
        mockMvc.perform(get("/orders/{id}", 100000L))
                .andExpect(status().isNotFound());
    }

    private void fillCart() {
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(2L, CartAction.PLUS);
    }

    private ItemDto milk() {
        return new ItemDto(1L, "Молоко ЛЕНТА пастеризованное", "Молоко питьевое пастеризованное 2,5%, 900 мл",
                "images/milk.jpg", 50L, 2);
    }

    private ItemDto kefir() {
        return new ItemDto(2L, "Кефир ПРОСТОКВАШИНО", "Кефир 2,5%, без змж, 930 г", "images/kefir.jpg", 95L, 1);
    }
}
