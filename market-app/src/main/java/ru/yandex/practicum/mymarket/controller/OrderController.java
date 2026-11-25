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
import ru.yandex.practicum.mymarket.service.CheckoutService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.security.Principal;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;

    public OrderController(OrderService orderService, CheckoutService checkoutService) {
        this.orderService = orderService;
        this.checkoutService = checkoutService;
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
    public Mono<String> buy(Principal principal) {
        return checkoutService.buy(principal.getName())
                .map(result -> {
                    if (result.status() == PaymentStatus.SUCCESS) {
                        return "redirect:/orders/" + result.orderId() + "?newOrder=true";
                    }
                    return "redirect:/cart/items?paymentStatus=" + result.status();
                })
                .onErrorResume(EmptyCartException.class, e -> Mono.just("redirect:/cart/items"));
    }
}
