package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemCacheService itemCacheService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void findItems_withoutSearch_returnsItemsWithCartCount() {
        Item milk = item(1L, "Молоко", 50L);
        Item kefir = item(2L, "Кефир", 95L);
        when(itemCacheService.findAll()).thenReturn(Mono.just(List.of(milk, kefir)));
        when(cartItemRepository.findAll()).thenReturn(Flux.just(new CartItem(2L, 3)));

        Page<ItemDto> page = itemService.findItems(null, SortType.NO, 1, 5).block();

        assertEquals(2, page.getContent().size());
        assertEquals(new ItemDto(1L, "Молоко", "Молоко описание", "images/1.jpg", 50L, 0), page.getContent().get(0));
        assertEquals(3, page.getContent().get(1).count());
        assertFalse(page.hasNext());
    }

    @Test
    void findItems_withSearchAndSortByPrice_searchesByTitleAndDescription() {
        Item milk = item(1L, "Молоко", 50L);
        Item secondMilk = item(2L, "Молоко 2", 60L);
        Item thirdMilk = item(3L, "Молоко 3", 70L);
        when(itemCacheService.findAll()).thenReturn(Mono.just(List.of(milk, secondMilk, thirdMilk)));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        Page<ItemDto> page = itemService.findItems("мол", SortType.PRICE, 2, 2).block();

        assertEquals(1, page.getContent().size());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
    }

    @Test
    void findItems_sortByAlpha_sortsByTitle() {
        Item milk = item(1L, "Молоко", 50L);
        Item kefir = item(2L, "Кефир", 95L);
        when(itemCacheService.findAll()).thenReturn(Mono.just(List.of(milk, kefir)));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        Page<ItemDto> page = itemService.findItems("", SortType.ALPHA, 1, 5).block();

        assertEquals(List.of(2L, 1L), page.getContent().stream().map(ItemDto::id).toList());
    }

    @Test
    void findItems_firstPageOfMany_hasNext() {
        Item milk = item(1L, "Молоко", 50L);
        Item kefir = item(2L, "Кефир", 95L);
        when(itemCacheService.findAll()).thenReturn(Mono.just(List.of(milk, kefir)));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        Page<ItemDto> page = itemService.findItems(null, SortType.NO, 1, 1).block();

        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
    }

    @Test
    void findItem_returnsItemWithCartCount() {
        Item milk = item(1L, "Молоко", 50L);
        when(itemCacheService.findById(1L)).thenReturn(Mono.just(milk));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(new CartItem(1L, 2)));

        ItemDto item = itemService.findItem(1L).block();

        assertEquals(new ItemDto(1L, "Молоко", "Молоко описание", "images/1.jpg", 50L, 2), item);
    }

    @Test
    void findItem_itemNotInCart_returnsZeroCount() {
        Item milk = item(1L, "Молоко", 50L);
        when(itemCacheService.findById(1L)).thenReturn(Mono.just(milk));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());

        ItemDto item = itemService.findItem(1L).block();

        assertEquals(0, item.count());
    }

    @Test
    void findItem_itemNotFound_throwsNotFoundException() {
        when(itemCacheService.findById(100L)).thenReturn(Mono.empty());
        when(cartItemRepository.findByItemId(100L)).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> itemService.findItem(100L).block());
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
        lenient().when(item.getId()).thenReturn(id);
        lenient().when(item.getTitle()).thenReturn(title);
        lenient().when(item.getDescription()).thenReturn(title + " описание");
        lenient().when(item.getImgPath()).thenReturn("images/" + id + ".jpg");
        lenient().when(item.getPrice()).thenReturn(price);
        return item;
    }

    private ItemDto dto(long id) {
        return new ItemDto(id, "Товар " + id, "Описание", "images/" + id + ".jpg", 100L, 0);
    }
}
