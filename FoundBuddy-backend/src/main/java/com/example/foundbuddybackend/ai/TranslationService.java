package com.example.foundbuddybackend.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final int CONNECT_TIMEOUT = 5_000;
    private static final int READ_TIMEOUT = 10_000;

    @Value("${ai.translate.url}")
    private String url;

    private final RestTemplate rest;

    public TranslationService() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(CONNECT_TIMEOUT);
        f.setReadTimeout(READ_TIMEOUT);
        this.rest = new RestTemplate(f);
    }

    public String deToEn(String text) {
        if (text == null || text.isBlank()) return text;

        try {
            String fullUrl = url + "?q=" + encode(text) + "&langpair=de|en";
            log.debug("Translating: {} -> URL: {}", text, fullUrl);
            
            String response = rest.getForObject(fullUrl, String.class);
            log.debug("Translation response: {}", response);

            if (response != null && response.contains("\"responseData\":")) {
                int start = response.indexOf("\"translatedText\":\"") + 18;
                int end = response.indexOf("\"", start);
                if (start > 17 && end > start) {
                    String translated = response.substring(start, end);
                    log.info("Translated '{}' -> '{}'", text, translated);
                    return translated;
                }
            }
            log.warn("Could not parse translation response, returning original");
            return text;
        } catch (Exception e) {
            log.error("Translation failed: {}", e.getMessage());
            return text;
        }
    }

    private String encode(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }
}
