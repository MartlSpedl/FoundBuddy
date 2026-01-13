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
 * Lädt den Service‑Account entweder aus einer Umgebungsvariablen oder aus der Ressource.
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;

            // Service‑Account aus Umgebungsvariable lesen (kompletter JSON‑Inhalt)
            String firebaseJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

            if (firebaseJson != null && !firebaseJson.isBlank()) {
                System.out.println("🔑 FIREBASE_SERVICE_ACCOUNT_JSON gefunden, Länge: " + firebaseJson.length());
                serviceAccount = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
                System.out.println("🔑 Firebase Key aus Umgebungsvariable geladen");
            } else {
                System.out.println("⚠️ Keine Umgebungsvariable für den Firebase‑Key gesetzt – lade Schlüssel aus der Resource");
                // Fallback: Schlüssel als Ressource laden
                // Die Datei `firebase-key.json` muss unter src/main/resources liegen, damit sie im JAR enthalten ist.
                serviceAccount = new ClassPathResource("firebase-key.json").getInputStream();
                System.out.println("🔑 Firebase Key aus Resource geladen");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://foundbuddy.firebaseio.com")
                    .build();

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
