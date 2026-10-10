package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageServiceTest {

    private final ImageService imageService = new ImageService();

    @Test
    void findImage_returnsImageBytes() {
        byte[] image = imageService.findImage("milk.jpg");

        assertTrue(image.length > 0);
    }

    @Test
    void findImage_imageNotFound_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () -> imageService.findImage("ololo.jpg"));
    }
}
