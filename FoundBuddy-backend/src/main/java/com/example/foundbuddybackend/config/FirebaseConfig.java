package com.example.foundbuddybackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Initialisiert Firebase beim Anwendungsstart.
 * Lädt den Service-Account entweder aus einer Umgebungsvariablen oder aus der Ressource.
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;

            // Service-Account aus Umgebungsvariable lesen (kompletter JSON-Inhalt)
            String firebaseJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

            if (firebaseJson != null && !firebaseJson.isBlank()) {
                System.out.println("🔑 FIREBASE_SERVICE_ACCOUNT_JSON gefunden, Länge: " + firebaseJson.length());

                // ⚠️ Render encodes newlines in env vars as literal \n (two chars) instead of real newlines.
                // RSA private keys NEED real newlines — fix them before parsing.
                // This replaces \\n (escaped) → real newline only inside the "private_key" value.
                firebaseJson = fixPrivateKeyNewlines(firebaseJson);

                serviceAccount = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
                System.out.println("🔑 Firebase Key aus Umgebungsvariable geladen");
            } else {
                System.out.println("⚠️ Keine Umgebungsvariable für den Firebase-Key gesetzt – lade Schlüssel aus der Resource");
                // Fallback: Schlüssel als Ressource laden
                // Die Datei `firebase-key.json` muss unter src/main/resources liegen, damit sie im JAR enthalten ist.
                serviceAccount = new ClassPathResource("firebase-key.json").getInputStream();
                System.out.println("🔑 Firebase Key aus Resource geladen");
            }

            // Storage Bucket: ueber Env Var setzen (Render)
            // Beispiel: FIREBASE_STORAGE_BUCKET="<project-id>.appspot.com"
            String storageBucket = System.getenv("FIREBASE_STORAGE_BUCKET");

            // Project ID MUSS gesetzt sein, sonst kann Firestore den Endpoint nicht auflösen!
            // Entweder über eigene Env Var FIREBASE_PROJECT_ID, oder aus FIREBASE_STORAGE_BUCKET extrahieren.
            String projectId = System.getenv("FIREBASE_PROJECT_ID");
            if ((projectId == null || projectId.isBlank()) && storageBucket != null && storageBucket.contains(".")) {
                // "myproject.appspot.com" → "myproject"
                projectId = storageBucket.split("\\.")[0];
                System.out.println("🔑 projectId aus Storage-Bucket extrahiert: " + projectId);
            }
            if (projectId == null || projectId.isBlank()) {
                System.err.println("⚠️ Kein projectId gefunden! Bitte FIREBASE_PROJECT_ID oder FIREBASE_STORAGE_BUCKET setzen.");
            } else {
                System.out.println("🔑 Firebase projectId: " + projectId);
            }

            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://foundbuddy.firebaseio.com");

            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId);
            }

            // ⚡ Force gRPC to use Java DNS resolver instead of native (fixes Render networking)
            System.setProperty("io.grpc.netty.shaded.io.netty.resolver.dns.defaultSearchDomains", "");
            System.setProperty("io.grpc.netty.useCustomNameResolver", "false");
            System.out.println("✅ gRPC DNS resolver override set");

            if (storageBucket != null && !storageBucket.isBlank()) {
                builder.setStorageBucket(storageBucket);
                System.out.println("🪣 Firebase Storage Bucket gesetzt: " + storageBucket);
            } else {
                System.out.println("⚠️ FIREBASE_STORAGE_BUCKET nicht gesetzt – Uploads funktionieren sonst nicht.");
            }

            FirebaseOptions options = builder.build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase erfolgreich initialisiert!");
            }
        } catch (Exception e) {
            System.err.println("❌ Firebase Initialisierung fehlgeschlagen: " + e.getMessage());
            e.printStackTrace();
            System.err.println("⚠️ App startet ohne Firebase – einige Features funktionieren nicht!");
        }
    }

    /**
     * Render pastes env var JSON with literal \n (two chars) instead of real newlines.
     * RSA private keys need REAL newlines inside the PEM block.
     * This method finds the private_key value and converts \\n → \n.
     */
    private static String fixPrivateKeyNewlines(String json) {
        // Find the private_key value (between quotes after "private_key":)
        int keyStart = json.indexOf("\"private_key\"");
        if (keyStart < 0) return json;

        int valueStart = json.indexOf('"', json.indexOf(':', keyStart) + 1);
        if (valueStart < 0) return json;
        valueStart++; // skip opening quote

        // Find end of value (closing quote not preceded by backslash)
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == '"' && json.charAt(valueEnd - 1) != '\\') break;
            valueEnd++;
        }

        String keyValue = json.substring(valueStart, valueEnd);

        // Only fix if it contains literal \n (not real newlines already)
        if (!keyValue.contains("\\n")) return json; // already fine

        String fixed = keyValue.replace("\\n", "\n");
        System.out.println("🔧 Fixed private_key newlines (was escaped, now real)");

        return json.substring(0, valueStart) + fixed + json.substring(valueEnd);
    }
}
