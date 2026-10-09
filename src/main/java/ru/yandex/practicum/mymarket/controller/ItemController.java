package ru.yandex.practicum.mymarket.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
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
    public String getItems(@RequestParam(name = "search", required = false) String search,
                           @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                           @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                           @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                           Model model) {
        Page<ItemDto> page = itemService.findItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", itemService.splitIntoRows(page.getContent()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext()));
        return "items";
    }

    @PostMapping("/items")
    public String changeCountOnItems(@RequestParam(name = "id") long id,
                                     @RequestParam(name = "search", required = false) String search,
                                     @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
                                     @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                     @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
                                     @RequestParam(name = "action") CartAction action,
                                     RedirectAttributes redirectAttributes) {
        cartService.changeCount(id, action);
        redirectAttributes.addAttribute("search", search);
        redirectAttributes.addAttribute("sort", sort);
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable(name = "id") long id, Model model) {
        model.addAttribute("item", itemService.findItem(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String changeCountOnItem(@PathVariable(name = "id") long id,
                                    @RequestParam(name = "action") CartAction action,
                                    Model model) {
        cartService.changeCount(id, action);
        model.addAttribute("item", itemService.findItem(id));
        return "item";
    }
}
