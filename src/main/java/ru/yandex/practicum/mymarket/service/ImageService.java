package ru.yandex.practicum.mymarket.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.exception.NotFoundException;

@Service
public class ImageService {

    private static final String IMAGE_NAME_PATTERN = "[a-zA-Z0-9_-]+\\.jpg";

    public Mono<byte[]> findImage(String name) {
        if (!name.matches(IMAGE_NAME_PATTERN)) {
            return Mono.error(new NotFoundException("Изображение не найдено: " + name));
        }
        ClassPathResource image = new ClassPathResource("images/" + name);
        if (!image.isReadable()) {
            return Mono.error(new NotFoundException("Изображение не найдено: " + name));
        }
        return Mono.fromCallable(image::getContentAsByteArray);
    }
}
