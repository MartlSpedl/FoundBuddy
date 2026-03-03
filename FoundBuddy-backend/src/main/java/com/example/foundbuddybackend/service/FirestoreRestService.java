package com.example.foundbuddybackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Firestore via HTTPS REST API — completely bypasses the gRPC SDK.
 *
 * Render Free Tier blocks gRPC connections to Google APIs.
 * The standard Firebase Admin SDK for Java uses gRPC for Firestore,
 * causing all queries to time out on Render Free Tier.
 *
 * This service uses the Firestore REST API v1 over plain HTTPS (port 443),
 * which is always reachable.
 */
@Service
public class FirestoreRestService {

    private static final String FIRESTORE_BASE =
            "https://firestore.googleapis.com/v1/projects/%s/databases/(default)/documents";

    private final RestTemplate rest = new RestTemplate();
    private final String projectId;
    private final GoogleCredentials credentials;

    public FirestoreRestService() {
        // Use hardcoded Firebase JSON to avoid environment variable issues
        String json = "{\"type\":\"service_account\",\"project_id\":\"foundbuddy\",\"private_key_id\":\"81d5b9e942f1d86849f6ef8f652de1bd85623a59\",\"private_key\":\"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDVyByNPxOqhce8\\neOplPCf7r609kVyi5AQL5KHisxeG6/wfvPd4FwrLtfM1sRlmXU8h1/2c4DHG34XB\\n/cxa/JK1gcWYlEGhwSpPFlz5skI0E04o8WsTgeITihduq7irlmOUaShGb4vg4zh9\\ntlMzZbZof8FDMo07xpZhIb7OOL8H6Q0HpeGufzlTDscgDy5hOsFJJgx2972SykqT\\nTdJz9aK9DEwu/mAPrbfFFl5L5mP49ql+sYjJewtVXlzA4w1BCabrIb41Nr9gcuLd\\np9/5PxoZcCt0T/Ro1HyC2S8s0L+N9Wt+mxZIHGJiPNcUB+HMq0GElKYn1GRmjrwU\\nw3/uLg6LAgMBAAECggEAJEYAEjdRru1kJqadnu/GGP6F9pxji4AE77+reDvLEimY\\nbgoyMz41prsIuWODW8sZUjTD44pm58elenUF56pTeli6nlkJidisxR6WSAfnE87y\\nxB+ye2zeX1JfB3g7rWGnI9dz7f+fTFlBjJMTKvVnLfP/Ztd8/1eCzJhdN8CJKV4i\\nZEdINupiaOsQgc7vDMsm1Hq+uc3WUMc4DGBSFg/6NJ8M57kyA5X44Z9lM8YDDfL8\\nDszCjV6dcqS3KNaouTIYq3XBBvqGuO9g9l9t3mKIaNrh3zUdTO/uUE1QAgNXBfcz\\nktf8QLFL9GnATTR6bH4lY+ZnerNw/P+Bzit7Qde6AQKBgQDp8nQjhE0EB4zoDVSL\\nd/R+brWxa9L9aqE9mvcusG/12AD4yxLtlfNTROQjKv6KbkC9yoy2gxT87KlK4mMC\\n/w4gQx3um0UaYfD10cJcEp+GcPs+7oeY1H+sOLSl1WBm+vzWSO9IbhHR7LqyM/e7\\nv43KwCzrmy7H5ZpZaYtfEBCMuwKBgQDp7wgzVxMNcAgZtkmI22krp/B3TSgQHqPf\\n8v0qzK7sgzg6jiOUb/eehkR9ueEP9zvEnjVdpQlaQll+llrNHaK2OqcRnEGg3Nj+\\n9oYEIqeGIqvtw4D6qEkiyyoN9h5dhEcwa41Ak2AY+9K0AF4P7ZvDVf+X5kWSjFyn\\nmlZ76RjQcQKBgQCfPZJCDq7hnEYUOeafXFJGsRLppmwiZK9GILI5zI0Y+SOINao3\\noAbVWiIzsfM6xNs6lKF9JfJmSqzdNQWSJ4w095prLLM3xwzeNh4mz+JX/5V0+6W+\\nqH4S+zIzYu9QiW0KavC9C3cDvjBCVk89M6DPLkolQSbxl96dF9NvLJ8nOwKBgQDZ\\nqRCmRFRz4lOHKPbOoef/OKkjN+UY6olrrDknA3+WjJla0joEuRFarG851CO2qUEx\\nNktNMGu9UZLXl6oww2xLxdMBLbRaqmI1CMe1PVFGGyiBd7CgiMhT2Vjt2Jx0QsnC\\nIbAx724wkM6BsP3UMC8H1xKUESuWqYbkY1Qowr/V4QKBgQDVxnH785iKU8Xor31w\\nkwMoO0Q1lvYV2FeFAoDtBwEXZ3JluVaMXvE3mIP2yKk/QsuME3PEnM1UsSCpH+nn\\nTW/jI8h2AybSxaQZW6pUf1R9n2VPI3xbrKEN3ObK6QajGYSnpcmE2gqpcGSRWvnb\\nBq5x6R/IngJCoFtjZMF5HUprkQ==\\n-----END PRIVATE KEY-----\\n\",\"client_email\":\"firebase-adminsdk-fbsvc@foundbuddy.iam.gserviceaccount.com\",\"client_id\":\"114131693678835424843\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40foundbuddy.iam.gserviceaccount.com\",\"universe_domain\":\"googleapis.com\"}";
        String pid = "foundbuddy";

        GoogleCredentials creds = null;
        String resolvedProjectId = pid;

        if (json != null && !json.isBlank()) {
            try {
                creds = ServiceAccountCredentials
                        .fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                        .createScoped("https://www.googleapis.com/auth/datastore",
                                "https://www.googleapis.com/auth/cloud-platform");
                if (resolvedProjectId == null || resolvedProjectId.isBlank()) {
                    // Try to extract project_id from the JSON
                    if (json.contains("\"project_id\"")) {
                        int start = json.indexOf("\"project_id\"") + 14;
                        int end = json.indexOf("\"", start);
                        resolvedProjectId = json.substring(start, end);
                    }
                }
                System.out.println("✅ FirestoreRestService: credentials loaded, projectId=" + resolvedProjectId);
            } catch (Exception e) {
                System.err.println("❌ FirestoreRestService: Failed to load credentials: " + e.getMessage());
            }
        }

        this.credentials = creds;
        this.projectId = resolvedProjectId != null ? resolvedProjectId : "foundbuddy";
    }

