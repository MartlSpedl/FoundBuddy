package com.example.foundbuddybackend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    // Email Regex Pattern (RFC 5322 vereinfacht)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Passwort Anforderungen:
    // - Mindestens 8 Zeichen
    // - Mindestens ein Großbuchstabe
    // - Mindestens ein Kleinbuchstabe
    // - Mindestens eine Zahl
    // - Mindestens ein Sonderzeichen
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validiert eine E-Mail-Adresse.
     * @return null wenn gültig, sonst Fehlermeldung
     */
    public String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return "E-Mail-Adresse darf nicht leer sein";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Ungültige E-Mail-Adresse";
        }
        return null;
    }

    /**
     * Validiert ein Passwort und gibt alle Fehler zurück.
     * @return Liste der Fehler (leer wenn gültig)
     */
    public List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Passwort darf nicht leer sein");
            return errors;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("Passwort muss mindestens " + MIN_PASSWORD_LENGTH + " Zeichen lang sein");
        }

        if (!password.matches(".*[A-Z].*")) {
            errors.add("Passwort muss mindestens einen Großbuchstaben enthalten");
        }

        if (!password.matches(".*[a-z].*")) {
            errors.add("Passwort muss mindestens einen Kleinbuchstaben enthalten");
        }

        if (!password.matches(".*[0-9].*")) {
            errors.add("Passwort muss mindestens eine Zahl enthalten");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("Passwort muss mindestens ein Sonderzeichen enthalten");
        }

        return errors;
    }

    /**
     * Validiert einen Benutzernamen.
     * @return null wenn gültig, sonst Fehlermeldung
     */
    public String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Benutzername darf nicht leer sein";
        }
        if (username.length() < 3) {
            return "Benutzername muss mindestens 3 Zeichen lang sein";
        }
        if (username.length() > 30) {
            return "Benutzername darf maximal 30 Zeichen lang sein";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Benutzername darf nur Buchstaben, Zahlen und Unterstriche enthalten";
        }
        return null;
    }
}
