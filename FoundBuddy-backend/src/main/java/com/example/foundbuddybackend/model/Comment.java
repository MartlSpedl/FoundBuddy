package com.example.foundbuddybackend.model;

public class Comment {
    
    private String author;
    private String text;
    private Long timestamp;

    public Comment() {}

    public Comment(String author, String text, Long timestamp) {
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
