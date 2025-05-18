package com.example.customermanagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.example.customermanagement.dto.CustomerRequest;
import com.example.customermanagement.dto.CustomerResponse;
import com.example.customermanagement.enums.Tier;

public interface CustomerService {

    Tier calculateTier(BigDecimal annualSpend, LocalDate lastPurchaseDate);

    CustomerResponse createCustomer(CustomerRequest customerRequest);
    CustomerResponse getCustomerById(UUID id);
    CustomerResponse getCustomerByName(String name);
    CustomerResponse getCustomerByEmail(String email);
    CustomerResponse updateCustomer(UUID id, CustomerRequest customerRequest);
    void deleteCustomer(UUID id);
}
