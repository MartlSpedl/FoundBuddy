package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.model.Item;
import com.example.foundbuddybackend.model.ItemStatus;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing lost and found items.
 * Uses Firebase Firestore instead of a JPA repository.
 */
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private static final String COLLECTION_NAME = "items";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Returns all items sorted by descending timestamp.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAll() {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<Item> items = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                Item item = doc.toObject(Item.class);
                item.setId(doc.getId());
                items.add(item);
            }

            // Sort descending by timestamp (nulls last)
            items.sort((a, b) -> {
                Long tsA = a.getTimestamp();
                Long tsB = b.getTimestamp();
                if (tsA == null && tsB == null) return 0;
                if (tsA == null) return 1;
                if (tsB == null) return -1;
                return tsB.compareTo(tsA);
            });

            return ResponseEntity.ok(items);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves a single item by Firestore document ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable String id) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                Item item = snapshot.toObject(Item.class);
                item.setId(snapshot.getId());
                return ResponseEntity.ok(item);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Creates a new item.  The {@code id} field is ignored.
     * The timestamp defaults to the current time if not specified.
     * Clients must supply {@link ItemStatus} as "FOUND" or "LOST".
     */
    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        try {
            Firestore db = getFirestore();

            // Generate UUID if missing
            if (item.getId() == null || item.getId().isBlank()) {
                item.setId(UUID.randomUUID().toString());
            }

            if (item.getTimestamp() == null) {
                item.setTimestamp(Instant.now().toEpochMilli());
            }

            // Validate status
            if (item.getStatus() == null) {
                return ResponseEntity.badRequest().build();
            }

            ApiFuture<WriteResult> result = db.collection(COLLECTION_NAME)
                    .document(item.getId())
                    .set(item);
            result.get(); // wait for completion

            return new ResponseEntity<>(item, HttpStatus.CREATED);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Searches items by title or description.
     * Firestore doesn't support OR queries on different fields directly,
     * so we merge results manually.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Item>> search(@RequestParam(name = "q", required = false) String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return getAll();
            }

            Firestore db = getFirestore();
            List<Item> results = new ArrayList<>();

            // Case-insensitive search simulation by lowercasing both sides
            String lowerQuery = query.toLowerCase();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                Item item = doc.toObject(Item.class);
                item.setId(doc.getId());

                if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery))) {
                    results.add(item);
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
