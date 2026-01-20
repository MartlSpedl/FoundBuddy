package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.ai.EmbeddingService;
import com.example.foundbuddybackend.model.FoundItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * REST controller exposing CRUD endpoints for {@link FoundItem} entities.
 *
 * <p>
 * Now uses Firebase Firestore instead of a JPA repository.
 * All endpoints are prefixed with {@code /api/found-items} and are CORS-enabled.
 */
@RestController
@RequestMapping("/api/found-items")
@CrossOrigin(origins = "*")
public class FoundItemController {

    private static final String COLLECTION_NAME = "found_items";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * Returns all found items sorted by descending creation time.
     */
    @GetMapping
    public ResponseEntity<List<FoundItem>> getAll() {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<FoundItem> items = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                FoundItem item = doc.toObject(FoundItem.class);
                item.setId(doc.getId());
                items.add(item);
            }

            // sort descending by createdAt (nulls last)
            items.sort((a, b) -> {
                Long tsA = a.getCreatedAt();
                Long tsB = b.getCreatedAt();
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
     * Retrieves a single found item by its Firestore document ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoundItem> getById(@PathVariable String id) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();

            if (snapshot.exists()) {
                FoundItem item = snapshot.toObject(FoundItem.class);
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
     * Creates a new found item (auto-generates Firestore ID if missing).
     */
    @PostMapping
    public ResponseEntity<FoundItem> create(@RequestBody FoundItem item) {
        try {
            Firestore db = getFirestore();

            if (item.getId() == null || item.getId().isBlank()) {
                item.setId(UUID.randomUUID().toString());
            }

            if (item.getCreatedAt() == null) {
                item.setCreatedAt(System.currentTimeMillis());
            }

            // Multi-User: imageUri muss eine von allen Clients erreichbare URL sein
            if (item.getImageUri() != null) {
                String uri = item.getImageUri();
                if (uri.startsWith("content://") || uri.startsWith("file://")) {
                    return ResponseEntity.badRequest().build();
                }

                // Embedding nur berechnen, wenn noch nicht vorhanden
                if (item.getImageEmbedding() == null) {
                    item.setImageEmbedding(embeddingService.embedImage(uri));
                }
            }

            db.collection("found_items")
                    .document(item.getId())
                    .set(item);

            return ResponseEntity.ok(item);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable String id) {
        try {
            Firestore db = getFirestore();
            DocumentReference ref = db.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = ref.get().get();

            if (!snapshot.exists()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> update = new HashMap<>();
            update.put("resolved", true);

            ref.update(update).get();

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deletes all found items in the Firestore collection.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {
                db.collection(COLLECTION_NAME).document(doc.getId()).delete();
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws Exception {
        getFirestore()
                .collection("found_items")
                .document(id)
                .delete()
                .get();
    }
}
