package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        model.addAttribute("orders", orderService.findOrders());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable(name = "id") long id,
                           @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
                           Model model) {
        model.addAttribute("order", orderService.findOrder(id));
        model.addAttribute("newOrder", newOrder);
        return "order";
    }

    @PostMapping("/buy")
    public String buy(RedirectAttributes redirectAttributes) {
        long id = orderService.buy();
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addAttribute("newOrder", true);
        return "redirect:/orders/{id}";
    }
}
