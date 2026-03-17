package com.example.foundbuddybackend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Speichert Bilder in Firebase Storage oder lokal als Fallback.
 * Fallback wird verwendet wenn Firebase Storage nicht konfiguriert ist.
 */
@Service
public class FirebaseStorageService {

    private static final String UPLOAD_DIR = "uploads";
    private final String baseUrl;

    public FirebaseStorageService() {
        // Base URL für lokale Bilder - Backend URL sollte bekannt sein
        // Für lokale Entwicklung: localhost:8080
        // Für Production: https://martlspedl-foundbuddy-backend.hf.space
        this.baseUrl = "https://martlspedl-foundbuddy-backend.hf.space";
    }

    /**
     * Laedt das Multipart-File hoch (Firebase oder lokaler Fallback).
     *
     * @return Download-URL
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        // Versuche zuerst Firebase Storage
        try {
            return uploadToFirebase(file);
        } catch (Exception e) {
            System.err.println("⚠️ Firebase Upload fehlgeschlagen, verwende lokalen Fallback: " + e.getMessage());
            return uploadToLocal(file);
        }
    }

    private String uploadToFirebase(MultipartFile file) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        if (bucket == null) {
            throw new IllegalStateException("Firebase Storage bucket is not configured");
        }

        String original = file.getOriginalFilename();
        String ext = guessExtension(original, file.getContentType());

        String objectName = "images/" + UUID.randomUUID() + ext;

        // Download Token setzen, damit die Datei ohne Auth abrufbar ist
        String token = UUID.randomUUID().toString();
        Map<String, String> metadata = Map.of(
                "firebaseStorageDownloadTokens", token
        );

        BlobInfo blobInfo = BlobInfo.newBuilder(bucket.getName(), objectName)
                .setContentType(file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .setMetadata(metadata)
                .build();

        Blob blob = bucket.getStorage().create(blobInfo, file.getBytes());
        String bucketName = bucket.getName();
        String encodedPath = URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8);
        return "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedPath + "?alt=media&token=" + token;
    }

    private String uploadToLocal(MultipartFile file) throws IOException {
        // Erstelle Upload-Verzeichnis falls nicht vorhanden
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String original = file.getOriginalFilename();
        String ext = guessExtension(original, file.getContentType());
        String fileName = UUID.randomUUID() + ext;
        
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        System.out.println("✅ Bild lokal gespeichert: " + filePath.toString());
        
        // Rückgabe URL für lokalen Zugriff
        return baseUrl + "/uploads/" + fileName;
    }

    private String guessExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf('.'));
            if (ext.length() <= 6) return ext;
        }
        if (contentType == null) return ".jpg";
        return switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
