package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartForm;
import ru.yandex.practicum.mymarket.dto.PaymentStatus;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.PaymentService;

@Controller
public class CartController {

    private final CartService cartService;
    private final PaymentService paymentService;

    public CartController(CartService cartService, PaymentService paymentService) {
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(@RequestParam(name = "paymentStatus", required = false) String paymentStatus,
                                Model model) {
        if (PaymentStatus.INSUFFICIENT_FUNDS.name().equals(paymentStatus)) {
            model.addAttribute("checkoutError", "Оплата не прошла: недостаточно средств. Заказ не оформлен.");
        } else if (PaymentStatus.UNAVAILABLE.name().equals(paymentStatus)) {
            model.addAttribute("checkoutError", "Оплата не прошла: сервис платежей недоступен. Заказ не оформлен.");
        }
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
                .flatMap(items -> {
                    model.addAttribute("items", items);
                    long total = cartService.calculateTotal(items);
                    model.addAttribute("total", total);
                    if (items.isEmpty()) {
                        return Mono.just("cart");
                    }
                    return paymentService.getBalance()
                            .map(payment -> {
                                boolean canBuy = payment.status() == PaymentStatus.SUCCESS
                                        && payment.balance() >= total;
                                model.addAttribute("canBuy", canBuy);
                                if (payment.status() == PaymentStatus.UNAVAILABLE) {
                                    model.addAttribute("paymentError", "Сервис платежей недоступен");
                                } else if (!canBuy) {
                                    model.addAttribute("paymentError", "Недостаточно средств");
                                }
                                return "cart";
                            });
                });
    }
}
