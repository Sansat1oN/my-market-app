package ru.yandex.practicum.mymarket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private long price;

    private int count;

    protected OrderItem() {
    }

    public OrderItem(Order order, Item item, long price, int count) {
        this.order = order;
        this.item = item;
        this.price = price;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public long getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }
}
