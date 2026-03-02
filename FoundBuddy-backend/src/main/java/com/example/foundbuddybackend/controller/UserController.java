package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.dto.ValidationErrorResponse;
import com.example.foundbuddybackend.model.User;
import com.example.foundbuddybackend.service.EmailService;
import com.example.foundbuddybackend.service.FirestoreRestService;
import com.example.foundbuddybackend.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * User management endpoints.
 * Uses FirestoreRestService (HTTPS) instead of the gRPC Firebase Admin SDK,
 * because Render Free Tier blocks outgoing gRPC connections to Google APIs.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final String COLLECTION = "users";

    @Autowired private FirestoreRestService db;
    @Autowired private ValidationService validationService;
    @Autowired private EmailService emailService;

    // ─── helpers ────────────────────────────────────────────────────────────

    /** Convert a Firestore flat map to a User object */
    private User mapToUser(Map<String, Object> m) {
        if (m == null) return null;
        User u = new User();
        u.setId(str(m, "id"));
        u.setUsername(str(m, "username"));
        u.setEmail(str(m, "email"));
        u.setPassword(str(m, "password"));
        u.setEmailVerified(Boolean.TRUE.equals(m.get("emailVerified")));
        u.setVerificationToken(str(m, "verificationToken"));
        u.setPasswordResetToken(str(m, "passwordResetToken"));
        Object ts = m.get("passwordResetRequestedAt");
        if (ts instanceof Number) u.setPasswordResetRequestedAt(((Number) ts).longValue());
        return u;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    /** Convert a User to a plain map for Firestore */
    private Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (u.getId() != null)       m.put("id", u.getId());
        if (u.getUsername() != null) m.put("username", u.getUsername());
        if (u.getEmail() != null)    m.put("email", u.getEmail());
        if (u.getPassword() != null) m.put("password", u.getPassword());
        m.put("emailVerified", u.isEmailVerified());
        if (u.getVerificationToken() != null) m.put("verificationToken", u.getVerificationToken());
        if (u.getPasswordResetToken() != null) m.put("passwordResetToken", u.getPasswordResetToken());
        if (u.getPasswordResetRequestedAt() != null) m.put("passwordResetRequestedAt", u.getPasswordResetRequestedAt());
        return m;
    }

    /** email-safe document ID: test@example.com → test_at_example_com */
    private String docId(String email) {
        return email.replace("@", "_at_").replace(".", "_");
    }

    // ─── endpoints ──────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            List<User> users = new ArrayList<>();
            for (Map<String, Object> doc : docs) {
                User u = mapToUser(doc);
                users.add(u);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(mapToUser(doc));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        try {
            // Validate input
            Map<String, List<String>> errors = new HashMap<>();
            Optional.ofNullable(validationService.validateUsername(user.getUsername()))
                    .ifPresent(e -> errors.put("username", List.of(e)));
            Optional.ofNullable(validationService.validateEmail(user.getEmail()))
                    .ifPresent(e -> errors.put("email", List.of(e)));
            List<String> pwErrors = validationService.validatePassword(user.getPassword());
            if (!pwErrors.isEmpty()) errors.put("password", pwErrors);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(new ValidationErrorResponse("Validierungsfehler", errors));
            }

            // Check email uniqueness via direct doc lookup (no index needed)
            String dId = docId(user.getEmail());
            Map<String, Object> existing = db.getDocument(COLLECTION, dId);
            if (existing != null) {
                errors.put("email", List.of("Diese E-Mail-Adresse ist bereits registriert"));
                return ResponseEntity.badRequest().body(new ValidationErrorResponse("E-Mail bereits vergeben", errors));
            }

            // Set up new user
            user.setId(dId);
            user.setEmailVerified(false);
            user.setVerificationToken(UUID.randomUUID().toString());

            db.setDocument(COLLECTION, dId, userToMap(user));

            // Send email async
            String email = user.getEmail(), name = user.getUsername(), token = user.getVerificationToken();
            new Thread(() -> {
                try { emailService.sendVerificationEmail(email, name, token); }
                catch (Exception ex) { System.err.println("Email error: " + ex.getMessage()); }
            }).start();

            return new ResponseEntity<>(user, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            // Must scan collection for verificationToken (no index needed – small collection)
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            Map<String, Object> found = docs.stream()
                    .filter(d -> token.equals(d.get("verificationToken")))
                    .findFirst().orElse(null);

            if (found == null) {
                return ResponseEntity.badRequest().body("Ungültiger oder abgelaufener Bestätigungslink.");
            }

            User u = mapToUser(found);
            u.setEmailVerified(true);
            u.setVerificationToken(null);
            db.setDocument(COLLECTION, u.getId(), userToMap(u));

            return ResponseEntity.ok(
                    "<html><body style='font-family:Arial;text-align:center;padding:50px'>" +
                    "<h1 style='color:#4CAF50'>E-Mail bestätigt!</h1>" +
                    "<p>Du kannst dich jetzt in der FoundBuddy App anmelden.</p>" +
                    "</body></html>"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Fehler bei der Verifizierung.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, docId(email));
            if (doc == null) return ResponseEntity.notFound().build();

            User u = mapToUser(doc);
            if (u.isEmailVerified()) return ResponseEntity.badRequest().body("E-Mail ist bereits verifiziert.");

            String newToken = UUID.randomUUID().toString();
            u.setVerificationToken(newToken);
            db.setDocument(COLLECTION, u.getId(), userToMap(u));

            String n = u.getUsername(), t = newToken, e2 = u.getEmail();
            new Thread(() -> {
                try { emailService.sendVerificationEmail(e2, n, t); }
                catch (Exception ex) { System.err.println(ex.getMessage()); }
            }).start();

            return ResponseEntity.ok("Bestätigungs-E-Mail wurde erneut gesendet.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503).body("Fehler beim Senden.");
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, docId(email));
            // Security: always return OK
            if (doc == null) return ResponseEntity.ok("Wenn ein Account existiert, wurde eine E-Mail gesendet.");

            User u = mapToUser(doc);
            String resetToken = UUID.randomUUID().toString();
            u.setPasswordResetToken(resetToken);
            u.setPasswordResetRequestedAt(System.currentTimeMillis());
            db.setDocument(COLLECTION, u.getId(), userToMap(u));

            String n = u.getUsername(), t = resetToken, e2 = u.getEmail();
            new Thread(() -> {
                try { emailService.sendPasswordResetEmail(e2, n, t); }
                catch (Exception ex) { System.err.println(ex.getMessage()); }
            }).start();

            return ResponseEntity.ok("Wenn ein Account existiert, wurde eine E-Mail gesendet.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        try {
            List<String> pwErrors = validationService.validatePassword(newPassword);
            if (!pwErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("errors", pwErrors));
            }

            // Scan for reset token
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            Map<String, Object> found = docs.stream()
                    .filter(d -> token.equals(d.get("passwordResetToken")))
                    .findFirst().orElse(null);

            if (found == null) {
                return ResponseEntity.badRequest().body("Ungültiger oder abgelaufener Reset-Link.");
            }

            // Check expiry (15 min)
            Object tsObj = found.get("passwordResetRequestedAt");
            if (tsObj instanceof Number) {
                long requested = ((Number) tsObj).longValue();
                if (System.currentTimeMillis() - requested > 15 * 60 * 1000) {
                    return ResponseEntity.badRequest().body("Reset-Link abgelaufen (15 Min.).");
                }
            }

            User u = mapToUser(found);
            u.setPassword(newPassword);
            u.setPasswordResetToken(null);
            u.setPasswordResetRequestedAt(null);
            db.setDocument(COLLECTION, u.getId(), userToMap(u));

            return ResponseEntity.ok("Passwort erfolgreich geändert.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
