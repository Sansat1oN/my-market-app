package ru.yandex.practicum.mymarket.dto;

public record ItemsForm(long id, String search, SortType sort, Integer pageNumber, Integer pageSize, CartAction action) {

    public ItemsForm {
        if (sort == null) {
            sort = SortType.NO;
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
    }
}
