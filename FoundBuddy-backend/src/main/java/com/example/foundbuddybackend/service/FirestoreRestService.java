package com.example.foundbuddybackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
        // 1. Try classpath (firebase-key.json in src/main/resources — works locally and when bundled)
        // 2. Fall back to FIREBASE_CREDENTIALS_JSON env var (set this on Render if the file is gitignored)
        String json = loadJson();
        String pid = null;

        GoogleCredentials creds = null;
        String resolvedProjectId = pid;

        if (json != null && !json.isBlank()) {
            try {
                creds = ServiceAccountCredentials
                        .fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                        .createScoped("https://www.googleapis.com/auth/datastore",
                                "https://www.googleapis.com/auth/cloud-platform");
                if (resolvedProjectId == null || resolvedProjectId.isBlank()) {
                    // Try to extract project_id from the JSON (more robustly)
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"project_id\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                    if (m.find()) {
                        resolvedProjectId = m.group(1);
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
        System.out.println("🔄 setDocument: PATCH " + collection + "/" + documentId);
        // Use PATCH directly — Firestore REST API does not support X-HTTP-Method-Override
        rest.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(body, authHeaders()), String.class);
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

    /**
     * Load Firebase service-account JSON.
     * Priority:
     *   1. firebase-key.json on the classpath (src/main/resources — bundled with the JAR)
     *   2. FIREBASE_CREDENTIALS_JSON environment variable (set this on Render)
     */
    private static String loadJson() {
        // 1. Classpath
        try (InputStream is = FirestoreRestService.class
                .getClassLoader().getResourceAsStream("firebase-key.json")) {
            if (is != null) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("✅ FirestoreRestService: loaded credentials from classpath firebase-key.json");
                return json;
            }
        } catch (Exception e) {
            System.err.println("⚠️ FirestoreRestService: could not read classpath firebase-key.json: " + e.getMessage());
        }

        // 2. Environment variable (FIREBASE_CREDENTIALS_JSON on Render)
        // IMPORTANT: paste the COMPACT (single-line) JSON from firebase-key.json.
        // Do NOT paste multiline JSON — Render may truncate it.
        String env = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (env != null && !env.isBlank()) {
            System.out.println("✅ FirestoreRestService: loaded credentials from FIREBASE_CREDENTIALS_JSON env var (" + env.length() + " chars)");
            return env;
        }

        System.err.println("❌ FirestoreRestService: no credentials found (classpath or env var)");
        return null;
    }
}
