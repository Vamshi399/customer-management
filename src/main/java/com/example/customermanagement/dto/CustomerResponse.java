package com.example.customermanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.example.customermanagement.enums.Tier;
import com.example.customermanagement.model.Customer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerResponse {
    private UUID id;
    private String name;
    private String email;
    private BigDecimal annualSpend;
    private LocalDate lastPurchaseDate;
    private Tier tier;
    
    public CustomerResponse(Customer customer, Tier tier) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.email = customer.getEmail();
        this.annualSpend = customer.getAnnualSpend();
        this.lastPurchaseDate = customer.getLastPurchaseDate();
        this.tier = tier;
    }
}
