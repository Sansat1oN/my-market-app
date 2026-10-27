package ru.yandex.practicum.mymarket.dto;

public record ItemsForm(long id, String search, SortType sort, int pageNumber, int pageSize, CartAction action) {
}
