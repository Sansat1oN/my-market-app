package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.mymarket.model.Customer;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {
}
