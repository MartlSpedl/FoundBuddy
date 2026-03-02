package com.example.foundbuddybackend.config;

import com.google.auth.oauth2.GoogleCredentials;
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
}
