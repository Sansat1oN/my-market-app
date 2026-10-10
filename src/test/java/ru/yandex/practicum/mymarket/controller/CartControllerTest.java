package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_returnsItemsAndTotal() throws Exception {
        List<ItemDto> items = List.of(item(1L, 2), item(2L, 1));
        when(cartService.findItems()).thenReturn(items);
        when(cartService.calculateTotal(items)).thenReturn(300L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("total", 300L));
    }

    @Test
    void getCart_emptyCart_returnsEmptyListAndZeroTotal() throws Exception {
        when(cartService.findItems()).thenReturn(List.of());
        when(cartService.calculateTotal(List.of())).thenReturn(0L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }

    @Test
    void changeCountOnCart_returnsCartWithNewCount() throws Exception {
        List<ItemDto> items = List.of(item(1L, 3));
        when(cartService.findItems()).thenReturn(items);
        when(cartService.calculateTotal(items)).thenReturn(300L);

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attribute("total", 300L));

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    @Test
    void changeCountOnCart_delete_callsServiceWithDelete() throws Exception {
        when(cartService.findItems()).thenReturn(List.of());

        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).changeCount(1L, CartAction.DELETE);
    }

    private ItemDto item(long id, int count) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, count);
    }
}