    /** Get a valid OAuth2 Bearer token */
    private String getToken() throws Exception {
        if (credentials == null) throw new IllegalStateException("No Firebase credentials available");
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    private HttpHeaders authHeaders() throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(getToken());
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String base() {
        return String.format(FIRESTORE_BASE, projectId);
    }

    /**
     * Get a single document by collection + documentId.
     * Returns null if not found (404).
     */
    public Map<String, Object> getDocument(String collection, String documentId) throws Exception {
        String url = base() + "/" + collection + "/" + documentId;
        try {
            ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(authHeaders()), Map.class);
            return flattenFirestoreDoc(resp.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return null; // Document doesn't exist
        }
    }

    /**
     * Write (create/overwrite) a document.
     */
    public void setDocument(String collection, String documentId, Map<String, Object> data) throws Exception {
        String url = base() + "/" + collection + "/" + documentId;
        String body = toFirestoreJson(data);
        
        // Always use PATCH with updateMask for both create and update
        // updateMask tells Firestore which fields to update
        HttpHeaders headers = authHeaders();
        headers.set("X-HTTP-Method-Override", "PATCH");
        
        rest.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);
    }

    /**
     * Get all documents in a collection.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCollection(String collection) throws Exception {
        String url = base() + "/" + collection;
        ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), Map.class);

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> body = resp.getBody();
        if (body == null) return result;

        List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
        if (docs == null) return result;

        for (Map<String, Object> doc : docs) {
            Map<String, Object> flat = flattenFirestoreDoc(doc);
            if (flat != null) result.add(flat);
        }
        return result;
    }

    /**
     * Delete a document.
     */
    public void deleteDocument(String collection, String documentId) throws Exception {
        String url = base() + "/" + collection + "/" + documentId;
        rest.exchange(url, HttpMethod.DELETE, new HttpEntity<>(authHeaders()), Void.class);
    }

