package com.example.customermanagement.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.customermanagement.dto.CustomerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Ensures clean state for each test
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    private CustomerRequest validCustomerRequest;

    @BeforeEach
    void setUp() {
        validCustomerRequest = new CustomerRequest();
        validCustomerRequest.setName("Valid User");
        validCustomerRequest.setEmail("valid@example.com");
        validCustomerRequest.setAnnualSpend(new BigDecimal("1200.50"));
        validCustomerRequest.setLastPurchaseDate(LocalDate.of(2023, 10, 15));
    }

    private UUID createTestCustomer(CustomerRequest request) throws Exception {
        String customerJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        return UUID.fromString(com.jayway.jsonpath.JsonPath.read(responseString, "$.id"));
    }
    
    @Test
    public void whenPostCustomer_thenCreateCustomer() throws Exception {
        String customerJson = objectMapper.writeValueAsString(validCustomerRequest);
        
        mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.name").value(validCustomerRequest.getName()))
            .andExpect(jsonPath("$.email").value(validCustomerRequest.getEmail()))
            .andExpect(jsonPath("$.tier").exists()); // Tier calculation is part of the service
    }

    @Test
    public void whenPostCustomer_withInvalidData_thenBadRequest() throws Exception {
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setName(""); // Blank name
        invalidRequest.setEmail("not-an-email");

        String customerJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Validation Failed")))
            .andExpect(jsonPath("$.validationErrors", hasKey("name")))
            .andExpect(jsonPath("$.validationErrors", hasKey("email")));
    }
    
    @Test
    public void givenCustomerExists_whenGetCustomerById_thenReturnsCustomer() throws Exception {
        UUID customerId = createTestCustomer(validCustomerRequest);

        mockMvc.perform(get("/customers/" + customerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.name").value(validCustomerRequest.getName()));
    }

    @Test
    public void whenGetCustomerByNonExistentId_thenNotFound() throws Exception {
        UUID invalidId = UUID.randomUUID();
        mockMvc.perform(get("/customers/" + invalidId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    public void givenCustomerExists_whenGetCustomerByName_thenReturnsCustomer() throws Exception {
        createTestCustomer(validCustomerRequest);

        mockMvc.perform(get("/customers").param("name", validCustomerRequest.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(validCustomerRequest.getName()))
            .andExpect(jsonPath("$.email").value(validCustomerRequest.getEmail()));
    }

    @Test
    public void whenGetCustomerByNonExistentName_thenNotFound() throws Exception {
        mockMvc.perform(get("/customers").param("name", "NonExistent Name"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    public void givenCustomerExists_whenGetCustomerByEmail_thenReturnsCustomer() throws Exception {
        createTestCustomer(validCustomerRequest);

        mockMvc.perform(get("/customers").param("email", validCustomerRequest.getEmail()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(validCustomerRequest.getName()))
            .andExpect(jsonPath("$.email").value(validCustomerRequest.getEmail()));
    }

    @Test
    public void whenGetCustomerByNonExistentEmail_thenNotFound() throws Exception {
        mockMvc.perform(get("/customers").param("email", "nonexistent@example.com"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    public void givenCustomerExists_whenUpdateCustomer_thenCustomerUpdated() throws Exception {
        UUID customerId = createTestCustomer(validCustomerRequest);

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setName("Updated User");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAnnualSpend(new BigDecimal("2000.00"));
        updateRequest.setLastPurchaseDate(LocalDate.of(2023, 12, 1));

        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/customers/" + customerId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerId.toString()))
            .andExpect(jsonPath("$.name").value("Updated User"))
            .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    public void whenUpdateNonExistentCustomer_thenNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String updateJson = objectMapper.writeValueAsString(validCustomerRequest);

        mockMvc.perform(put("/customers/" + nonExistentId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }

    @Test
    public void givenCustomerExists_whenUpdateCustomer_withInvalidData_thenBadRequest() throws Exception {
        UUID customerId = createTestCustomer(validCustomerRequest);

        CustomerRequest invalidUpdateRequest = new CustomerRequest();
        invalidUpdateRequest.setName(""); // Invalid
        invalidUpdateRequest.setEmail("stillvalid@example.com");

        String updateJson = objectMapper.writeValueAsString(invalidUpdateRequest);

        mockMvc.perform(put("/customers/" + customerId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Validation Failed")))
            .andExpect(jsonPath("$.validationErrors", hasKey("name")));
    }

    @Test
    public void givenCustomerExists_whenDeleteCustomer_thenNoContent() throws Exception {
        UUID customerId = createTestCustomer(validCustomerRequest);

        mockMvc.perform(delete("/customers/" + customerId))
            .andExpect(status().isNoContent());

        // Optionally, verify it's actually deleted
        mockMvc.perform(get("/customers/" + customerId))
            .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteNonExistentCustomer_thenNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/customers/" + nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Resource Not Found")));
    }
}