package com.example.customermanagement.dto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse implements org.springframework.web.ErrorResponse {

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

    @Override
    @NonNull
    public HttpStatusCode getStatusCode() {
        return HttpStatusCode.valueOf(this.status);
    }

    @Override
    @NonNull
    public ProblemDetail getBody() {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(getStatusCode(), this.message);
        problemDetail.setTitle(this.error);
        if (this.path != null && !this.path.equals("N/A")) {
            try {
                problemDetail.setInstance(URI.create(this.path));
            } catch (IllegalArgumentException e) {
                log.warn("Path '{}' could not be parsed as a URI for ProblemDetail instance. Setting as property instead.", this.path, e);
                // Handle cases where path might not be a valid URI, though typically it should be
                problemDetail.setProperty("path", this.path);
            }
        }
        problemDetail.setProperty("timestamp", this.timestamp.toString());
        if (this.validationErrors != null && !this.validationErrors.isEmpty()) {
            problemDetail.setProperty("validationErrors", this.validationErrors);
        }
        log.trace("Constructed ProblemDetail: {}", problemDetail);
        return problemDetail;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
    
    // getHeaders() is also part of the interface, but ResponseEntityExceptionHandler handles headers.
    // If direct use of ErrorResponse as return type (not in ResponseEntity) is done,
    // getHeaders() might be more relevant. For now, default (empty) is fine.
     @Override
     @NonNull
    public HttpHeaders getHeaders() {
        // Return specific headers if needed, otherwise, HttpHeaders.EMPTY is fine.
        // When used within ResponseEntity, the ResponseEntity's headers take precedence.
        return HttpHeaders.EMPTY;
    }
}
