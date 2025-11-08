package com.example.foundbuddybackend.model;
/**
 * Entity representing a found item posted by a user.  A found item consists of
 * a title, description and an optional image URI.  The timestamp is stored
 * separately as {@code createdAt} in milliseconds since the epoch.
 */

public class FoundItem {
    private String id;
    private String title;
    private String description;

    /**
     * URI pointing to an image asset.  This can be a relative path on the
     * client, a data URI, or a link to an uploaded asset.  The backend
     * preserves the value without interpretation.
     */
    private String imageUri;

    /**
     * Creation timestamp in milliseconds since UNIX epoch.
     */
    private Long createdAt;

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
}