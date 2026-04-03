package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.ai.EmbeddingService;
import com.example.foundbuddybackend.ai.ImageSearchService;
import com.example.foundbuddybackend.ai.TranslationService;
import com.example.foundbuddybackend.dto.AiSearchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiSearchController {

    private final ImageSearchService searchService;
    private final TranslationService translationService;
    private final EmbeddingService embeddingService;

    public AiSearchController(ImageSearchService searchService, 
                            TranslationService translationService,
                            EmbeddingService embeddingService) {
        this.searchService = searchService;
        this.translationService = translationService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/search")
    public List<AiSearchResult> search(@RequestBody Map<String, String> body) throws Exception {
        String query = body.get("description");
        if (query == null) query = body.get("query");
        return searchService.searchByDescription(query);
    }

    @GetMapping("/test-translate")
    public Map<String, String> testTranslate(@RequestParam String text) {
        String translated = translationService.deToEn(text);
        return Map.of("original", text, "translated", translated);
    }

    @GetMapping("/test-embed")
    public Map<String, Object> testEmbed(@RequestParam String text) {
        try {
            List<Double> embedding = embeddingService.embedText(text);
            return Map.of("text", text, "embeddingSize", embedding.size(), "first3", embedding.subList(0, Math.min(3, embedding.size())));
        } catch (Exception e) {
            return Map.of("text", text, "error", e.getMessage());
        }
    }
}
