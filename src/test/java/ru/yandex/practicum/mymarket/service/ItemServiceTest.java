package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void findItems_withoutSearch_returnsItemsWithCartCount() {
        Item milk = item(1L, "Молоко", 50L);
        Item kefir = item(2L, "Кефир", 95L);
        PageRequest pageable = PageRequest.of(0, 5, Sort.unsorted());
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(milk, kefir), pageable, 2));
        when(cartItemRepository.findAll()).thenReturn(List.of(new CartItem(kefir, 3)));

        Page<ItemDto> page = itemService.findItems(null, SortType.NO, 1, 5);

        assertEquals(2, page.getContent().size());
        assertEquals(new ItemDto(1L, "Молоко", "Молоко описание", "images/1.jpg", 50L, 0), page.getContent().get(0));
        assertEquals(3, page.getContent().get(1).count());
    }

    @Test
    void findItems_withSearchAndSortByPrice_searchesByTitleAndDescription() {
        Item milk = item(1L, "Молоко", 50L);
        PageRequest pageable = PageRequest.of(1, 2, Sort.by("price", "id"));
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("мол", "мол", pageable))
                .thenReturn(new PageImpl<>(List.of(milk), pageable, 3));
        when(cartItemRepository.findAll()).thenReturn(List.of());

        Page<ItemDto> page = itemService.findItems("мол", SortType.PRICE, 2, 2);

        assertEquals(1, page.getContent().size());
        assertTrue(page.hasPrevious());
    }

    @Test
    void findItems_sortByAlpha_sortsByTitle() {
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("title", "id"));
        when(itemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));
        when(cartItemRepository.findAll()).thenReturn(List.of());

        Page<ItemDto> page = itemService.findItems("", SortType.ALPHA, 1, 5);

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findItem_returnsItemWithCartCount() {
        Item milk = item(1L, "Молоко", 50L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(milk));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.of(new CartItem(milk, 2)));

        ItemDto item = itemService.findItem(1L);

        assertEquals(new ItemDto(1L, "Молоко", "Молоко описание", "images/1.jpg", 50L, 2), item);
    }

    @Test
    void findItem_itemNotFound_throwsNotFoundException() {
        when(itemRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findItem(100L));
    }

    @Test
    void splitIntoRows_fillsLastRowWithStubs() {
        List<ItemDto> items = List.of(dto(1L), dto(2L), dto(3L), dto(4L));

        List<List<ItemDto>> rows = itemService.splitIntoRows(items);

        assertEquals(2, rows.size());
        assertEquals(List.of(dto(1L), dto(2L), dto(3L)), rows.get(0));
        assertEquals(3, rows.get(1).size());
        assertEquals(4L, rows.get(1).get(0).id());
        assertEquals(-1L, rows.get(1).get(1).id());
        assertEquals(-1L, rows.get(1).get(2).id());
    }

    private Item item(long id, String title, long price) {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(id);
        when(item.getTitle()).thenReturn(title);
        when(item.getDescription()).thenReturn(title + " описание");
        when(item.getImgPath()).thenReturn("images/" + id + ".jpg");
        when(item.getPrice()).thenReturn(price);
        return item;
    }

    private ItemDto dto(long id) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, 0);
    }
}
