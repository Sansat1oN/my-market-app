package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.exception.NotFoundException;
import ru.yandex.practicum.mymarket.service.ImageService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

    @Test
    void getImage_returnsImage() throws Exception {
        byte[] bytes = {1, 2, 3};
        when(imageService.findImage("milk.jpg")).thenReturn(bytes);

        mockMvc.perform(get("/images/{name}", "milk.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void getImage_imageNotFound_404() throws Exception {
        when(imageService.findImage("ololo.jpg")).thenThrow(new NotFoundException("Изображение не найдено: ololo.jpg"));

        mockMvc.perform(get("/images/{name}", "ololo.jpg"))
                .andExpect(status().isNotFound());
    }
}
