package ru.yandex.practicum.mymarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemsForm(long id, String search, SortType sort, @Positive Integer pageNumber, @Positive Integer pageSize, @NotNull CartAction action) {

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
