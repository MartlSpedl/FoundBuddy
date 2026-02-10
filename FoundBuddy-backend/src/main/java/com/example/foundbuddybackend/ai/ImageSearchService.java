package com.example.foundbuddybackend.ai;

import com.example.foundbuddybackend.dto.AiSearchResult;
import com.example.foundbuddybackend.model.FoundItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ImageSearchService {

    private static final String COLLECTION = "found_items";

    private final EmbeddingService embeddingService;
    private final TranslationService translationService;
    private final MockImageSearchService mockSearchService;

    public ImageSearchService(EmbeddingService embeddingService, TranslationService translationService, MockImageSearchService mockSearchService) {
        this.embeddingService = embeddingService;
        this.translationService = translationService;
        this.mockSearchService = mockSearchService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    public List<AiSearchResult> searchByDescription(String description) throws Exception {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }

        // Temporär Mock-Service verwenden, da CLIP Service nicht erreichbar
        try {
            // Versuche CLIP Service
            return searchWithEmbedding(description);
        } catch (Exception e) {
            System.out.println("CLIP Service nicht erreichbar, verwende Mock: " + e.getMessage());
            // Fallback auf Mock-Suche
            return mockSearchService.searchByDescription(description);
        }
    }

    private List<AiSearchResult> searchWithEmbedding(String description) throws Exception {
        // Original CLIP Implementierung
        String englishDescription = translationService.deToEn(description);
        List<Double> queryEmbedding = embeddingService.embedText(englishDescription);

        CollectionReference items = db().collection(COLLECTION);
        ApiFuture<QuerySnapshot> future = items.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<AiSearchResult> results = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            FoundItem item = doc.toObject(FoundItem.class);
            if (item != null && item.getImagePath() != null) {
                String itemText = translationService.deToEn(item.getTitle() + " " + item.getDescription());
                List<Double> itemEmbedding = embeddingService.embedText(itemText);

                double clipScore = calculateCosineSimilarity(queryEmbedding, itemEmbedding);
                double textScore = calculateTextScore(description.toLowerCase(), item);
                double recencyScore = calculateRecencyScore(item);
                double overallScore = 0.4 * clipScore + 0.4 * textScore + 0.2 * recencyScore;

                AiSearchResult result = new AiSearchResult(item, overallScore, clipScore, textScore, recencyScore);
                results.add(result);
            }
                    0.70 * clipScore +
                            0.25 * textScore +
                            0.05 * recencyScore;

            results.add(new AiSearchResult(
                    item,
                    finalScore,
                    clipScore,
                    textScore,
                    recencyScore
            ));
        }

        // Sortierung nach Final-Score
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Mindestqualität + Limit
        return results.stream()
                .filter(r -> r.getScore() > 0.25)
                .limit(10)
                .toList();
    }

    private String buildItemTextDe(FoundItem item) {
        String title = item.getTitle() == null ? "" : item.getTitle().trim();
        String desc  = item.getDescription() == null ? "" : item.getDescription().trim();

        // "Title. Description" (falls eins leer ist, passt trim danach)
        return (title + ". " + desc).trim();
    }

    private double recencyScore(FoundItem item) {
        if (item.getCreatedAt() == null) return 0.0;

        long ageMillis = System.currentTimeMillis() - item.getCreatedAt();
        double ageHours = ageMillis / 1000.0 / 3600.0;

        if (ageHours <= 24) return 1.0;
        if (ageHours >= 168) return 0.0; // älter als 7 Tage

        return 1.0 - (ageHours / 168.0);
    }
}
