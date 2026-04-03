package com.example.foundbuddybackend.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final RestTemplate rest;

    public EmbeddingService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000);
        factory.setReadTimeout(120_000);
        this.rest = new RestTemplate(factory);
    }

    @Value("${ai.clip.url}")
    private String clipUrl;

    public List<Double> embedText(String text) {
        try {
            log.debug("Embedding text: {}", text);
            return rest.postForObject(
                    clipUrl + "/embed/text",
                    Map.of("text", text),
                    List.class
            );
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to CLIP service for text embedding: {}", e.getMessage());
            throw new RuntimeException("CLIP service unavailable", e);
        } catch (Exception e) {
            log.error("Failed to embed text '{}': {}", text, e.getMessage());
            throw new RuntimeException("Text embedding failed", e);
        }
    }

    public List<Double> embedImage(String imageUri) {
        try {
            log.debug("Embedding image: {}", imageUri);
            return rest.postForObject(
                    clipUrl + "/embed/image",
                    Map.of("image_uri", imageUri),
                    List.class
            );
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to CLIP service for image embedding: {}", e.getMessage());
            throw new RuntimeException("CLIP service unavailable", e);
        } catch (Exception e) {
            log.error("Failed to embed image '{}': {}", imageUri, e.getMessage());
            throw new RuntimeException("Image embedding failed", e);
        }
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
