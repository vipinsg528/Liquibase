package com.Liquibase.exceptionHandler; // Ensure this matches your package structure

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

// Tells Spring to return an HTTP 404 Not Found status when this exception is thrown
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Unique ID for serialization
    @Serial
    private static final long serialVersionUID = 1L;

    // Constructor that accepts a message for the exception
    public ResourceNotFoundException(String message) {
        // Pass the message to the parent RuntimeException class
        super(message);
    }
}