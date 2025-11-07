package com.example.foundbuddybackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes Firebase on application startup.
 * Make sure the path to your service account JSON key is correct.
 */
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        // Pfad zu deiner Firebase-Schlüsseldatei
        // (relativ oder absolut)
        FileInputStream serviceAccount =
                new FileInputStream("firebase-key.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://foundbuddy.firebaseio.com") // Firestore-URL oder Realtime-DB
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase erfolgreich initialisiert!");
        }
    }
}
