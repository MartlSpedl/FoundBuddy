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
            return List.of();
        }

        // 1) Query: DE -> EN (vorübergehend deaktiviert, direkt EN verwenden)
        String queryEn = description; // translationService.deToEn(description);

        // 2) Query-Embedding (Text)
        List<Double> queryEmbedding = embeddingService.embedText(queryEn);

        // 3) Firestore laden
        ApiFuture<QuerySnapshot> future = db().collection(COLLECTION).get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        List<AiSearchResult> results = new ArrayList<>();
        Set<String> seenUris = new HashSet<>();

        for (QueryDocumentSnapshot doc : docs) {
            FoundItem item = doc.toObject(FoundItem.class);
            item.setId(doc.getId());

            // Dedup möglichst früh
            String uri = item.getImageUri();
            if (uri != null && !uri.isBlank() && !seenUris.add(uri)) {
                continue;
            }

            // A) Image-Embedding lazy erzeugen
            if ((item.getImageEmbedding() == null || item.getImageEmbedding().isEmpty())
                    && uri != null && !uri.isBlank()) {

                List<Double> emb = embeddingService.embedImage(uri);
                item.setImageEmbedding(emb);

                // Nur Feld updaten
                db().collection(COLLECTION)
                        .document(item.getId())
                        .update("imageEmbedding", emb);
            }

            if (item.getImageEmbedding() == null || item.getImageEmbedding().isEmpty()) {
                continue;
            }

            // B) Text-Embedding lazy erzeugen (NEU)
            // Wir embedden Titel+Beschreibung (als EN), damit es zur Query passt
            if (item.getTextEmbedding() == null || item.getTextEmbedding().isEmpty()) {
                String itemTextDe = buildItemTextDe(item);

                if (!itemTextDe.isBlank()) {
                    String itemTextEn = itemTextDe; // translationService.deToEn(itemTextDe);
                    List<Double> textEmb = embeddingService.embedText(itemTextEn);
                    item.setTextEmbedding(textEmb);

                    db().collection(COLLECTION)
                            .document(item.getId())
                            .update("textEmbedding", textEmb);
                }
            }

            // 1) CLIP-Score (Bild ↔ Query-Text)
            double clipScore = embeddingService.cosineSimilarity(
                    queryEmbedding,
                    item.getImageEmbedding()
            );

            // 2) Text-Score (Query-Text ↔ Item-Text) via Embedding (NEU)
            double textScore = 0.0;
            if (item.getTextEmbedding() != null && !item.getTextEmbedding().isEmpty()) {
                textScore = embeddingService.cosineSimilarity(
                        queryEmbedding,
                        item.getTextEmbedding()
                );
            }

            // 3) Zeit-Score (Recency)
            double recencyScore = recencyScore(item);

            // Finaler Hybrid-Score (leicht angepasst)
            double finalScore =
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
