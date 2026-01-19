package com.example.foundbuddybackend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    @Value("${ai.translate.url}")
    private String url;

    private final RestTemplate rest;

    public TranslationService() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5_000);
        f.setReadTimeout(60_000);
        this.rest = new RestTemplate(f);
    }

    public String deToEn(String text) {
        if (text == null || text.isBlank()) return text;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> body = Map.of(
                    "q", text,
                    "source", "de",
                    "target", "en",
                    "format", "text"
            );

            ResponseEntity<Map> resp = rest.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Object translated = resp.getBody() == null ? null : resp.getBody().get("translatedText");
            return translated == null ? text : translated.toString();

        } catch (RestClientException ex) {
            return text; // fallback
        }
    }
}
