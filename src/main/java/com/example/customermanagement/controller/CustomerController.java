package com.example.customermanagement.controller;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.customermanagement.dto.CustomerRequest;
import com.example.customermanagement.dto.CustomerResponse;
import com.example.customermanagement.service.CustomerService; // Service interface

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create a new customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        log.info("Received request to create customer: {}", customerRequest);
        CustomerResponse createdCustomerResponse = customerService.createCustomer(customerRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCustomerResponse.getId())
                .toUri();
        log.info("Customer created successfully with ID: {}. Location: {}", createdCustomerResponse.getId(), location);
        return ResponseEntity.created(location).body(createdCustomerResponse);
    }

    @Operation(summary = "Get a customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the customer", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(description = "ID of customer to be retrieved") @PathVariable UUID id) {
        log.info("Received request to get customer by ID: {}", id);
        CustomerResponse customerResponse = customerService.getCustomerById(id);
        log.info("Returning customer with ID: {}", id);
        return ResponseEntity.ok(customerResponse);
    }

    @Operation(summary = "Get a customer by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the customer", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @GetMapping(params = "name")
    public ResponseEntity<CustomerResponse> getCustomerByName(@RequestParam String name) {
        log.info("Received request to get customer by name: {}", name);
        CustomerResponse customerResponse = customerService.getCustomerByName(name);
        log.info("Returning customer with name: {}", name);
        return ResponseEntity.ok(customerResponse);
    }

    @Operation(summary = "Get a customer by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the customer", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @GetMapping(params = "email")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@RequestParam String email) {
        log.info("Received request to get customer by email: {}", email);
        CustomerResponse customerResponse = customerService.getCustomerByEmail(email);
        log.info("Returning customer with email: {}", email);
        return ResponseEntity.ok(customerResponse);
    }

    @Operation(summary = "Update an existing customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest customerRequest) {
        log.info("Received request to update customer with ID: {}. Request body: {}", id, customerRequest);
        CustomerResponse updatedCustomerResponse = customerService.updateCustomer(id, customerRequest);
        log.info("Customer with ID: {} updated successfully.", id);
        return ResponseEntity.ok(updatedCustomerResponse);
    }

    @Operation(summary = "Delete a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(schema = @Schema(implementation = com.example.customermanagement.dto.ErrorResponse.class))) })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        log.info("Received request to delete customer with ID: {}", id);
        customerService.deleteCustomer(id);
        log.info("Customer with ID: {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }
}