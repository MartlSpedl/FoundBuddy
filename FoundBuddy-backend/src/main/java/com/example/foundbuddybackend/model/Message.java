package com.example.foundbuddybackend.model;

import com.google.cloud.firestore.annotation.DocumentId;

public class Message {

    @DocumentId
    private String id;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String content;
    private Long timestamp;

    public Message() {}

    public Message(String senderId, String senderName, String recipientId, String content, Long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
