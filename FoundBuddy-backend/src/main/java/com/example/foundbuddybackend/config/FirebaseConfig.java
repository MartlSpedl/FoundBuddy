package com.example.foundbuddybackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Initializes Firebase on application startup.
 * Supports both local file and environment variable (for cloud deployment).
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;
            
            // Prüfe ob Umgebungsvariable gesetzt ist (für Cloud-Deployment)
            String firebaseKeyBase64 = System.getenv("FIREBASE_KEY_BASE64");
            
            if (firebaseKeyBase64 != null && !firebaseKeyBase64.isEmpty()) {
                System.out.println("🔑 FIREBASE_KEY_BASE64 gefunden, Länge: " + firebaseKeyBase64.length());
                // Decode Base64 und verwende als InputStream
                byte[] decoded = Base64.getDecoder().decode(firebaseKeyBase64);
                serviceAccount = new ByteArrayInputStream(decoded);
                System.out.println("🔑 Firebase Key aus Umgebungsvariable geladen (" + decoded.length + " bytes)");
            } else {
                System.out.println("⚠️ FIREBASE_KEY_BASE64 nicht gesetzt, versuche lokale Datei...");
                // Fallback: Lokale Datei verwenden
                serviceAccount = new FileInputStream("firebase-key.json");
                System.out.println("🔑 Firebase Key aus lokaler Datei geladen");
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
            // App trotzdem starten lassen (für Debugging)
            System.err.println("⚠️ App startet ohne Firebase - einige Features funktionieren nicht!");
        }
    }
}
