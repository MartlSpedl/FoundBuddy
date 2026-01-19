package com.example.foundbuddybackend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${ai.clip.url}")
    private String clipUrl;

    public List<Double> embedText(String text) {
        return rest.postForObject(
                clipUrl + "/embed/text",
                Map.of("text", text),
                List.class
        );
    }

    public List<Double> embedImage(String imageUri) {
        return rest.postForObject(
                clipUrl + "/embed/image",
                Map.of("image_uri", imageUri),
                List.class
        );
    }

    public double cosineSimilarity(List<Double> a, List<Double> b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            na += a.get(i) * a.get(i);
            nb += b.get(i) * b.get(i);
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
