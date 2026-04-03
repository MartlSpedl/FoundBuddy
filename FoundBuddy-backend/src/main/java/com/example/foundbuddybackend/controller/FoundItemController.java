package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.ai.EmbeddingService;
import com.example.foundbuddybackend.model.FoundItem;
import com.example.foundbuddybackend.service.FirestoreRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for {@link FoundItem} entities.
 *
 * Uses FirestoreRestService (HTTPS) instead of gRPC FirestoreClient
 * for reliable cross-platform compatibility.
 */
@RestController
@RequestMapping("/api/found-items")
@CrossOrigin(origins = "*")
public class FoundItemController {

    private static final String COLLECTION = "found_items";

    @Autowired private FirestoreRestService db;
    @Autowired private EmbeddingService embeddingService;

    // ─── helpers ────────────────────────────────────────────────────────────

    private FoundItem mapToItem(Map<String, Object> m) {
        if (m == null) return null;
        FoundItem item = new FoundItem();
        item.setId(str(m, "id"));
        item.setTitle(str(m, "title"));
        item.setDescription(str(m, "description"));
        item.setImageUri(str(m, "imageUri"));
        item.setStatus(str(m, "status") != null ? str(m, "status") : "Gefunden");
        item.setUploaderName(str(m, "uploaderName") != null ? str(m, "uploaderName") : "Unbekannt");
        item.setWorkflowStatus(str(m, "workflowStatus") != null ? str(m, "workflowStatus") : "Gemeldet");

        Object ts = m.get("createdAt");
        if (ts instanceof Number) item.setCreatedAt(((Number) ts).longValue());

        Object resolved = m.get("isResolved");
        item.setResolved(Boolean.TRUE.equals(resolved));

        Object likes = m.get("likes");
        if (likes instanceof Number) item.setLikes(((Number) likes).intValue());

        Object isFavorite = m.get("isFavorite");
        item.setFavorite(Boolean.TRUE.equals(isFavorite));

        Object uploaderId = m.get("uploaderId");
        if (uploaderId != null) item.setUploaderId(str(m, "uploaderId"));

        // statusHistory (List<Map>)
        @SuppressWarnings("unchecked")
        List<Object> rawHistory = (List<Object>) m.get("statusHistory");
        if (rawHistory != null) {
            List<Object> history = new ArrayList<>();
            for (Object h : rawHistory) {
                if (h instanceof Map) history.add(h);
            }
            item.setStatusHistory(history);
        }

        // allowedEditors (List<String>)
        @SuppressWarnings("unchecked")
        List<Object> rawEditors = (List<Object>) m.get("allowedEditors");
        if (rawEditors != null) {
            List<String> editors = new ArrayList<>();
            for (Object e : rawEditors) {
                if (e != null) editors.add(e.toString());
            }
            item.setAllowedEditors(editors);
        }

        // imageEmbedding (List<Double>) — stored in Firestore for search
        @SuppressWarnings("unchecked")
        List<Object> rawEmb = (List<Object>) m.get("imageEmbedding");
        if (rawEmb != null) {
            List<Double> emb = new ArrayList<>();
            for (Object v : rawEmb) {
                if (v instanceof Number) emb.add(((Number) v).doubleValue());
            }
            item.setImageEmbedding(emb);
        }

        return item;
    }

