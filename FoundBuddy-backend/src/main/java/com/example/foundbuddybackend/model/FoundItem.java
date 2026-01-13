package com.example.foundbuddybackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class FoundItem {

    private String id;
    private String title;
    private String description;
    private String imageUri;
    private Long createdAt;

    /**
     * KI-Embedding des Bildes (z. B. 512 Dimensionen)
     * Wird einmal beim Erstellen berechnet und in Firestore gespeichert
     */
    @JsonIgnore
    private List<Double> imageEmbedding;

    public FoundItem() {}

    public FoundItem(String title, String description, String imageUri, Long createdAt) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public List<Double> getImageEmbedding() {
        return imageEmbedding;
    }

    public void setImageEmbedding(List<Double> imageEmbedding) {
        this.imageEmbedding = imageEmbedding;
    }
}
