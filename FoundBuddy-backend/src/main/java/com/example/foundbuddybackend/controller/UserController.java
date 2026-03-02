package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.dto.ValidationErrorResponse;
import com.example.foundbuddybackend.model.User;
import com.example.foundbuddybackend.service.EmailService;
import com.example.foundbuddybackend.service.ValidationService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final String COLLECTION_NAME = "users";

    @Autowired
    private ValidationService validationService;

    @Autowired
    private EmailService emailService;

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                User user = doc.toObject(User.class);
                user.setId(doc.getId());
                users.add(user);
            }

            return ResponseEntity.ok(users);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                user.setId(snapshot.getId());
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody User user) {
        try {
            Map<String, List<String>> errors = new HashMap<>();

            String usernameError = validationService.validateUsername(user.getUsername());
            if (usernameError != null) {
                errors.put("username", List.of(usernameError));
            }

            String emailError = validationService.validateEmail(user.getEmail());
            if (emailError != null) {
                errors.put("email", List.of(emailError));
            }

            List<String> passwordErrors = validationService.validatePassword(user.getPassword());
            if (!passwordErrors.isEmpty()) {
                errors.put("password", passwordErrors);
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ValidationErrorResponse("Validierungsfehler", errors));
            }

            Firestore db = getFirestore();

            ApiFuture<QuerySnapshot> emailCheck = db.collection(COLLECTION_NAME)
                    .whereEqualTo("email", user.getEmail())
                    .get();
            if (!emailCheck.get().isEmpty()) {
                errors.put("email", List.of("Diese E-Mail-Adresse ist bereits registriert"));
                return ResponseEntity.badRequest()
                        .body(new ValidationErrorResponse("E-Mail bereits vergeben", errors));
            }

            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }

            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setEmailVerified(false);

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME)
                    .document(user.getId())
                    .set(user);
            result.get();

            // E-Mail asynchron senden – damit der User sofort eine Antwort bekommt
            final String emailToSend = user.getEmail();
            final String usernameToSend = user.getUsername();
            final String tokenToSend = verificationToken;
            new Thread(() -> {
                try {
                    emailService.sendVerificationEmail(emailToSend, usernameToSend, tokenToSend);
                } catch (Exception e) {
                    System.err.println("Email konnte nicht gesendet werden: " + e.getMessage());
                }
            }).start();

            return new ResponseEntity<>(user, HttpStatus.CREATED);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            Firestore db = getFirestore();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("verificationToken", token)
                    .get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            if (docs.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Ungültiger oder abgelaufener Bestätigungslink.");
            }

            DocumentSnapshot doc = docs.get(0);
            User user = doc.toObject(User.class);
            user.setId(doc.getId());
            user.setEmailVerified(true);
            user.setVerificationToken(null);

            db.collection(COLLECTION_NAME).document(user.getId()).set(user).get();

            return ResponseEntity.ok(
                    "<html><body style='font-family: Arial; text-align: center; padding: 50px;'>" +
                            "<h1 style='color: #4CAF50;'>E-Mail bestätigt!</h1>" +
                            "<p>Deine E-Mail-Adresse wurde erfolgreich bestätigt.</p>" +
                            "<p>Du kannst dich jetzt in der FoundBuddy App anmelden.</p>" +
                            "</body></html>"
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Fehler bei der Verifizierung.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            Firestore db = getFirestore();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            if (docs.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            DocumentSnapshot doc = docs.get(0);
            User user = doc.toObject(User.class);
            user.setId(doc.getId());

            if (user.isEmailVerified()) {
                return ResponseEntity.badRequest().body("E-Mail ist bereits verifiziert.");
            }

            String newToken = UUID.randomUUID().toString();
            user.setVerificationToken(newToken);

            db.collection(COLLECTION_NAME).document(user.getId()).set(user).get();

            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), newToken);

            return ResponseEntity.ok().body("Bestätigungs-E-Mail wurde erneut gesendet.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503)
                    .body("Mailservice derzeit nicht erreichbar. Bitte später erneut versuchen.");
        }
    }

    // >>> NEU: Passwort-Reset anfordern
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        try {
            Firestore db = getFirestore();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            // Security: immer OK zurückgeben
            if (docs.isEmpty()) {
                return ResponseEntity.ok("Wenn ein Account existiert, wurde eine E-Mail gesendet.");
            }

            DocumentSnapshot doc = docs.get(0);
            User user = doc.toObject(User.class);
            user.setId(doc.getId());

            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetRequestedAt(System.currentTimeMillis());

            db.collection(COLLECTION_NAME).document(user.getId()).set(user).get();

            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken);

            return ResponseEntity.ok("Wenn ein Account existiert, wurde eine E-Mail gesendet.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(503)
                    .body("Mailservice derzeit nicht erreichbar. Bitte später erneut versuchen.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id, @RequestBody User updated) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                return ResponseEntity.notFound().build();
            }

            updated.setId(id);
            docRef.set(updated).get();

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (!snapshot.exists()) {
                return ResponseEntity.notFound().build();
            }

            docRef.delete();
            return ResponseEntity.noContent().build();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