    private void setUploaderId(String uploaderId) {
        // Already handled above
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private Map<String, Object> itemToMap(FoundItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (item.getId() != null)           m.put("id", item.getId());
        if (item.getTitle() != null)        m.put("title", item.getTitle());
        if (item.getDescription() != null)  m.put("description", item.getDescription());
        if (item.getImageUri() != null)     m.put("imageUri", item.getImageUri());
        if (item.getCreatedAt() != null)    m.put("createdAt", item.getCreatedAt());
        m.put("status", item.getStatus() != null ? item.getStatus() : "Gefunden");
        m.put("isResolved", item.isResolved());
        m.put("uploaderName", item.getUploaderName() != null ? item.getUploaderName() : "Unbekannt");
        m.put("uploaderId", item.getUploaderId() != null ? item.getUploaderId() : "");
        m.put("likes", item.getLikes());
        m.put("workflowStatus", item.getWorkflowStatus() != null ? item.getWorkflowStatus() : "Gemeldet");
        m.put("isFavorite", item.isFavorite());
        if (item.getStatusHistory() != null) m.put("statusHistory", item.getStatusHistory());
        if (item.getAllowedEditors() != null) m.put("allowedEditors", item.getAllowedEditors());
        if (item.getImageEmbedding() != null) m.put("imageEmbedding", item.getImageEmbedding());
        return m;
    }

    // ─── endpoints ──────────────────────────────────────────────────────────

    /** Returns all found items sorted by descending creation time. */
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            List<FoundItem> items = new ArrayList<>();
            for (Map<String, Object> doc : docs) {
                FoundItem item = mapToItem(doc);
                if (item != null) items.add(item);
            }
            // Sort descending by createdAt (nulls last)
            items.sort((a, b) -> {
                Long tsA = a.getCreatedAt(), tsB = b.getCreatedAt();
                if (tsA == null && tsB == null) return 0;
                if (tsA == null) return 1;
                if (tsB == null) return -1;
                return tsB.compareTo(tsA);
            });
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Retrieves a single found item by Firestore document ID. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(mapToItem(doc));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Creates a new found item (auto-generates ID if missing). */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody FoundItem item) {
        try {
            // ID
            if (item.getId() == null || item.getId().isBlank()) {
                item.setId(UUID.randomUUID().toString());
            }
            // Timestamp
            if (item.getCreatedAt() == null) {
                item.setCreatedAt(System.currentTimeMillis());
            }
            // Validate URI
            if (item.getImageUri() != null) {
                String uri = item.getImageUri();
                if (uri.startsWith("content://") || uri.startsWith("file://")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Local URIs not allowed"));
                }
            }

            // ✅ Save item IMMEDIATELY (without embedding) so response is instant
            db.setDocument(COLLECTION, item.getId(), itemToMap(item));

            // ⚡ Compute embedding ASYNCHRONOUSLY in background — never blocks the client
            if (item.getImageUri() != null) {
                final String itemId = item.getId();
                final String imageUri = item.getImageUri();
                new Thread(() -> {
                    try {
                        System.out.println("🔄 Computing embedding for item " + itemId + " in background...");
                        List<Double> embedding = embeddingService.embedImage(imageUri);
                        // Patch the embedding into the existing Firestore document
                        Map<String, Object> existing = db.getDocument(COLLECTION, itemId);
                        if (existing != null) {
                            existing.put("imageEmbedding", embedding);
                            db.setDocument(COLLECTION, itemId, existing);
                            System.out.println("✅ Embedding saved for item " + itemId);
                        }
                    } catch (Exception e) {
                        // Just log — embedding is optional, item is already saved
                        System.err.println("⚠️ Embedding failed for item " + itemId + ": " + e.getMessage());
                    }
                }, "embedding-" + item.getId()).start();
            }

            return ResponseEntity.ok(item);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Marks an item as resolved. */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable String id) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();

            FoundItem item = mapToItem(doc);
            item.setResolved(true);
            db.setDocument(COLLECTION, id, itemToMap(item));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Updates workflow status and appends to history. */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();

            FoundItem item = mapToItem(doc);
            String newStatus = str(body, "newStatus");
            String userId = str(body, "userId");
            String username = str(body, "username");
            String comment = str(body, "comment");

            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "newStatus is required"));
            }

            String oldStatus = item.getWorkflowStatus();

            // Append to status history
            Map<String, Object> historyEntry = new LinkedHashMap<>();
            historyEntry.put("oldStatus", oldStatus != null ? oldStatus : "Gemeldet");
            historyEntry.put("newStatus", newStatus);
            historyEntry.put("userId", userId != null ? userId : "");
            historyEntry.put("username", username != null ? username : "Unbekannt");
            historyEntry.put("timestamp", System.currentTimeMillis());
            if (comment != null) historyEntry.put("comment", comment);

            List<Object> history = item.getStatusHistory();
            if (history == null) history = new ArrayList<>();
            history.add(historyEntry);
            item.setStatusHistory(history);

            // Update workflow status
            item.setWorkflowStatus(newStatus);

            db.setDocument(COLLECTION, id, itemToMap(item));
            return ResponseEntity.ok(Map.of("success", true, "workflowStatus", newStatus, "statusHistory", history));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Returns status history for an item. */
    @GetMapping("/{id}/status-history")
    public ResponseEntity<?> getStatusHistory(@PathVariable String id) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();

            FoundItem item = mapToItem(doc);
            List<Object> history = item.getStatusHistory();
            if (history == null) history = new ArrayList<>();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Toggles favorite status for an item. */
    @PutMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable String id, @RequestParam String userId) {
        try {
            Map<String, Object> doc = db.getDocument(COLLECTION, id);
            if (doc == null) return ResponseEntity.notFound().build();

            FoundItem item = mapToItem(doc);
            boolean newValue = !item.isFavorite();
            item.setFavorite(newValue);

            // Add user to allowedEditors when favoriting
            List<String> editors = item.getAllowedEditors();
            if (editors == null) editors = new ArrayList<>();
            if (newValue && !editors.contains(userId)) {
                editors.add(userId);
                item.setAllowedEditors(editors);
            }

            db.setDocument(COLLECTION, id, itemToMap(item));
            return ResponseEntity.ok(Map.of("success", true, "isFavorite", newValue));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Deletes all found items. */
    @DeleteMapping
    public ResponseEntity<?> deleteAll() {
        try {
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            for (Map<String, Object> doc : docs) {
                String id = (String) doc.get("id");
                if (id != null) db.deleteDocument(COLLECTION, id);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Deletes a single item. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            db.deleteDocument(COLLECTION, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
