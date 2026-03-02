package com.example.foundbuddybackend.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Full Firebase + Firestore diagnostic endpoint.
     * Tests both gRPC SDK path AND direct HTTP REST API path.
     */
    @GetMapping("/health/firebase")
    public ResponseEntity<Map<String, Object>> firebaseHealth() {
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            var apps = FirebaseApp.getApps();
            info.put("firebaseAppsCount", apps.size());

            if (!apps.isEmpty()) {
                var opts = apps.get(0).getOptions();
                info.put("projectId", opts.getProjectId());
                info.put("storageBucket", opts.getStorageBucket());

                // 1. gRPC SDK test (5s timeout)
                try {
                    Firestore db = FirestoreClient.getFirestore();
                    var future = db.collection("_health").limit(1).get();
                    future.get(5, TimeUnit.SECONDS);
                    info.put("firestoreGrpc", "OK");
                } catch (java.util.concurrent.TimeoutException e) {
                    info.put("firestoreGrpc", "TIMEOUT – gRPC blocked");
                } catch (Exception e) {
                    info.put("firestoreGrpc", "ERROR: " + e.getMessage());
                }

                // 2. Direct REST API test via HTTPS (bypasses gRPC entirely)
                try {
                    String projectId = opts.getProjectId();
                    // Get a fresh access token via file or environment variable
                    GoogleCredentials creds;
                    try {
                        // First try to load from file
                        creds = GoogleCredentials.fromStream(
                            new ClassPathResource("firebase-key.json").getInputStream()
                        ).createScoped("https://www.googleapis.com/auth/datastore");
                    } catch (Exception e) {
                        // Fallback to environment variable
                        String firebaseJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
                        if (firebaseJson != null && !firebaseJson.isBlank()) {
                            creds = GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8)))
                                    .createScoped("https://www.googleapis.com/auth/datastore");
                        } else {
                            throw new Exception("No Firebase credentials available");
                        }
                    }
                    creds.refreshIfExpired();
                    String token = creds.getAccessToken().getTokenValue();

                    RestTemplate rt = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(token);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    String url = "https://firestore.googleapis.com/v1/projects/" + projectId +
                            "/databases/(default)/documents/_health?pageSize=1";

                    ResponseEntity<String> resp = rt.exchange(url, HttpMethod.GET,
                            new HttpEntity<>(headers), String.class);

                    info.put("firestoreRest", "OK (HTTP " + resp.getStatusCodeValue() + ")");
                } catch (Exception e) {
                    info.put("firestoreRest", "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    if (e.getCause() != null) {
                        info.put("firestoreRestCause", e.getCause().getMessage());
                    }
                }

            } else {
                info.put("firestoreGrpc", "NO_FIREBASE_APP");
                info.put("firestoreRest", "NO_FIREBASE_APP");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            info.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return ResponseEntity.ok(info);
    }
}
