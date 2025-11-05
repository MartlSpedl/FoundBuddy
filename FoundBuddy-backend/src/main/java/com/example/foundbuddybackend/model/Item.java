package com.example.foundbuddybackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity representing either a lost or a found item reported by users.
 *
 * <p>
 * Unlike {@link FoundItem}, this class captures whether the item is lost or
 * found via the {@link ItemStatus} enum.  It also stores an optional
 * {@code photoUri} pointing to an image resource and a timestamp in
 * milliseconds.
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4096)
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    private Long timestamp;

    /**
     * Optional URI to a photo of the item.  May be {@code null}.
     */
    private String photoUri;

    public Item() {
        // Default constructor required by JPA
    }

    public Item(String title, String description, ItemStatus status, Long timestamp, String photoUri) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.photoUri = photoUri;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}