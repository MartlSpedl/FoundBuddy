package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing user profiles using Firebase Firestore.
 *
 * <p>No authentication implemented; any caller can create, update or delete users.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final String COLLECTION_NAME = "users";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Returns all users.
     */
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

    /**
     * Retrieves a single user by Firestore document ID.
     */
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

    /**
     * Creates a new user profile. The {@code id} field is ignored and replaced by a UUID.
     */
    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        try {
            Firestore db = getFirestore();

            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME)
                    .document(user.getId())
                    .set(user);
            result.get();

            return new ResponseEntity<>(user, HttpStatus.CREATED);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Updates an existing user profile. Returns 404 if not found.
     */
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

    /**
     * Deletes a user by ID.
     */
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
