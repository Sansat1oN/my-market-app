package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.AbstractTest;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Paging;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ItemControllerIntegrationTest extends AbstractTest {

    @Test
    void getItems_withoutParams_returnsFirstPage() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", new Paging(5, 1, false, true)));
    }

    @Test
    void getItems_search_returnsFoundItems() throws Exception {
        mockMvc.perform(get("/items")
                        .param("search", "кефир"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", List.of(List.of(kefir(0), stub(), stub()))))
                .andExpect(model().attribute("search", "кефир"))
                .andExpect(model().attribute("paging", new Paging(5, 1, false, false)));
    }

    @Test
    void getItems_sortByPrice_returnsCheapestItems() throws Exception {
        mockMvc.perform(get("/items")
                        .param("sort", "PRICE")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of(List.of(milk(0), yogurt(0), stub()))))
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(model().attribute("paging", new Paging(2, 1, false, true)));
    }

    @Test
    void changeCountOnItems_plus_addsItemToCart() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("pageSize", "10")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?sort=NO&pageNumber=1&pageSize=10"));

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", milk(1)));
    }

    @Test
    void changeCountOnItem_plusAndMinus_changesCount() throws Exception {
        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", milk(1)));

        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("item", milk(0)));
    }

    @Test
    void getItem_itemNotFound_404() throws Exception {
        mockMvc.perform(get("/items/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    private ItemDto milk(int count) {
        return new ItemDto(1L, "Молоко ЛЕНТА пастеризованное", "Молоко питьевое пастеризованное 2,5%, 900 мл",
                "images/milk.jpg", 50L, count);
    }

    private ItemDto kefir(int count) {
        return new ItemDto(2L, "Кефир ПРОСТОКВАШИНО", "Кефир 2,5%, без змж, 930 г", "images/kefir.jpg", 95L, count);
    }

    private ItemDto yogurt(int count) {
        return new ItemDto(6L, "Йогурт TEOS Греческий натуральный", "Йогурт греческий 2%, без змж, 250 г",
                "images/yogurt.jpg", 80L, count);
    }

    private ItemDto stub() {
        return new ItemDto(-1, null, null, null, 0, 0);
    }
}
