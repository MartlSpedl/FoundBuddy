package com.example.foundbuddybackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FoundItem {

    private String id;
    private String title;
    private String description;
    private String imageUri;

    // Frontend expects "timestamp", Firestore has "createdAt"
    // We store as "createdAt" in Firestore, serialise as both for compatibility.
    private Long createdAt;

    private String status = "Gefunden";        // "Gefunden" | "Verloren"
    private boolean isResolved = false;
    private String uploaderName = "Unbekannt";
    private int likes = 0;
    private boolean likedByUser = false;
    private String workflowStatus = "Gemeldet"; // Gemeldet | In Kontakt | Abgeschlossen
    private boolean isFavorite = false;
    private List<Object> statusHistory = List.of();
    private List<String> allowedEditors = List.of();

    /**
     * KI-Embedding des Bildes (z. B. 512 Dimensionen)
     * Wird einmal beim Erstellen berechnet und in Firestore gespeichert.
     */
    @JsonIgnore
    private List<Double> imageEmbedding;

    @JsonIgnore
    private List<Double> textEmbedding;

    public FoundItem() {}

    public FoundItem(String title, String description, String imageUri, Long createdAt) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.createdAt = createdAt;
    }

    // ── id ──────────────────────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // ── title ────────────────────────────────────────────────────────────────
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // ── description ──────────────────────────────────────────────────────────
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ── imageUri ─────────────────────────────────────────────────────────────
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    // ── createdAt / timestamp ────────────────────────────────────────────────
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    /** Alias so the frontend field "timestamp" is also present in JSON */
    public Long getTimestamp() { return createdAt; }

    // ── status ───────────────────────────────────────────────────────────────
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ── isResolved ───────────────────────────────────────────────────────────
    @JsonProperty("isResolved")
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    // ── uploaderName ─────────────────────────────────────────────────────────
    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    // ── likes ─────────────────────────────────────────────────────────────────
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    // ── likedByUser ───────────────────────────────────────────────────────────
    public boolean isLikedByUser() { return likedByUser; }
    public void setLikedByUser(boolean likedByUser) { this.likedByUser = likedByUser; }

    // ── workflowStatus ────────────────────────────────────────────────────────
    public String getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(String workflowStatus) { this.workflowStatus = workflowStatus; }

    // ── isFavorite ────────────────────────────────────────────────────────────
    @JsonProperty("isFavorite")
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // ── statusHistory ─────────────────────────────────────────────────────────
    public List<Object> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<Object> statusHistory) { this.statusHistory = statusHistory; }

    // ── allowedEditors ────────────────────────────────────────────────────────
    public List<String> getAllowedEditors() { return allowedEditors; }
    public void setAllowedEditors(List<String> allowedEditors) { this.allowedEditors = allowedEditors; }

    // ── embeddings ────────────────────────────────────────────────────────────
    @JsonIgnore
    public List<Double> getImageEmbedding() { return imageEmbedding; }
    public void setImageEmbedding(List<Double> imageEmbedding) { this.imageEmbedding = imageEmbedding; }

    @JsonIgnore
    public List<Double> getTextEmbedding() { return textEmbedding; }
    public void setTextEmbedding(List<Double> textEmbedding) { this.textEmbedding = textEmbedding; }
}
