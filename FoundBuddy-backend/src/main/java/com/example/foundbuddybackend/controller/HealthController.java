package com.example.foundbuddybackend.controller;

import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Debug endpoint: shows Firebase init state + Firestore reachability (5s).
     * Use to diagnose FIREBASE_SERVICE_ACCOUNT_JSON issues on Render.
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

                // Quick Firestore reachability check (5s timeout)
                try {
                    var db = FirestoreClient.getFirestore();
                    var future = db.collection("_health").limit(1).get();
                    future.get(5, TimeUnit.SECONDS);
                    info.put("firestoreStatus", "OK");
                } catch (java.util.concurrent.TimeoutException e) {
                    info.put("firestoreStatus", "TIMEOUT (>5s) – Firestore unreachable from Render");
                } catch (Exception e) {
                    info.put("firestoreStatus", "ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            } else {
                info.put("firestoreStatus", "NO_FIREBASE_APP – check FIREBASE_SERVICE_ACCOUNT_JSON");
            }
        } catch (Exception e) {
            info.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return ResponseEntity.ok(info);
    }
}
