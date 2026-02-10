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

    private final EmbeddingService embeddingService;
    private final TranslationService translationService;

    public ImageSearchService(EmbeddingService embeddingService, TranslationService translationService) {
        this.embeddingService = embeddingService;
        this.translationService = translationService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    public List<AiSearchResult> searchByDescription(String description) throws Exception {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }

        // Temporär nur Text-Suche ohne CLIP
        List<AiSearchResult> results = new ArrayList<>();
        Firestore db = FirestoreClient.getFirestore();
        
        // Alle Items aus Firestore holen
        CollectionReference items = db.collection(COLLECTION);
        ApiFuture<QuerySnapshot> future = items.get();
        
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        for (QueryDocumentSnapshot doc : documents) {
            FoundItem item = doc.toObject(FoundItem.class);
            if (item != null && item.getImagePath() != null) {
                // Einfache Text-Suche
                double textScore = calculateTextScore(description.toLowerCase(), item);
                double recencyScore = calculateRecencyScore(item);
                double overallScore = 0.7 * textScore + 0.3 * recencyScore;
                
                AiSearchResult result = new AiSearchResult(item, overallScore, 0.0, textScore, recencyScore);
                results.add(result);
            }
        }
        
        // Sortieren nach Score
        results.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        
        return results;
    }
    
    private double calculateTextScore(String searchQuery, FoundItem item) {
        String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
        String description = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
        
        double titleScore = title.contains(searchQuery) ? 0.8 : 0.0;
        double descScore = description.contains(searchQuery) ? 0.6 : 0.0;
        
        return Math.max(titleScore, descScore);
    }
    
    private double calculateRecencyScore(FoundItem item) {
        if (item.getCreatedAt() == null) {
            return 0.0;
        }
        
        long daysSinceCreation = System.currentTimeMillis() - item.getCreatedAt().toDate().getTime();
        long days = daysSinceCreation / (1000 * 60 * 60 * 24);
        
        // Neuer ist besser (max 1.0)
        return Math.max(0.0, 1.0 - (days / 30.0));
    }
}
