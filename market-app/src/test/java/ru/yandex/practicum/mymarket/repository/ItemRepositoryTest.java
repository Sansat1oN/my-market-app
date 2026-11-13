package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void count_returnsItemsLoadedFromDataSql() {
        assertEquals(10L, itemRepository.count().block());
    }

    @Test
    void findById_returnsItemLoadedFromDataSql() {
        Item item = itemRepository.findById(1L).block();

        assertEquals("Молоко ЛЕНТА пастеризованное", item.getTitle());
        assertEquals("Молоко питьевое пастеризованное 2,5%, 900 мл", item.getDescription());
        assertEquals("images/milk.jpg", item.getImgPath());
        assertEquals(50L, item.getPrice());
    }

    @Test
    void search_byTitleIgnoreCase_returnsMatchingItems() {
        List<Item> items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "простоквашино", "простоквашино", PageRequest.of(0, 10)).collectList().block();

        assertEquals(3, items.size());
        assertEquals(3L, itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "простоквашино", "простоквашино").block());
    }

    @Test
    void search_byDescription_returnsMatchingItems() {
        List<Item> items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "изюм", "изюм", PageRequest.of(0, 10)).collectList().block();

        assertEquals(1, items.size());
        assertEquals("Масса творожная РОСТАГРОЭКСПОРТ Особая", items.get(0).getTitle());
    }

    @Test
    void search_returnsRequestedPage() {
        List<Item> items = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "простоквашино", "простоквашино", PageRequest.of(1, 2, Sort.by("title"))).collectList().block();

        assertEquals(1, items.size());
        assertEquals("Творог ПРОСТОКВАШИНО", items.get(0).getTitle());
    }

    @Test
    void findAllBy_sortedByPrice_returnsFirstPage() {
        List<Item> items = itemRepository.findAllBy(PageRequest.of(0, 5, Sort.by("price"))).collectList().block();

        assertEquals(5, items.size());
        assertEquals(50L, items.get(0).getPrice());
    }

    @Test
    void findAllBy_sortedByTitle_returnsLastPage() {
        List<Item> items = itemRepository.findAllBy(PageRequest.of(1, 5, Sort.by("title"))).collectList().block();

        assertEquals(5, items.size());
        assertEquals("Творог ПРОСТОКВАШИНО", items.get(4).getTitle());
    }
}
