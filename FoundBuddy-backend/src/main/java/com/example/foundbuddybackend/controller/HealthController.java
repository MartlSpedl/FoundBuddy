package com.example.foundbuddybackend.controller;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
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
     * Debug endpoint: shows Firebase init state + Firestore reachability.
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
                info.put("databaseUrl", opts.getDatabaseUrl());
                info.put("storageBucket", opts.getStorageBucket());

                // Quick Firestore reachability check (5s timeout)
                try {
                    Firestore db = FirestoreClient.getFirestore();
                    info.put("firestoreInstance", db.getClass().getSimpleName());

                    var future = db.collection("_health").limit(1).get();
                    future.get(5, TimeUnit.SECONDS);
                    info.put("firestoreStatus", "OK");
                } catch (java.util.concurrent.TimeoutException e) {
                    info.put("firestoreStatus", "TIMEOUT (>5s)");
                    info.put("firestoreError", "Connection timed out - possible gRPC/network issue on Render");
                } catch (Exception e) {
                    info.put("firestoreStatus", "ERROR");
                    info.put("firestoreErrorClass", e.getClass().getName());
                    info.put("firestoreErrorMessage", e.getMessage());
                    if (e.getCause() != null) {
                        info.put("firestoreCause", e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
                    }
                    // Full stack trace for diagnosis
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    info.put("stackTrace", sw.toString().substring(0, Math.min(sw.toString().length(), 1000)));
                }
            } else {
                info.put("firestoreStatus", "NO_FIREBASE_APP");
            }
        } catch (Exception e) {
            info.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return ResponseEntity.ok(info);
    }
}
