package com.example.foundbuddybackend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Catches ALL unhandled exceptions from any controller and returns
 * the actual error message in the response body.
 *
 * This makes debugging 500 errors much easier – instead of an empty body,
 * the client (and developer) can see exactly what went wrong.
 *
 * Remove or restrict this in production!
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        ex.printStackTrace();
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
        System.err.println("🔴 Unhandled exception: " + ex.getClass().getName() + ": " + message);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", ex.getClass().getSimpleName(),
                        "message", message
                ));
    }
}
