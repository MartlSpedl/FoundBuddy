package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.ai.ImageSearchService;
import com.example.foundbuddybackend.dto.AiSearchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiSearchController {

    private final ImageSearchService searchService;

    public AiSearchController(ImageSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public List<AiSearchResult> search(@RequestBody Map<String, String> body) throws Exception {
        String query = body.get("query");
        return searchService.searchByDescription(query);
    }
}
