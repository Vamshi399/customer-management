package com.example.customermanagement.dto;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    private static final Logger log = LoggerFactory.getLogger(ErrorResponse.class);

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors; // Specific for validation errors

    // Constructor for general errors
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        log.debug("Created ErrorResponse (general): status={}, error={}, message={}, path={}", status, error, message, path);
    }

    // Constructor for validation errors
    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(status, error, message, path);
        this.validationErrors = validationErrors;
        log.debug("Added validation errors to ErrorResponse: {}", validationErrors);
    }

    public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(this.status);
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
