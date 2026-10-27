package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void findItems_returnsCartItemsWithCount() {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(1L);
        when(item.getTitle()).thenReturn("Молоко");
        when(item.getDescription()).thenReturn("Описание");
        when(item.getImgPath()).thenReturn("images/milk.jpg");
        when(item.getPrice()).thenReturn(100L);
        when(cartItemRepository.findAll()).thenReturn(Flux.just(new CartItem(1L, 2)));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        List<ItemDto> items = cartService.findItems().collectList().block();

        assertEquals(List.of(new ItemDto(1L, "Молоко", "Описание", "images/milk.jpg", 100L, 2)), items);
    }

    @Test
    void findItems_emptyCart_returnsEmptyList() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        assertEquals(List.of(), cartService.findItems().collectList().block());
    }

    @Test
    void calculateTotal_sumsPriceMultipliedByCount() {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Молоко", "Описание", "images/milk.jpg", 100L, 2),
                new ItemDto(2L, "Кефир", "Описание", "images/kefir.jpg", 50L, 3));

        assertEquals(350L, cartService.calculateTotal(items));
    }

    @Test
    void calculateTotal_emptyCart_returnsZero() {
        assertEquals(0L, cartService.calculateTotal(List.of()));
    }

    @Test
    void changeCount_plusItemNotInCart_addsItemToCart() {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(1L);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        cartService.changeCount(1L, CartAction.PLUS).block();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getItemId());
        assertEquals(1, captor.getValue().getCount());
    }

    @Test
    void changeCount_plusItemInCart_increasesCount() {
        CartItem cartItem = new CartItem(1L, 2);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(Mono.just(cartItem));

        cartService.changeCount(1L, CartAction.PLUS).block();

        assertEquals(3, cartItem.getCount());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void changeCount_minus_decreasesCount() {
        CartItem cartItem = new CartItem(1L, 2);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(Mono.just(cartItem));

        cartService.changeCount(1L, CartAction.MINUS).block();

        assertEquals(1, cartItem.getCount());
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void changeCount_minusLastItem_removesItemFromCart() {
        CartItem cartItem = new CartItem(1L, 1);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        cartService.changeCount(1L, CartAction.MINUS).block();

        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void changeCount_minusItemNotInCart_doesNothing() {
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());

        cartService.changeCount(1L, CartAction.MINUS).block();

        verify(cartItemRepository, never()).save(any());
        verify(cartItemRepository, never()).delete(any());
        verify(itemRepository, never()).findById(any(Long.class));
    }

    @Test
    void changeCount_delete_removesItemFromCart() {
        CartItem cartItem = new CartItem(1L, 5);
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        cartService.changeCount(1L, CartAction.DELETE).block();

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void changeCount_plusUnknownItem_throwsNotFoundException() {
        when(cartItemRepository.findByItemId(100L)).thenReturn(Mono.empty());
        when(itemRepository.findById(100L)).thenReturn(Mono.empty());

        assertThrows(NotFoundException.class, () -> cartService.changeCount(100L, CartAction.PLUS).block());

        verify(cartItemRepository, never()).save(any());
    }
}
