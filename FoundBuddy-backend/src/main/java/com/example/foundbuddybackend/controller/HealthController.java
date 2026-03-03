package com.example.foundbuddybackend.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.core.io.ClassPathResource;
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
                    // Use hardcoded Firebase JSON as last resort
                    String firebaseJson = "{\"type\":\"service_account\",\"project_id\":\"foundbuddy\",\"private_key_id\":\"81d5b9e942f1d86849f6ef8f652de1bd85623a59\",\"private_key\":\"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDVyByNPxOqhce8\\neOplPCf7r609kVyi5AQL5KHisxeG6/wfvPd4FwrLtfM1sRlmXU8h1/2c4DHG34XB\\n/cxa/JK1gcWYlEGhwSpPFlz5skI0E04o8WsTgeITihduq7irlmOUaShGb4vg4zh9\\ntlMzZbZof8FDMo07xpZhIb7OOL8H6Q0HpeGufzlTDscgDy5hOsFJJgx2972SykqT\\nTdJz9aK9DEwu/mAPrbfFFl5L5mP49ql+sYjJewtVXlzA4w1BCabrIb41Nr9gcuLd\\np9/5PxoZcCt0T/Ro1HyC2S8s0L+N9Wt+mxZIHGJiPNcUB+HMq0GElKYn1GRmjrwU\\nw3/uLg6LAgMBAAECggEAJEYAEjdRru1kJqadnu/GGP6F9pxji4AE77+reDvLEimY\\nbgoyMz41prsIuWODW8sZUjTD44pm58elenUF56pTeli6nlkJidisxR6WSAfnE87y\\nxB+ye2zeX1JfB3g7rWGnI9dz7f+fTFlBjJMTKvVnLfP/Ztd8/1eCzJhdN8CJKV4i\\nZEdINupiaOsQgc7vDMsm1Hq+uc3WUMc4DGBSFg/6NJ8M57kyA5X44Z9lM8YDDfL8\\nDszCjV6dcqS3KNaouTIYq3XBBvqGuO9g9l9t3mKIaNrh3zUdTO/uUE1QAgNXBfcz\\nktf8QLFL9GnATTR6bH4lY+ZnerNw/P+Bzit7Qde6AQKBgQDp8nQjhE0EB4zoDVSL\\nd/R+brWxa9L9aqE9mvcusG/12AD4yxLtlfNTROQjKv6KbkC9yoy2gxT87KlK4mMC\\n/w4gQx3um0UaYfD10cJcEp+GcPs+7oeY1H+sOLSl1WBm+vzWSO9IbhHR7LqyM/e7\\nv43KwCzrmy7H5ZpZaYtfEBCMuwKBgQDp7wgzVxMNcAgZtkmI22krp/B3TSgQHqPf\\n8v0qzK7sgzg6jiOUb/eehkR9ueEP9zvEnjVdpQlaQll+llrNHaK2OqcRnEGg3Nj+\\n9oYEIqeGIqvtw4D6qEkiyyoN9h5dhEcwa41Ak2AY+9K0AF4P7ZvDVf+X5kWSjFyn\\nmlZ76RjQcQKBgQCfPZJCDq7hnEYUOeafXFJGsRLppmwiZK9GILI5zI0Y+SOINao3\\noAbVWiIzsfM6xNs6lKF9JfJmSqzdNQWSJ4w095prLLM3xwzeNh4mz+JX/5V0+6W+\\nqH4S+zIzYu9QiW0KavC9C3cDvjBCVk89M6DPLkolQSbxl96dF9NvLJ8nOwKBgQDZ\\nqRCmRFRz4lOHKPbOoef/OKkjN+UY6olrrDknA3+WjJla0joEuRFarG851CO2qUEx\\nNktNMGu9UZLXl6oww2xLxdMBLbRaqmI1CMe1PVFGGyiBd7CgiMhT2Vjt2Jx0QsnC\\nIbAx724wkM6BsP3UMC8H1xKUESuWqYbkY1Qowr/V4QKBgQDVxnH785iKU8Xor31w\\nkwMoO0Q1lvYV2FeFAoDtBwEXZ3JluVaMXvE3mIP2yKk/QsuME3PEnM1UsSCpH+nn\\nTW/jI8h2AybSxaQZW6pUf1R9n2VPI3xbrKEN3ObK6QajGYSnpcmE2gqpcGSRWvnb\\nBq5x6R/IngJCoFtjZMF5HUprkQ==\\n-----END PRIVATE KEY-----\\n\",\"client_email\":\"firebase-adminsdk-fbsvc@foundbuddy.iam.gserviceaccount.com\",\"client_id\":\"114131693678835424843\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40foundbuddy.iam.gserviceaccount.com\",\"universe_domain\":\"googleapis.com\"}";
                    
                    GoogleCredentials creds = GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8)))
                            .createScoped("https://www.googleapis.com/auth/datastore");
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
