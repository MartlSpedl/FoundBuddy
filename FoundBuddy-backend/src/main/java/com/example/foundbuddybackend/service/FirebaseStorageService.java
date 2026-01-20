package com.example.foundbuddybackend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Speichert Bilder in Firebase Storage und erzeugt eine Download-URL,
 * die von jedem Client (Multi-User) geladen werden kann.
 */
@Service
public class FirebaseStorageService {

    /**
     * Laedt das Multipart-File in den konfigurierten Firebase Storage Bucket.
     *
     * @return Download-URL (alt=media & token)
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        Bucket bucket = StorageClient.getInstance().bucket();
        if (bucket == null) {
            throw new IllegalStateException("Firebase Storage bucket is not configured. Set FIREBASE_STORAGE_BUCKET.");
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
