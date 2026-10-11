package ru.yandex.practicum.mymarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public abstract class AbstractRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;
}
