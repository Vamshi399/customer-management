package com.example.customermanagement.exception;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Import the GlobalExceptionHandler and the TestController
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- Test Controller to throw exceptions ---
    @RestController
    @Validated // Required for @RequestParam validation
    static class TestController {
        @PostMapping("/test/method-argument-not-valid")
        public ResponseEntity<String> testMethodArgumentNotValid(@Valid @RequestBody TestDto dto) {
            return ResponseEntity.ok("Valid DTO");
        }

        @PostMapping("/test/http-message-not-readable")
        public ResponseEntity<String> testHttpMessageNotReadable(@RequestBody TestDto dto) {
            // This endpoint itself doesn't throw it directly,
            // malformed JSON in the request body will trigger it before controller method.
            return ResponseEntity.ok("Should not reach here with malformed JSON");
        }

        @GetMapping("/test/resource-not-found")
        public ResponseEntity<String> testResourceNotFound() {
            throw new ResourceNotFoundException("Test resource was not found");
        }

        @GetMapping("/test/entity-not-found")
        public ResponseEntity<String> testEntityNotFound() {
            throw new EntityNotFoundException("Test entity was not found by JPA");
        }

        @GetMapping("/test/constraint-violation")
        public ResponseEntity<String> testConstraintViolation(@RequestParam @Min(5) Integer value) {
            return ResponseEntity.ok("Value is valid: " + value);
        }

        @GetMapping("/test/generic-exception")
        public ResponseEntity<String> testGenericException() {
            throw new RuntimeException("Some unexpected generic error");
        }

        @GetMapping("/test/missing-param")
        public ResponseEntity<String> testMissingParam(@RequestParam String requiredParam) {
            return ResponseEntity.ok("Param received: " + requiredParam);
        }
    }

    @Data
    static class TestDto {
        @NotBlank(message = "Name cannot be blank")
        private String name;
        @NotNull(message = "Value cannot be null")
        private Integer value;
    }

    // --- Tests for GlobalExceptionHandler ---

    @Test
    void handleMethodArgumentNotValidException() throws Exception {
        String invalidDtoJson = "{\"name\":\"\", \"value\":null}"; // name is blank, value is null
        mockMvc.perform(post("/test/method-argument-not-valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors", hasKey("name")))
                .andExpect(jsonPath("$.validationErrors.name", is("Name cannot be blank")))
                .andExpect(jsonPath("$.validationErrors", hasKey("value")))
                .andExpect(jsonPath("$.validationErrors.value", is("Value cannot be null")));
    }

    @Test
    void handleHttpMessageNotReadableException() throws Exception {
        String malformedJson = "{\"name\":\"Test\", \"value\":abc}"; // abc is not a valid integer
        mockMvc.perform(post("/test/http-message-not-readable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", notNullValue())); // Message can vary
    }

    @Test
    void handleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Resource Not Found")))
                .andExpect(jsonPath("$.message", is("Test resource was not found")));
    }

    @Test
    void handleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Entity Not Found"))) // Assuming this is the error string in your handler
                .andExpect(jsonPath("$.message", is("Test entity was not found by JPA")));
    }

    @Test
    void handleConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-violation").param("value", "3")) // value < 5
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.validationErrors", hasKey("testConstraintViolation.value")))
                .andExpect(jsonPath("$.validationErrors['testConstraintViolation.value']", is("must be greater than or equal to 5")));
    }

    @Test
    void handleAllUncaughtExceptions() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("Some unexpected generic error"))); // Or specific message from your handler
    }

    @Test
    void handleMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(get("/test/missing-param")) // 'requiredParam' is missing
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request"))) // Common error for this, or more specific
                .andExpect(jsonPath("$.message", containsString("Required String parameter 'requiredParam' is not present")));
    }

    @Test
    void handleHttpRequestMethodNotSupportedException() throws Exception {
        mockMvc.perform(post("/test/resource-not-found")) // Assuming /test/resource-not-found is GET only
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString("Request method 'POST' not supported")));
    }
}
