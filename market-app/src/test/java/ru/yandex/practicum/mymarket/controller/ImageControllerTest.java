package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.AbstractControllerMockTest;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import static org.mockito.Mockito.when;

class ImageControllerTest extends AbstractControllerMockTest {

    @Test
    void getImage_returnsImage() {
        byte[] bytes = {1, 2, 3};
        when(imageService.findImage("milk.jpg")).thenReturn(Mono.just(bytes));

        webTestClient.get()
                .uri("/images/{name}", "milk.jpg")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class)
                .isEqualTo(bytes);
    }

    @Test
    void getImage_imageNotFound_404() {
        when(imageService.findImage("ololo.jpg"))
                .thenReturn(Mono.error(new NotFoundException("Изображение не найдено: ololo.jpg")));

        webTestClient.get()
                .uri("/images/{name}", "ololo.jpg")
                .exchange()
                .expectStatus().isNotFound();
    }
}
