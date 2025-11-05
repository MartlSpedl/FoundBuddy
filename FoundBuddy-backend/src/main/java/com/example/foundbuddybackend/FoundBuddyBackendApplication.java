package com.example.foundbuddybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the FoundBuddy backend application.
 *
 * <p>
 * This Spring Boot application exposes a simple REST API to manage found and lost
 * items as well as user profiles. The aim is to complement the existing
 * Android front‑end by persisting data on the server side.  The API is
 * intentionally lightweight and uses an embedded H2 database for storage.  See
 * the controllers in {@code com.example.foundbuddybackend.controller} for
 * available endpoints.
 */
@SpringBootApplication
public class FoundBuddyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoundBuddyBackendApplication.class, args);
    }
}