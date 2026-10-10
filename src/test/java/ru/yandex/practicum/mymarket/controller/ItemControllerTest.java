package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mymarket.AbstractControllerMockTest;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ItemControllerTest extends AbstractControllerMockTest {

    @Test
    void getItems_withoutParams_usesDefaults() throws Exception {
        List<ItemDto> items = List.of(item(1L, 0));
        List<List<ItemDto>> rows = List.of(List.of(item(1L, 0), stub(), stub()));
        when(itemService.findItems(null, SortType.NO, 1, 5))
                .thenReturn(new PageImpl<>(items, PageRequest.of(0, 5), 1));
        when(itemService.splitIntoRows(items)).thenReturn(rows);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", rows))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", new Paging(5, 1, false, false)));
    }

    @Test
    void getItems_rootWithParams_returnsRequestedPage() throws Exception {
        List<ItemDto> items = List.of(item(3L, 0), item(4L, 2));
        when(itemService.findItems("мол", SortType.PRICE, 2, 2))
                .thenReturn(new PageImpl<>(items, PageRequest.of(1, 2), 7));
        when(itemService.splitIntoRows(items)).thenReturn(List.of(List.of(item(3L, 0), item(4L, 2), stub())));

        mockMvc.perform(get("/")
                        .param("search", "мол")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", "мол"))
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(model().attribute("paging", new Paging(2, 2, true, true)));
    }

    @Test
    void changeCountOnItems_redirectsWithParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "milk")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "2")
                        .param("pageSize", "10")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=milk&sort=ALPHA&pageNumber=2&pageSize=10"));

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    @Test
    void changeCountOnItems_withoutOptionalParams_redirectsWithDefaults() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?sort=NO&pageNumber=1&pageSize=5"));

        verify(cartService).changeCount(1L, CartAction.MINUS);
    }

    @Test
    void getItem_returnsItem() throws Exception {
        when(itemService.findItem(1L)).thenReturn(item(1L, 2));

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item(1L, 2)));
    }

    @Test
    void getItem_itemNotFound_404() throws Exception {
        when(itemService.findItem(100L)).thenThrow(new NotFoundException("Товар не найден: 100"));

        mockMvc.perform(get("/items/{id}", 100L))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeCountOnItem_returnsItemWithNewCount() throws Exception {
        when(itemService.findItem(1L)).thenReturn(item(1L, 3));

        mockMvc.perform(post("/items/{id}", 1L)
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item(1L, 3)));

        verify(cartService).changeCount(1L, CartAction.PLUS);
    }

    private ItemDto item(long id, int count) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, count);
    }

    private ItemDto stub() {
        return new ItemDto(-1, null, null, null, 0, 0);
    }
}
