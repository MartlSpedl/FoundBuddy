package com.example.foundbuddybackend.ai;

import com.example.foundbuddybackend.dto.AiSearchResult;
import com.example.foundbuddybackend.model.FoundItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ImageSearchService {

    private static final String COLLECTION = "found_items";

    // Minimum score to appear in results
    private static final double MIN_SCORE = 0.15;

    // How many results to return at most
    private static final int MAX_RESULTS = 10;

    private final EmbeddingService embeddingService;
    private final TranslationService translationService;

    public ImageSearchService(EmbeddingService embeddingService, TranslationService translationService) {
        this.embeddingService = embeddingService;
        this.translationService = translationService;
    }

    /**
     * Searches found items by natural language description.
     * Uses CLIP embeddings for semantic search when available,
     * falls back to text-only search if CLIP is unavailable.
     */
    public List<AiSearchResult> searchByDescription(String description) throws Exception {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }

        // Load all items from Firestore
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference col = db.collection(COLLECTION);
        ApiFuture<QuerySnapshot> future = col.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        // Try CLIP-based semantic search first
        try {
            return searchWithClip(description, documents);
        } catch (Exception e) {
            System.err.println("⚠️ CLIP service unavailable, falling back to text search: " + e.getMessage());
            return searchWithTextOnly(description, documents);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLIP-based semantic search
    // ─────────────────────────────────────────────────────────────────────────

    private List<AiSearchResult> searchWithClip(String description, List<QueryDocumentSnapshot> documents) throws Exception {
        // Translate German query to English for better CLIP results
        String englishQuery = translationService.deToEn(description);
        System.out.println("🔍 CLIP search: '" + description + "' → '" + englishQuery + "'");

        // Get text embedding for the search query
        List<Double> queryEmbedding = embeddingService.embedText(englishQuery);

        List<AiSearchResult> results = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            FoundItem item = doc.toObject(FoundItem.class);
            if (item == null) continue;

            item.setId(doc.getId());

            double clipScore = 0.0;

            // If item has a stored image embedding → use it for similarity
            if (item.getImageEmbedding() != null && !item.getImageEmbedding().isEmpty()) {
                clipScore = embeddingService.cosineSimilarity(queryEmbedding, item.getImageEmbedding());
            } else if (item.getImageUri() != null) {
                // Item has image but no stored embedding → compute text embedding from item description
                String itemTextEn = translationService.deToEn(buildItemText(item));
                List<Double> itemEmbedding = embeddingService.embedText(itemTextEn);
                clipScore = embeddingService.cosineSimilarity(queryEmbedding, itemEmbedding);
            } else {
                // No image → embed item title/description text
                String itemTextEn = translationService.deToEn(buildItemText(item));
                List<Double> itemEmbedding = embeddingService.embedText(itemTextEn);
                clipScore = embeddingService.cosineSimilarity(queryEmbedding, itemEmbedding);
            }

            double textScore  = calculateTextScore(description.toLowerCase(), item);
            double recency    = calculateRecencyScore(item);

            // Weighted final score: CLIP semantic (60%) + text keyword (25%) + recency (15%)
            double finalScore = 0.60 * clipScore + 0.25 * textScore + 0.15 * recency;

            if (finalScore > MIN_SCORE) {
                results.add(new AiSearchResult(item, finalScore, clipScore, textScore, recency));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(MAX_RESULTS).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text-only fallback search (no CLIP)
    // ─────────────────────────────────────────────────────────────────────────

    private List<AiSearchResult> searchWithTextOnly(String description, List<QueryDocumentSnapshot> documents) {
        System.out.println("📝 Text-only search for: '" + description + "'");
        List<AiSearchResult> results = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            FoundItem item = doc.toObject(FoundItem.class);
            if (item == null) continue;

            item.setId(doc.getId());

            double textScore  = calculateTextScore(description.toLowerCase(), item);
            double recency    = calculateRecencyScore(item);
            double finalScore = 0.75 * textScore + 0.25 * recency;

            if (finalScore > 0.0) {
                results.add(new AiSearchResult(item, finalScore, 0.0, textScore, recency));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(MAX_RESULTS).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Score helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String buildItemText(FoundItem item) {
        String title = item.getTitle() != null ? item.getTitle().trim() : "";
        String desc  = item.getDescription() != null ? item.getDescription().trim() : "";
        return (title + ". " + desc).trim();
    }

    private double calculateTextScore(String searchQuery, FoundItem item) {
        String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
        String desc  = item.getDescription() != null ? item.getDescription().toLowerCase() : "";

        // Exact match gets higher score
        if (title.contains(searchQuery)) return 1.0;
        if (desc.contains(searchQuery))  return 0.7;

        // Partial word match (any single word in the query)
        String[] words = searchQuery.split("\\s+");
        double wordScore = 0.0;
        for (String word : words) {
            if (word.length() < 3) continue; // skip very short words
            if (title.contains(word)) wordScore = Math.max(wordScore, 0.5);
            if (desc.contains(word))  wordScore = Math.max(wordScore, 0.3);
        }
        return wordScore;
    }

    private double calculateRecencyScore(FoundItem item) {
        if (item.getCreatedAt() == null) return 0.0;

        long ageMillis = System.currentTimeMillis() - item.getCreatedAt();
        double ageDays = ageMillis / (1000.0 * 60 * 60 * 24);

        // Linear decay: 1.0 at age 0 days → 0.0 at age 30 days
        return Math.max(0.0, 1.0 - (ageDays / 30.0));
    }
}
