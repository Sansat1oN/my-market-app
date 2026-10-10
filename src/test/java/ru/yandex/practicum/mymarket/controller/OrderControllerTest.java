package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void getOrders_returnsOrders() throws Exception {
        List<OrderDto> orders = List.of(order(1L), order(2L));
        when(orderService.findOrders()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", orders));
    }

    @Test
    void getOrder_withoutNewOrder_returnsOrderAndFalse() throws Exception {
        when(orderService.findOrder(1L)).thenReturn(order(1L));

        mockMvc.perform(get("/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order(1L)))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void getOrder_newOrder_returnsOrderAndTrue() throws Exception {
        when(orderService.findOrder(1L)).thenReturn(order(1L));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order(1L)))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void getOrder_orderNotFound_404() throws Exception {
        when(orderService.findOrder(100L)).thenThrow(new NotFoundException("Заказ не найден: 100"));

        mockMvc.perform(get("/orders/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    void buy_redirectsToNewOrder() throws Exception {
        when(orderService.buy()).thenReturn(5L);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/5?newOrder=true"));
    }

    private OrderDto order(long id) {
        List<ItemDto> items = List.of(new ItemDto(1L, "Товар 1", "Описание", "images/1.jpg", 100L, 2));
        return new OrderDto(id, items, 200L);
    }
}
