package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemForm;
import ru.yandex.practicum.mymarket.dto.ItemsForm;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                 @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                 @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                 Model model) {
        return itemService.findItems(search, sort, pageNumber, pageSize)
                .map(page -> {
                    model.addAttribute("items", itemService.splitIntoRows(page.getContent()));
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort.name());
                    model.addAttribute("paging", new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext()));
                    return "items";
                });
    }

    @PostMapping("/items")
    public Mono<String> changeCountOnItems(@ModelAttribute ItemsForm form) {
        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/items");
        if (form.search() != null) {
            redirect.queryParam("search", form.search());
        }
        redirect.queryParam("sort", form.sort())
                .queryParam("pageNumber", form.pageNumber())
                .queryParam("pageSize", form.pageSize());
        return cartService.changeCount(form.id(), form.action())
                .thenReturn("redirect:" + redirect.encode().build().toUriString());
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable(name = "id") long id, Model model) {
        return itemService.findItem(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeCountOnItem(@PathVariable(name = "id") long id,
                                          @ModelAttribute ItemForm form,
                                          Model model) {
        return cartService.changeCount(id, form.action())
                .then(itemService.findItem(id))
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item";
                });
    }
}
