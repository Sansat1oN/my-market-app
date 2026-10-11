package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class CartControllerIntegrationTest extends AbstractTest {

    @Autowired
    private CartService cartService;

    @Test
    void getCart_emptyCart_returnsEmptyListAndZeroTotal() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void getCart_returnsItemsAndTotal() throws Exception {
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(2L, CartAction.PLUS);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(milk(2), kefir(1))))
                .andExpect(model().attribute("total", 195L));
    }

    @Test
    void changeCountOnCart_plus_increasesCount() throws Exception {
        cartService.changeCount(1L, CartAction.PLUS);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(milk(2))))
                .andExpect(model().attribute("total", 100L));
    }

    @Test
    void changeCountOnCart_minusLastItem_removesItemFromCart() throws Exception {
        cartService.changeCount(1L, CartAction.PLUS);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void changeCountOnCart_delete_removesItemFromCart() throws Exception {
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(1L, CartAction.PLUS);
        cartService.changeCount(2L, CartAction.PLUS);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of(kefir(1))))
                .andExpect(model().attribute("total", 95L));
    }

    private ItemDto milk(int count) {
        return new ItemDto(1L, "Молоко ЛЕНТА пастеризованное", "Молоко питьевое пастеризованное 2,5%, 900 мл",
                "images/milk.jpg", 50L, count);
    }

    private ItemDto kefir(int count) {
        return new ItemDto(2L, "Кефир ПРОСТОКВАШИНО", "Кефир 2,5%, без змж, 930 г", "images/kefir.jpg", 95L, count);
    }
}
