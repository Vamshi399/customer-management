package com.example.customermanagement.exception;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Handle validation errors for @Valid annotated objects
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode httpStatus, // Renamed from status to httpStatus for clarity
                        WebRequest request) {
                
                String path = getRequestPath(request);
                log.info("Handling MethodArgumentNotValidException for path: {}. Errors: {}", path, ex.getBindingResult().getFieldErrors().size());

                // Get all validation errors
                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                fieldError -> fieldError.getDefaultMessage() != null
                                                                ? fieldError.getDefaultMessage()
                                                                : "Validation error"));

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                httpStatus.value(),
                                "Validation Failed",
                                "Input validation failed for one or more fields.",
                                path,
                                errors);

                return new ResponseEntity<>(errorResponse, headers, httpStatus);
        }

        // Handle cases when request body is malformed
        @Override
        protected ResponseEntity<Object> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex,
                        HttpHeaders headers,
                        HttpStatusCode httpStatus, // Renamed from status to httpStatus for clarity
                        WebRequest request) {
                
                String path = getRequestPath(request);
                log.info("Handling HttpMessageNotReadableException for path: {}. Message: {}", path, ex.getMessage());

                String message = "Malformed JSON request";
                if (ex.getMostSpecificCause() != null) {
                        message = ex.getMostSpecificCause().getMessage();
                }

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                httpStatus.value(),
                                "Bad Request",
                                message,
                                path);

                return new ResponseEntity<>(errorResponse, headers, httpStatus);
        }

        // Handle custom ResourceNotFoundException
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<org.springframework.web.ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {
                String path = getRequestPath(request);
                log.info("Handling ResourceNotFoundException: {} for path: {}", ex.getMessage(), path); // Added logging

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Resource Not Found",
                                ex.getMessage(),
                                path);
                return new ResponseEntity<>(errorResponse, errorResponse.getStatusCode());
        }
        
        // Handle entity not found exceptions
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<org.springframework.web.ErrorResponse> handleEntityNotFound(
                        EntityNotFoundException ex, WebRequest request) { // Added WebRequest

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Entity Not Found",
                                ex.getMessage(),
                                getRequestPath(request)); // Path already logged by ResourceNotFoundException handler if it extends it

                return new ResponseEntity<>(errorResponse, errorResponse.getStatusCode());
        }

        // Handle constraint violations (e.g., @NotNull, @Size, etc.)
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<org.springframework.web.ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex, WebRequest request) { // Added WebRequest
                
                String path = getRequestPath(request);
                log.info("Handling ConstraintViolationException for path: {}. Violations: {}", path, ex.getConstraintViolations().size());

                // Get all constraint violations
                Map<String, String> errors = new HashMap<>();
                ex.getConstraintViolations().forEach(violation -> {
                        String field = violation.getPropertyPath().toString();
                        String message = violation.getMessage();
                        errors.put(field, message);
                });

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Constraint Violation",
                                "One or more constraints were violated.",
                                path,
                                errors);

                return new ResponseEntity<>(errorResponse, errorResponse.getStatusCode());
        }

        // Handle all other exceptions
        @ExceptionHandler(Exception.class)
        public ResponseEntity<org.springframework.web.ErrorResponse> handleAllUncaughtExceptions( // Renamed for clarity
                        Exception ex,
                        WebRequest request) {

                log.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Added logging

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected internal server error occurred. Please contact support.", // More
                                                                                                         // user-friendly
                                                                                                         // message
                                getRequestPath(request));
                return new ResponseEntity<>(errorResponse, errorResponse.getStatusCode());
        }

        // Assuming org.springframework.security.access.AccessDeniedException
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<org.springframework.web.ErrorResponse> handleSpringSecurityAccessDenied(
                        AccessDeniedException ex,
                        WebRequest request) {
                
                String path = getRequestPath(request);
                log.warn("Handling AccessDeniedException for path: {}. Message: {}", path, ex.getMessage()); // Using WARN for security-related issues

                com.example.customermanagement.dto.ErrorResponse errorResponse = new com.example.customermanagement.dto.ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Access Denied",
                                ex.getMessage(),
                                getRequestPath(request));

                return new ResponseEntity<>(errorResponse, errorResponse.getStatusCode());
        }

        private String getRequestPath(WebRequest request) {
                if (request instanceof ServletWebRequest) {
                        return ((ServletWebRequest) request).getRequest().getRequestURI();
                }
                return "N/A";
        }
}