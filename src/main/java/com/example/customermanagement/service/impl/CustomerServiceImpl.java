package com.example.customermanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.customermanagement.dto.CustomerRequest;
import com.example.customermanagement.dto.CustomerResponse;
import com.example.customermanagement.enums.Tier;
import com.example.customermanagement.exception.ResourceNotFoundException;
import com.example.customermanagement.model.Customer;
import com.example.customermanagement.repository.CustomerRepository;
import com.example.customermanagement.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    // Tier calculation constants
    private static final BigDecimal PLATINUM_SPEND_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal GOLD_SPEND_THRESHOLD = new BigDecimal("1000");
    private static final int PLATINUM_RECENCY_MONTHS = 6;
    private static final int GOLD_RECENCY_MONTHS = 12;

    public CustomerServiceImpl(CustomerRepository customerRepository, ModelMapper modelMapper) {
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Tier calculateTier(BigDecimal annualSpend, LocalDate lastPurchaseDate) {
        log.debug("Calculating tier for annualSpend: {} and lastPurchaseDate: {}", annualSpend, lastPurchaseDate);
        if (annualSpend == null || lastPurchaseDate == null) {
            log.info("Annual spend or last purchase date is null, defaulting to SILVER tier.");
            return Tier.SILVER; // Default tier for incomplete data
        }

        LocalDate currentDate = LocalDate.now();
        Tier calculatedTier = Tier.SILVER; // Default to SILVER

        // Platinum Tier Check
        if (annualSpend.compareTo(PLATINUM_SPEND_THRESHOLD) >= 0) {
            LocalDate sixMonthsAgo = currentDate.minusMonths(PLATINUM_RECENCY_MONTHS);
            if (!lastPurchaseDate.isBefore(sixMonthsAgo)) {
                calculatedTier = Tier.PLATINUM;
            }
        }

        // Gold Tier Check
        // Only check for Gold if not already Platinum
        if (calculatedTier == Tier.SILVER && annualSpend.compareTo(GOLD_SPEND_THRESHOLD) >= 0) {
            LocalDate twelveMonthsAgo = currentDate.minusMonths(GOLD_RECENCY_MONTHS);
            if (!lastPurchaseDate.isBefore(twelveMonthsAgo)) {
                calculatedTier = Tier.GOLD;
            }
        }
        log.info("Calculated tier: {} for annualSpend: {}, lastPurchaseDate: {}", calculatedTier, annualSpend, lastPurchaseDate);
        return calculatedTier;
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest customerRequest) {
        log.info("Attempting to create customer with request: {}", customerRequest);
        Customer customer = modelMapper.map(customerRequest, Customer.class);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return mapToCustomerResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        log.info("Attempting to retrieve customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ID: {}", id);
                    return new ResourceNotFoundException("Customer not found with id: " + id);
                });
        log.info("Customer found with ID: {}", id);
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByName(String name) {
        log.info("Attempting to retrieve customer by name: {}", name);
        Customer customer = customerRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Customer not found with name: {}", name);
                    return new ResourceNotFoundException("Customer not found with name: " + name);
                });
        log.info("Customer found with name: {}", name);
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        log.info("Attempting to retrieve customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Customer not found with email: {}", email);
                    return new ResourceNotFoundException("Customer not found with email: " + email);
                });
        log.info("Customer found with email: {}", email);
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomer(UUID id, CustomerRequest customerRequest) {
        log.info("Attempting to update customer with ID: {}. Request data: {}", id, customerRequest);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found for update with ID: {}", id);
                    return new ResourceNotFoundException("Customer not found with id: " + id);
                });

        modelMapper.map(customerRequest, existingCustomer); // Update existing entity
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer with ID: {} updated successfully.", updatedCustomer.getId());
        return mapToCustomerResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(UUID id) {
        log.info("Attempting to delete customer with ID: {}", id);
        if (!customerRepository.existsById(id)) {
            log.warn("Customer not found for deletion with ID: {}", id);
            throw new ResourceNotFoundException("Customer not found with id: " + id + " for deletion.");
        }
        customerRepository.deleteById(id);
        log.info("Customer with ID: {} deleted successfully.", id);
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        log.debug("Mapping Customer entity (ID: {}) to CustomerResponse.", customer.getId());
        CustomerResponse response = modelMapper.map(customer, CustomerResponse.class);
        Tier tier = calculateTier(customer.getAnnualSpend(), customer.getLastPurchaseDate());
        response.setTier(tier);
        log.debug("Mapped CustomerResponse: Tier set to {} for customer ID: {}", tier, customer.getId());
        return response;
    }
}
