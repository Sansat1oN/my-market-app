package ru.yandex.practicum.mymarket.dto;

import jakarta.validation.constraints.NotNull;

public record CartForm(long id, @NotNull CartAction action) {
}
