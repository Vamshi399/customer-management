package com.example.customermanagement.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Import the GlobalExceptionHandler
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Define a simple DTO for testing MethodArgumentNotValidException
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestRequestDto {
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @Min(value = 1, message = "Value must be at least 1")
        private int value;
    }

    // Define a TestController to throw exceptions
    @RestController
    @Validated // For ConstraintViolationException on method parameters
    static class TestController {
        @PostMapping("/test/method-argument-not-valid")
        public ResponseEntity<String> testMethodArgumentNotValid(@Valid @RequestBody TestRequestDto dto) {
            return ResponseEntity.ok("Valid DTO");
        }

        @PostMapping("/test/http-message-not-readable")
        public ResponseEntity<String> testHttpMessageNotReadable(@RequestBody TestRequestDto dto) {
            return ResponseEntity.ok("Readable DTO");
        }

        @GetMapping("/test/resource-not-found")
        public ResponseEntity<String> testResourceNotFound() {
            throw new ResourceNotFoundException("Test resource was not found here.");
        }

        @GetMapping("/test/entity-not-found")
        public ResponseEntity<String> testEntityNotFound() {
            throw new EntityNotFoundException("Test entity could not be located.");
        }

        @GetMapping("/test/constraint-violation")
        public ResponseEntity<String> testConstraintViolation(@RequestParam @Min(value = 5, message = "ID must be at least 5") Integer id) {
            return ResponseEntity.ok("Valid ID: " + id);
        }

        @GetMapping("/test/uncaught-exception")
        public ResponseEntity<String> testUncaughtException() {
            throw new RuntimeException("A wild unexpected error appeared!");
        }
    }

    @Test
    @DisplayName("Handle MethodArgumentNotValidException")
    void handleMethodArgumentNotValid() throws Exception {
        TestRequestDto invalidDto = new TestRequestDto("", 0); // Blank name, value less than 1
        mockMvc.perform(post("/test/method-argument-not-valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.message", is("Input validation failed for one or more fields.")))
                .andExpect(jsonPath("$.path", is("/test/method-argument-not-valid")))
                .andExpect(jsonPath("$.validationErrors", hasKey("name")))
                .andExpect(jsonPath("$.validationErrors", hasKey("value")))
                .andExpect(jsonPath("$.validationErrors.name", containsString("Name cannot be blank"))) // Could be one of many
                .andExpect(jsonPath("$.validationErrors.value", is("Value must be at least 1")));
    }

    @Test
    @DisplayName("Handle HttpMessageNotReadableException")
    void handleHttpMessageNotReadable() throws Exception {
        String malformedJson = "{\"name\":\"Test\", \"value\":\"not_an_int\"}"; // Malformed JSON (value is not int)
        mockMvc.perform(post("/test/http-message-not-readable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Cannot deserialize value of type `int`"))) // Message can vary
                .andExpect(jsonPath("$.path", is("/test/http-message-not-readable")));
    }

    @Test
    @DisplayName("Handle ResourceNotFoundException")
    void handleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Resource Not Found")))
                .andExpect(jsonPath("$.message", is("Test resource was not found here.")))
                .andExpect(jsonPath("$.path", is("/test/resource-not-found")));
    }

    @Test
    @DisplayName("Handle EntityNotFoundException")
    void handleEntityNotFoundException() throws Exception {
        mockMvc.perform(get("/test/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Entity Not Found")))
                .andExpect(jsonPath("$.message", is("Test entity could not be located.")))
                .andExpect(jsonPath("$.path", is("/test/entity-not-found")));
    }

    @Test
    @DisplayName("Handle ConstraintViolationException")
    void handleConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-violation").param("id", "3")) // id < 5
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Constraint Violation")))
                .andExpect(jsonPath("$.message", is("One or more constraints were violated.")))
                .andExpect(jsonPath("$.path", is("/test/constraint-violation")))
                .andExpect(jsonPath("$.validationErrors", hasKey("testConstraintViolation.id")))
                .andExpect(jsonPath("$.validationErrors['testConstraintViolation.id']", is("ID must be at least 5")));
    }

    @Test
    @DisplayName("Handle All Uncaught Exceptions")
    void handleAllUncaughtExceptions() throws Exception {
        mockMvc.perform(get("/test/uncaught-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred. Please contact support.")))
                .andExpect(jsonPath("$.path", is("/test/uncaught-exception")));
    }
}
