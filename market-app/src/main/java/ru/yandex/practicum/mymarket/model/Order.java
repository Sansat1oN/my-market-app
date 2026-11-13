package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
public class Order {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }
}
