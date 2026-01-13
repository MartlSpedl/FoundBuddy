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

    public ImageSearchService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    public List<AiSearchResult> searchByDescription(String description) throws Exception {

        List<Double> queryEmbedding = embeddingService.embedText(description);

        ApiFuture<QuerySnapshot> future =
                db().collection(COLLECTION).get();

        List<QueryDocumentSnapshot> docs = future.get().getDocuments();
        List<AiSearchResult> results = new ArrayList<>();

        Set<String> seenUris = new HashSet<>();

        for (QueryDocumentSnapshot doc : docs) {

            FoundItem item = doc.toObject(FoundItem.class);
            item.setId(doc.getId());

            // 🔹 Falls noch kein Bild-Embedding existiert → nachträglich erzeugen
            if (item.getImageEmbedding() == null && item.getImageUri() != null) {
                item.setImageEmbedding(
                        embeddingService.embedImage(item.getImageUri())
                );
                db().collection(COLLECTION)
                        .document(item.getId())
                        .set(item);
            }

            if (item.getImageEmbedding() == null) {
                continue;
            }

            // 🔹 1) CLIP-Score (Bild ↔ Text)
            double clipScore = embeddingService.cosineSimilarity(
                    queryEmbedding,
                    item.getImageEmbedding()
            );

            // 🔹 2) Text-Score (Titel + Beschreibung)
            double textScore = textScore(item, description);

            // 🔹 3) Zeit-Score (Recency)
            double recencyScore = recencyScore(item);

            // 🔹 Finaler Hybrid-Score
            double finalScore =
                    0.65 * clipScore +
                            0.25 * textScore +
                            0.10 * recencyScore;

            if (item.getImageUri() != null && !seenUris.add(item.getImageUri())) {
                continue; // gleiche imageUri schon gesehen -> skip
            }

            results.add(
                    new AiSearchResult(
                            item,
                            finalScore,
                            clipScore,
                            textScore,
                            recencyScore
                    )
            );
        }

        // 🔹 Sortierung nach Final-Score
        results.sort((a, b) ->
                Double.compare(b.getScore(), a.getScore())
        );

        // 🔹 Mindestqualität + Limit
        return results.stream()
                .filter(r -> r.getScore() > 0.25)
                .limit(10)
                .toList();
    }

    private double textScore(FoundItem item, String query) {
        if (query == null || query.isBlank()) return 0.0;

        String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
        String desc  = item.getDescription() == null ? "" : item.getDescription().toLowerCase();

        // Tokenize query: nur sinnvolle Wörter (>=3 Zeichen)
        String[] tokens = query.toLowerCase()
                .replaceAll("[^\\p{L}\\p{Nd}\\s]", " ")
                .split("\\s+");

        int total = 0;
        double hits = 0.0;

        for (String t : tokens) {
            if (t.length() < 3) continue;
            total++;

            // Titel stärker gewichten als Beschreibung
            if (title.contains(t)) hits += 1.0;
            else if (desc.contains(t)) hits += 0.6;
        }

        if (total == 0) return 0.0;

        // normalisieren auf 0..1
        double score = hits / total;
        return Math.max(0.0, Math.min(1.0, score));
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
