package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class AbstractTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DatabaseClient databaseClient;

    @BeforeEach
    protected void cleanDatabase() {
        databaseClient.sql("delete from order_items").then()
                .then(databaseClient.sql("delete from orders").then())
                .then(databaseClient.sql("delete from cart_items").then())
                .block();
    }
}
