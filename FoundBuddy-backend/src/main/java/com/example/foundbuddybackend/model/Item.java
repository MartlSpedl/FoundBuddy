package com.example.foundbuddybackend.model;

/**
 * Entity representing either a lost or a found item reported by users.
 *
 * <p>
 * Unlike {@link FoundItem}, this class captures whether the item is lost or
 * found via the {@link ItemStatus} enum.  It also stores an optional
 * {@code photoUri} pointing to an image resource and a timestamp in
 * milliseconds.
 */
public class Item {
    private String id;
    private String title;
    private String description;
    private ItemStatus status;
    private Long timestamp;

    /**
     * Optional URI to a photo of the item.  May be {@code null}.
     */
    private String photoUri;

    public Item(String title, String description, ItemStatus status, Long timestamp, String photoUri) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.photoUri = photoUri;
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