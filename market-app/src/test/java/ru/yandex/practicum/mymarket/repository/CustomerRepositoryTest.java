package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.AbstractRepositoryTest;
import ru.yandex.practicum.mymarket.model.Customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @ParameterizedTest
    @ValueSource(strings = {"buyer1", "buyer2"})
    void findById_returnsCustomerLoadedFromDataSql(String username) {
        Customer customer = customerRepository.findById(username).block();

        assertNotNull(customer);
        assertEquals(username, customer.getUsername());
        assertNotNull(customer.getPasswordHash());
    }

    @Test
    void findById_returnsEmptyForUnknownCustomer() {
        assertTrue(customerRepository.findById("unknown").blockOptional().isEmpty());
    }
}
