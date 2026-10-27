package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;

@DataR2dbcTest(properties = "spring.r2dbc.url=r2dbc:h2:mem:///repositorydb")
public abstract class AbstractRepositoryTest {

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