    /**
     * Flatten Firestore REST API typed value format to plain Java Map.
     * {"fields": {"name": {"stringValue": "Alice"}, "age": {"integerValue": "30"}}}
     * → {"name": "Alice", "age": 30, "__id__": "document-id"}
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> flattenFirestoreDoc(Map<String, Object> doc) {
        if (doc == null) return null;
        Map<String, Object> result = new LinkedHashMap<>();

        // Extract ID from the "name" field: ".../documents/collection/docId"
        String name = (String) doc.get("name");
        if (name != null) {
            String[] parts = name.split("/");
            result.put("id", parts[parts.length - 1]);
        }

        Map<String, Object> fields = (Map<String, Object>) doc.get("fields");
        if (fields == null) return result;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            result.put(entry.getKey(), extractValue((Map<String, Object>) entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object extractValue(Map<String, Object> typed) {
        if (typed == null) return null;
        if (typed.containsKey("stringValue")) return typed.get("stringValue");
        if (typed.containsKey("integerValue")) {
            Object v = typed.get("integerValue");
            return v instanceof Number ? ((Number)v).longValue() : Long.parseLong(v.toString());
        }
        if (typed.containsKey("doubleValue")) return typed.get("doubleValue");
        if (typed.containsKey("booleanValue")) return typed.get("booleanValue");
        if (typed.containsKey("nullValue")) return null;
        if (typed.containsKey("timestampValue")) return typed.get("timestampValue");
        if (typed.containsKey("arrayValue")) {
            Map<String, Object> arr = (Map<String, Object>) typed.get("arrayValue");
            List<Object> list = new ArrayList<>();
            if (arr != null && arr.containsKey("values")) {
                for (Map<String, Object> v : (List<Map<String, Object>>) arr.get("values")) {
                    list.add(extractValue(v));
                }
            }
            return list;
        }
        if (typed.containsKey("mapValue")) {
            Map<String, Object> m = (Map<String, Object>) typed.get("mapValue");
            if (m != null && m.containsKey("fields")) {
                return flattenFirestoreDoc(Map.of("fields", m.get("fields")));
            }
            return new HashMap<>();
        }
        return typed.toString();
    }

    /**
     * Convert a plain Java Map to Firestore REST JSON format.
     * {"name": "Alice", "age": 30} → {"fields": {"name": {"stringValue":"Alice"}, "age": {"integerValue":"30"}}}
     */
    @SuppressWarnings("unchecked")
    public static String toFirestoreJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{\"fields\":{");
        boolean first = true;
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (e.getKey().equals("id")) continue; // skip synthetic id field
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(e.getKey()).append("\":");
            sb.append(toFirestoreValue(e.getValue()));
        }
        sb.append("}}");
        return sb.toString();
    }

    private static String toFirestoreValue(Object v) {
        if (v == null) return "{\"nullValue\":null}";
        if (v instanceof Boolean) return "{\"booleanValue\":" + v + "}";
        if (v instanceof Long || v instanceof Integer)
            return "{\"integerValue\":\"" + v + "\"}";
        if (v instanceof Double || v instanceof Float)
            return "{\"doubleValue\":" + v + "}";
        if (v instanceof List) {
            StringBuilder sb = new StringBuilder("{\"arrayValue\":{\"values\":[");
            boolean first = true;
            for (Object item : (List<?>)v) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toFirestoreValue(item));
            }
            sb.append("]}}");
            return sb.toString();
        }
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder("{\"mapValue\":{\"fields\":{");
            boolean first = true;
            for (Map.Entry<?,?> e : ((Map<?,?>)v).entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(e.getKey()).append("\":");
                sb.append(toFirestoreValue(e.getValue()));
            }
            sb.append("}}}");
            return sb.toString();
        }
        // String - escape it
        String s = v.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return "{\"stringValue\":\"" + s + "\"}";
    }
}
