package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.dto.UploadImageResponse;
import com.example.foundbuddybackend.service.FirebaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multi-User-tauglicher Bild-Upload.
 *
 * Frontend flow:
 * 1) POST /api/images (multipart "file") -> { imageUrl }
 * 2) imageUrl in Item.photoUri / FoundItem.imageUri speichern
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    private final FirebaseStorageService storage;

    public ImageUploadController(FirebaseStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        try {
            String url = storage.uploadImage(file);
            return ResponseEntity.ok(new UploadImageResponse(url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
            if (e.getCause() != null) msg += " | caused by: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("upload failed: " + msg);
        }
    }
}
