package com.example.customermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.example.customermanagement.dto.CustomerRequest;
import com.example.customermanagement.dto.CustomerResponse;
import com.example.customermanagement.enums.Tier;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.model.Customer;
import com.example.customermanagement.repository.CustomerRepository;
import com.example.customermanagement.service.impl.CustomerServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        customerRequest = new CustomerRequest();
        customerRequest.setName("Test User");
        customerRequest.setEmail("test@example.com");
        customerRequest.setAnnualSpend(new BigDecimal("1000"));
        customerRequest.setLastPurchaseDate(LocalDate.now().minusMonths(1));

        customer = new Customer();
        customer.setId(customerId);
        customer.setName("Test User");
        customer.setEmail("test@example.com");
        customer.setAnnualSpend(new BigDecimal("1000"));
        customer.setLastPurchaseDate(LocalDate.now().minusMonths(1));

        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setName("Test User");
        customerResponse.setEmail("test@example.com");
        customerResponse.setTier(Tier.SILVER); // Default, will be recalculated
    }

    @Test
    @DisplayName("Calculate Tier - Platinum: High spend, recent purchase")
    public void calculateTier_whenHighSpendRecentPurchase_thenPlatinum() {
        BigDecimal annualSpend = new BigDecimal("60000");
        LocalDate lastPurchaseDate = LocalDate.now().minusMonths(5); // Within 6 months
        assertEquals(Tier.PLATINUM, customerService.calculateTier(annualSpend, lastPurchaseDate));
    }

    @Test
    @DisplayName("Calculate Tier - Gold: Medium spend, recent purchase (not Platinum eligible)")
    public void calculateTier_whenMediumSpendRecentPurchase_thenGold() {
        BigDecimal annualSpend = new BigDecimal("15000"); // Between 10k and 50k
        LocalDate lastPurchaseDate = LocalDate.now().minusMonths(11); // Within 12 months
        assertEquals(Tier.GOLD, customerService.calculateTier(annualSpend, lastPurchaseDate));
    }

    @Test
    @DisplayName("Calculate Tier - Silver: Low spend")
    public void calculateTier_whenLowSpend_thenSilver() {
        BigDecimal annualSpend = new BigDecimal("5000"); // Below 10k
        LocalDate lastPurchaseDate = LocalDate.now().minusMonths(1);
        assertEquals(Tier.SILVER, customerService.calculateTier(annualSpend, lastPurchaseDate));
    }

    @Test
    @DisplayName("Calculate Tier - Silver: High spend, old purchase (Platinum recency miss)")
    public void calculateTier_whenHighSpendOldPurchasePlatinumMiss_thenSilver() {
        BigDecimal annualSpend = new BigDecimal("60000");
        LocalDate lastPurchaseDate = LocalDate.now().minusMonths(7); // Older than 6 months
        assertEquals(Tier.SILVER, customerService.calculateTier(annualSpend, lastPurchaseDate));
    }

    @Test
    @DisplayName("Calculate Tier - Silver: Medium spend, old purchase (Gold recency miss)")
    public void calculateTier_whenMediumSpendOldPurchaseGoldMiss_thenSilver() {
        BigDecimal annualSpend = new BigDecimal("15000");
        LocalDate lastPurchaseDate = LocalDate.now().minusMonths(13); // Older than 12 months
        assertEquals(Tier.SILVER, customerService.calculateTier(annualSpend, lastPurchaseDate));
    }

    @Test
    @DisplayName("Calculate Tier - Silver: Null annual spend")
    public void calculateTier_whenNullAnnualSpend_thenSilver() {
        assertEquals(Tier.SILVER, customerService.calculateTier(null, LocalDate.now()));
    }

    @Test
    @DisplayName("Calculate Tier - Silver: Null last purchase date")
    public void calculateTier_whenNullLastPurchaseDate_thenSilver() {
        assertEquals(Tier.SILVER, customerService.calculateTier(new BigDecimal("10000"), null));
    }

    @Test
    @DisplayName("Create Customer - Success")
    void createCustomer_success() {
        when(modelMapper.map(customerRequest, Customer.class)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);
        // Tier will be calculated, let's assume it's SILVER for this spend
        customerResponse.setTier(Tier.SILVER);

        CustomerResponse result = customerService.createCustomer(customerRequest);

        assertNotNull(result);
        assertEquals(customerResponse.getId(), result.getId());
        assertEquals(Tier.SILVER, result.getTier()); // Tier is calculated
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Get Customer By ID - Success")
    void getCustomerById_success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);
        customerResponse.setTier(Tier.SILVER); // Tier is calculated

        CustomerResponse result = customerService.getCustomerById(customerId);

        assertNotNull(result);
        assertEquals(customerResponse.getId(), result.getId());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Get Customer By ID - Not Found")
    void getCustomerById_notFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(customerId));
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    @DisplayName("Get Customer By Name - Success")
    void getCustomerByName_success() {
        when(customerRepository.findByName(customer.getName())).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);
        customerResponse.setTier(Tier.SILVER);

        CustomerResponse result = customerService.getCustomerByName(customer.getName());
        assertNotNull(result);
        assertEquals(customerResponse.getName(), result.getName());
    }

    @Test
    @DisplayName("Get Customer By Name - Not Found")
    void getCustomerByName_notFound() {
        when(customerRepository.findByName("Unknown Name")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerByName("Unknown Name"));
    }

    @Test
    @DisplayName("Get Customer By Email - Success")
    void getCustomerByEmail_success() {
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);
        customerResponse.setTier(Tier.SILVER);

        CustomerResponse result = customerService.getCustomerByEmail(customer.getEmail());
        assertNotNull(result);
        assertEquals(customerResponse.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Get Customer By Email - Not Found")
    void getCustomerByEmail_notFound() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerByEmail("unknown@example.com"));
    }

    @Test
    @DisplayName("Update Customer - Success")
    void updateCustomer_success() {
        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer); // Assume save returns the updated customer
        when(modelMapper.map(customer, CustomerResponse.class)).thenReturn(customerResponse);
        // Simulate ModelMapper updating the existingCustomer instance
        doNothing().when(modelMapper).map(eq(updateRequest), eq(customer));

        customerResponse.setName("Updated Name"); // Reflect update
        customerResponse.setEmail("updated@example.com");
        customerResponse.setTier(Tier.SILVER);

        CustomerResponse result = customerService.updateCustomer(customerId, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(modelMapper, times(1)).map(updateRequest, customer);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Update Customer - Not Found")
    void updateCustomer_notFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(customerId, customerRequest));
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void deleteCustomer_success() {
        when(customerRepository.existsById(customerId)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(customerId);

        customerService.deleteCustomer(customerId);

        verify(customerRepository, times(1)).existsById(customerId);
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    @DisplayName("Delete Customer - Not Found")
    void deleteCustomer_notFound() {
        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(customerId));
        verify(customerRepository, times(1)).existsById(customerId);
        verify(customerRepository, never()).deleteById(any());
    }
}