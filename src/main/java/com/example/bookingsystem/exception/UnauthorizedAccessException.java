package com.example.bookingsystem.exception;

/**
 * Thrown when an authenticated user tries to access or mutate a resource
 * they do not own (data isolation violation). Mapped to HTTP 403.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
