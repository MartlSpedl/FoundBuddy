package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.ai.EmbeddingService;
import com.example.foundbuddybackend.service.FirestoreRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private static final String COLLECTION = "found_items";

    @Autowired private FirestoreRestService db;
    @Autowired private EmbeddingService embeddingService;

    @PostMapping("/reindex")
    public ResponseEntity<?> reindex() {
        try {
            log.info("Starting embedding reindex...");
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            
            int total = 0;
            int updated = 0;
            int skipped = 0;
            int failed = 0;
            List<String> errors = new ArrayList<>();

            for (Map<String, Object> doc : docs) {
                total++;
                String id = str(doc, "id");
                String imageUri = str(doc, "imageUri");
                
                if (id == null) {
                    skipped++;
                    continue;
                }

                List<Object> rawEmb = (List<Object>) doc.get("imageEmbedding");
                boolean hasEmbedding = rawEmb != null && !rawEmb.isEmpty();

                if (imageUri == null) {
                    skipped++;
                    continue;
                }

                if (hasEmbedding) {
                    skipped++;
                    continue;
                }

                try {
                    log.info("Computing embedding for item {}...", id);
                    List<Double> embedding = embeddingService.embedImage(imageUri);
                    
                    doc.put("imageEmbedding", embedding);
                    db.setDocument(COLLECTION, id, doc);
                    
                    updated++;
                    log.info("Updated embedding for item {}", id);
                } catch (Exception e) {
                    failed++;
                    String error = "Item " + id + ": " + e.getMessage();
                    errors.add(error);
                    log.error("Failed to update embedding for item {}: {}", id, e.getMessage());
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("total", total);
            result.put("updated", updated);
            result.put("skipped", skipped);
            result.put("failed", failed);
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

            log.info("Reindex complete: {} total, {} updated, {} skipped, {} failed", total, updated, skipped, failed);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Reindex failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        try {
            List<Map<String, Object>> docs = db.getCollection(COLLECTION);
            
            int total = 0;
            int withEmbedding = 0;
            int withoutEmbedding = 0;

            for (Map<String, Object> doc : docs) {
                total++;
                String imageUri = str(doc, "imageUri");
                
                if (imageUri == null) continue;

                List<Object> rawEmb = (List<Object>) doc.get("imageEmbedding");
                if (rawEmb != null && !rawEmb.isEmpty()) {
                    withEmbedding++;
                } else {
                    withoutEmbedding++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "totalItems", total,
                    "withEmbedding", withEmbedding,
                    "withoutEmbedding", withoutEmbedding
            ));
        } catch (Exception e) {
            log.error("Stats failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
}
