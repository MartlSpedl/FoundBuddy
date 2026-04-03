package com.example.foundbuddybackend.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.foundbuddybackend.dto.AiSearchResult;
import com.example.foundbuddybackend.model.FoundItem;
import com.example.foundbuddybackend.service.FirestoreRestService;

@Service
public class ImageSearchService {

    private static final Logger log = LoggerFactory.getLogger(ImageSearchService.class);
    private static final String COLLECTION = "found_items";
    private static final double MIN_SCORE = 0.0;
    private static final int MAX_RESULTS = 10;

    @Autowired private FirestoreRestService db;
    private final EmbeddingService embeddingService;
    private final TranslationService translationService;

    public ImageSearchService(EmbeddingService embeddingService, TranslationService translationService) {
        this.embeddingService = embeddingService;
        this.translationService = translationService;
    }

    public List<AiSearchResult> searchByDescription(String description) throws Exception {
        if (description == null || description.isBlank()) return Collections.emptyList();

        List<Map<String, Object>> docs = db.getCollection(COLLECTION);
        if (docs.isEmpty()) return Collections.emptyList();

        List<FoundItem> items = new ArrayList<>();
        int skippedNoEmbedding = 0;
        for (Map<String, Object> doc : docs) {
            FoundItem item = mapToItem(doc);
            if (item == null) continue;
            if (item.getImageUri() != null && (item.getImageEmbedding() == null || item.getImageEmbedding().isEmpty())) {
                skippedNoEmbedding++;
            }
            items.add(item);
        }
        if (skippedNoEmbedding > 0) {
            log.info("Skipped {} items without embeddings during search", skippedNoEmbedding);
        }

        try {
            return searchWithClip(description, items);
        } catch (Exception e) {
            log.warn("CLIP unavailable, falling back to text search: {}", e.getMessage());
            return searchWithTextOnly(description, items);
        }
    }

    private List<AiSearchResult> searchWithClip(String description, List<FoundItem> items) throws Exception {
        String englishQuery = translationService.deToEn(description);
        log.info("CLIP search: '{}' -> '{}'", description, englishQuery);

        if (englishQuery == null || englishQuery.equals(description)) {
            log.warn("Translation returned same text - translation may have failed");
        }

        List<Double> queryEmbedding = embeddingService.embedText(englishQuery);
        log.info("Got query embedding, size: {}", queryEmbedding.size());

        List<AiSearchResult> results = new ArrayList<>();

        for (FoundItem item : items) {
            double clipScore;

            if (item.getImageEmbedding() != null && !item.getImageEmbedding().isEmpty()) {
                clipScore = embeddingService.cosineSimilarity(queryEmbedding, item.getImageEmbedding());
            } else if (item.getImageUri() != null) {
                log.debug("Item {} has no embedding, skipping CLIP score", item.getId());
                continue;
            } else {
                String itemText = buildItemText(item);
                if (itemText.isBlank()) continue;
                String itemTextEn = translationService.deToEn(itemText);
                List<Double> itemEmbedding = embeddingService.embedText(itemTextEn);
                clipScore = embeddingService.cosineSimilarity(queryEmbedding, itemEmbedding);
            }

            double textScore = calculateTextScore(description.toLowerCase(), item);
            double recency   = calculateRecencyScore(item);
            double finalScore = 0.60 * clipScore + 0.25 * textScore + 0.15 * recency;

            log.debug("Item {}: clip={}, text={}, recency={}, final={}", 
                item.getId(), clipScore, textScore, recency, finalScore);

            if (finalScore > MIN_SCORE) {
                results.add(new AiSearchResult(item, finalScore, clipScore, textScore, recency));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(MAX_RESULTS).toList();
    }

    private List<AiSearchResult> searchWithTextOnly(String description, List<FoundItem> items) {
        log.info("Text-only search for: '{}'", description);
        List<AiSearchResult> results = new ArrayList<>();

        for (FoundItem item : items) {
            double textScore  = calculateTextScore(description.toLowerCase(), item);
            double recency    = calculateRecencyScore(item);
            double finalScore = 0.75 * textScore + 0.25 * recency;

            if (finalScore > 0.0) {
                results.add(new AiSearchResult(item, finalScore, 0.1, textScore, recency));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results.stream().limit(MAX_RESULTS).toList();
    }

    @SuppressWarnings("unchecked")
    private FoundItem mapToItem(Map<String, Object> m) {
        if (m == null) return null;
        FoundItem item = new FoundItem();
        item.setId(str(m, "id"));
        item.setTitle(str(m, "title"));
        item.setDescription(str(m, "description"));
        item.setImageUri(str(m, "imageUri"));

        Object ts = m.get("createdAt");
        if (ts instanceof Number) item.setCreatedAt(((Number) ts).longValue());

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

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }

    private String buildItemText(FoundItem item) {
        String t = item.getTitle() != null ? item.getTitle().trim() : "";
        String d = item.getDescription() != null ? item.getDescription().trim() : "";
        return (t + ". " + d).trim();
    }

    private double calculateTextScore(String query, FoundItem item) {
        String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
        String desc  = item.getDescription() != null ? item.getDescription().toLowerCase() : "";

        if (title.contains(query)) return 1.0;
        if (desc.contains(query))  return 0.7;

        double wordScore = 0.0;
        for (String word : query.split("\\s+")) {
            if (word.length() < 3) continue;
            if (title.contains(word)) wordScore = Math.max(wordScore, 0.5);
            if (desc.contains(word))  wordScore = Math.max(wordScore, 0.3);
        }
        return wordScore;
    }

    private double calculateRecencyScore(FoundItem item) {
        if (item.getCreatedAt() == null) return 0.0;
        double ageDays = (System.currentTimeMillis() - item.getCreatedAt()) / (1000.0 * 60 * 60 * 24);
        return Math.max(0.0, 1.0 - (ageDays / 30.0));
    }
}
