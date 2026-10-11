package ru.yandex.practicum.mymarket.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class ImageService {

    private static final String IMAGE_NAME_PATTERN = "[a-zA-Z0-9_-]+\\.jpg";

    public byte[] findImage(String name) {
        if (!name.matches(IMAGE_NAME_PATTERN)) {
            throw new NotFoundException("Изображение не найдено: " + name);
        }
        ClassPathResource image = new ClassPathResource("images/" + name);
        if (!image.isReadable()) {
            throw new NotFoundException("Изображение не найдено: " + name);
        }
        try {
            return image.getContentAsByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
