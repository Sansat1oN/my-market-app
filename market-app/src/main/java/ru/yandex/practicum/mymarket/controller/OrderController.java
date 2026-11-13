package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;
import ru.yandex.practicum.mymarket.exception.EmptyCartException;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.PaymentService;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, CartService cartService, PaymentService paymentService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.findOrders()
                .collectList()
                .map(orders -> {
                    model.addAttribute("orders", orders);
                    return "orders";
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(@PathVariable(name = "id") long id,
                                 @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                                 Model model) {
        return orderService.findOrder(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                    return "order";
                });
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return cartService.findItems()
                .collectList()
                .flatMap(items -> {
                    if (items.isEmpty()) {
                        return Mono.just("redirect:/cart/items");
                    }
                    return paymentService.pay(cartService.calculateTotal(items))
                            .flatMap(payment -> {
                                if (payment.status() == PaymentStatus.SUCCESS) {
                                    return orderService.buy()
                                            .map(id -> "redirect:/orders/" + id + "?newOrder=true");
                                }
                                return Mono.just("redirect:/cart/items");
                            });
                })
                .onErrorResume(EmptyCartException.class, e -> Mono.just("redirect:/cart/items"));
    }
}
