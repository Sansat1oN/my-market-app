package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItem {

    @Id
    private Long id;

    private Long orderId;

    private Long itemId;

    private long price;

    private int count;

    protected OrderItem() {
    }

    public OrderItem(Long orderId, Long itemId, long price, int count) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.price = price;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getItemId() {
        return itemId;
    }

    public long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}
