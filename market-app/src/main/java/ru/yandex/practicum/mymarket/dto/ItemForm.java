package ru.yandex.practicum.mymarket.dto;

import jakarta.validation.constraints.NotNull;

public record ItemForm(@NotNull CartAction action) {
}
