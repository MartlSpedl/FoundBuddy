package com.example.foundbuddybackend.model;

import com.google.cloud.firestore.annotation.DocumentId;

public class Conversation {

    @DocumentId
    private String id;
    private String participantId;
    private String participantName;
    private Message lastMessage;
    private boolean isAccepted;

    public Conversation() {}

    public Conversation(String participantId, String participantName, Message lastMessage, boolean isAccepted) {
        this.participantId = participantId;
        this.participantName = participantName;
        this.lastMessage = lastMessage;
        this.isAccepted = isAccepted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public Message getLastMessage() { return lastMessage; }
    public void setLastMessage(Message lastMessage) { this.lastMessage = lastMessage; }

    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { isAccepted = accepted; }

    // Required for Firebase bean mapping (is vs get)
    public boolean getIsAccepted() { return isAccepted; }
    public void setIsAccepted(boolean accepted) { isAccepted = accepted; }
}
