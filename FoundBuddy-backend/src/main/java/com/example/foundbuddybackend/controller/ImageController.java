package com.example.foundbuddybackend.controller;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.firebase.cloud.StorageClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Handles image uploads to Firebase Storage.
 * POST /api/images  (multipart/form-data, field name "file")
 * → returns { "imageUrl": "https://storage.googleapis.com/..." }
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
            }

            // Detect content type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            // Generate unique file name
            String extension = contentType.split("/")[1]; // jpeg, png, webp …
            String fileName = "uploads/" + UUID.randomUUID() + "." + extension;

            // Upload to Firebase Storage
            var bucket = StorageClient.getInstance().bucket();
            bucket.create(fileName, file.getBytes(), contentType);

            // Build public download URL
            String bucketName = bucket.getName();
            String encodedPath = fileName.replace("/", "%2F");
            String imageUrl = "https://firebasestorage.googleapis.com/v0/b/"
                    + bucketName + "/o/" + encodedPath + "?alt=media";

            System.out.println("✅ Image uploaded: " + imageUrl);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
