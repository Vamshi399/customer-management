package com.example.customermanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.customermanagement.model.Customer;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll(); // Clean up before each test

        customer1 = new Customer("John Doe", "john.doe@example.com",
            new BigDecimal("1500.75"), LocalDate.of(2023, 1, 15));

        customer2 = new Customer("Jane Smith", "jane.smith@example.com",
            new BigDecimal("250.50"), LocalDate.of(2023, 3, 10));
    }

    @Test
    public void whenSaveCustomer_thenIdIsAssignedAndCanBeFound() {
        // Given
        Customer savedCustomer = customerRepository.save(customer1);

        // Then
        assertNotNull(savedCustomer.getId());
        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());
        assertTrue(foundCustomer.isPresent());
        assertEquals(customer1.getName(), foundCustomer.get().getName());
        assertEquals(customer1.getEmail(), foundCustomer.get().getEmail());
        assertEquals(0, customer1.getAnnualSpend().compareTo(foundCustomer.get().getAnnualSpend()));
        assertEquals(customer1.getLastPurchaseDate(), foundCustomer.get().getLastPurchaseDate());
    }

    @Test
    public void whenFindById_andCustomerExists_thenReturnCustomer() {
        // Given
        Customer savedCustomer = customerRepository.save(customer1);

        // When
        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());

        // Then
        assertTrue(foundCustomer.isPresent());
        assertEquals(savedCustomer.getName(), foundCustomer.get().getName());
    }

    @Test
    public void whenFindById_andCustomerDoesNotExist_thenReturnEmpty() {
        // When
        Optional<Customer> foundCustomer = customerRepository.findById(UUID.randomUUID());

        // Then
        assertFalse(foundCustomer.isPresent());
    }

    @Test
    public void whenFindByName_andCustomerExists_thenReturnCustomer() {
        // Given
        customerRepository.save(customer1);

        // When
        Optional<Customer> foundCustomer = customerRepository.findByName("John Doe");

        // Then
        assertTrue(foundCustomer.isPresent());
        assertEquals("john.doe@example.com", foundCustomer.get().getEmail());
    }

    @Test
    public void whenFindByName_andCustomerDoesNotExist_thenReturnEmpty() {
        // When
        Optional<Customer> foundCustomer = customerRepository.findByName("Non Existent Name");

        // Then
        assertFalse(foundCustomer.isPresent());
    }

    @Test
    public void whenFindByEmail_andCustomerExists_thenReturnCustomer() {
        // Given
        customerRepository.save(customer1);

        // When
        Optional<Customer> foundCustomer = customerRepository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(foundCustomer.isPresent());
        assertEquals("John Doe", foundCustomer.get().getName());
    }

    @Test
    public void whenFindByEmail_andCustomerDoesNotExist_thenReturnEmpty() {
        // When
        Optional<Customer> foundCustomer = customerRepository.findByEmail("non.existent@example.com");

        // Then
        assertFalse(foundCustomer.isPresent());
    }

    @Test
    public void whenUpdateCustomer_thenDataIsChanged() {
        // Given
        Customer savedCustomer = customerRepository.save(customer1);
        savedCustomer.setName("Johnathan Doe");
        savedCustomer.setAnnualSpend(new BigDecimal("2000.00"));

        // When
        Customer updatedCustomer = customerRepository.save(savedCustomer);
        Optional<Customer> foundAfterUpdate = customerRepository.findById(savedCustomer.getId());

        // Then
        assertEquals("Johnathan Doe", updatedCustomer.getName());
        assertEquals(0, new BigDecimal("2000.00").compareTo(updatedCustomer.getAnnualSpend()));
        assertTrue(foundAfterUpdate.isPresent());
        assertEquals("Johnathan Doe", foundAfterUpdate.get().getName());
    }

    @Test
    public void whenDeleteCustomer_thenItCannotBeFound() {
        // Given
        Customer savedCustomer = customerRepository.save(customer1);
        UUID id = savedCustomer.getId();

        // When
        customerRepository.deleteById(id);
        Optional<Customer> foundCustomer = customerRepository.findById(id);

        // Then
        assertFalse(foundCustomer.isPresent());
    }

    @Test
    public void whenExistsById_andCustomerExists_thenReturnTrue() {
        Customer savedCustomer = customerRepository.save(customer1);
        assertTrue(customerRepository.existsById(savedCustomer.getId()));
    }

    @Test
    public void whenExistsById_andCustomerDoesNotExist_thenReturnFalse() {
        assertFalse(customerRepository.existsById(UUID.randomUUID()));
    }

    @Test
    public void whenSaveCustomerWithNullName_thenDataIntegrityViolationException() {
        Customer customerWithNullName = new Customer(null, "test@example.com", BigDecimal.TEN, LocalDate.now());
        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.saveAndFlush(customerWithNullName); // saveAndFlush to trigger validation immediately
        });
    }

    @Test
    public void whenSaveCustomerWithNullEmail_thenDataIntegrityViolationException() {
        Customer customerWithNullEmail = new Customer("Test Name", null, BigDecimal.TEN, LocalDate.now());
        assertThrows(DataIntegrityViolationException.class, () -> {
            customerRepository.saveAndFlush(customerWithNullEmail);
        });
    }
}