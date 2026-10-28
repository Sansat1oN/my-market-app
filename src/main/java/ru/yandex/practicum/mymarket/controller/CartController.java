package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartForm;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return showCart(model);
    }

    @PostMapping("/cart/items")
    public Mono<String> changeCountOnCart(@ModelAttribute CartForm form, Model model) {
        return cartService.changeCount(form.id(), form.action())
                .then(showCart(model));
    }

    private Mono<String> showCart(Model model) {
        return cartService.findItems()
                .collectList()
                .map(items -> {
                    model.addAttribute("items", items);
                    model.addAttribute("total", cartService.calculateTotal(items));
                    return "cart";
                });
    }
}
