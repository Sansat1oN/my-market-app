package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public String getCart(Model model) {
        addCartToModel(model);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String changeCountOnCart(@RequestParam(name = "id") long id,
                                    @RequestParam(name = "action") CartAction action,
                                    Model model) {
        cartService.changeCount(id, action);
        addCartToModel(model);
        return "cart";
    }

    private void addCartToModel(Model model) {
        List<ItemDto> items = cartService.findItems();
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.calculateTotal(items));
    }
}
