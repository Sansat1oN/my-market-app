package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
public class CartItem {

    @Id
    private Long id;

    private Long itemId;

    private int count;

    protected CartItem() {
    }

    public CartItem(Long itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
