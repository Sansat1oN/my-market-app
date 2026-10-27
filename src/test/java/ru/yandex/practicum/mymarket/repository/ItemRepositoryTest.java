package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAll_returnsItemsLoadedFromCsv() {
        assertEquals(10, itemRepository.findAll().size());
    }

    @Test
    void findById_returnsItemLoadedFromCsv() {
        Item item = itemRepository.findById(1L).orElseThrow();

        assertEquals("Молоко ЛЕНТА пастеризованное", item.getTitle());
        assertEquals("Молоко питьевое пастеризованное 2,5%, 900 мл", item.getDescription());
        assertEquals("images/milk.jpg", item.getImgPath());
        assertEquals(50L, item.getPrice());
    }

    @Test
    void search_byTitleIgnoreCase_returnsMatchingItems() {
        Page<Item> page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "простоквашино", "простоквашино", PageRequest.of(0, 10));

        assertEquals(3, page.getTotalElements());
    }

    @Test
    void search_byDescription_returnsMatchingItems() {
        Page<Item> page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "изюм", "изюм", PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Масса творожная РОСТАГРОЭКСПОРТ Особая", page.getContent().get(0).getTitle());
    }

    @Test
    void findAll_sortedByPrice_returnsFirstPage() {
        Page<Item> page = itemRepository.findAll(PageRequest.of(0, 5, Sort.by("price")));

        assertEquals(5, page.getContent().size());
        assertEquals(50L, page.getContent().get(0).getPrice());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
    }

    @Test
    void findAll_sortedByTitle_returnsLastPage() {
        Page<Item> page = itemRepository.findAll(PageRequest.of(1, 5, Sort.by("title")));

        assertEquals(5, page.getContent().size());
        assertEquals("Творог ПРОСТОКВАШИНО", page.getContent().get(4).getTitle());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
    }
}
