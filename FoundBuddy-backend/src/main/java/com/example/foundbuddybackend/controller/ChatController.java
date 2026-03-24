package com.example.foundbuddybackend.controller;

import com.example.foundbuddybackend.model.Conversation;
import com.example.foundbuddybackend.model.Message;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*")
public class ChatController {

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    private String getChatId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }

    /**
     * Gets all conversations for a specific user (both accepted and pending requests).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("users").document(userId).collection("conversations").get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<Conversation> conversations = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                Conversation conv = doc.toObject(Conversation.class);
                conv.setId(doc.getId());
                conversations.add(conv);
            }

            // Sort by last message timestamp descending
            conversations.sort((a, b) -> {
                Long tsA = a.getLastMessage() != null ? a.getLastMessage().getTimestamp() : 0L;
                Long tsB = b.getLastMessage() != null ? b.getLastMessage().getTimestamp() : 0L;
                return tsB.compareTo(tsA);
            });

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gets all messages between two users.
     */
    @GetMapping("/messages/{userId}/{otherUserId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String userId, @PathVariable String otherUserId) {
        try {
            Firestore db = getFirestore();
            String chatId = getChatId(userId, otherUserId);
            // Default limit to 50 for performance
            ApiFuture<QuerySnapshot> future = db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            List<Message> messages = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                Message msg = doc.toObject(Message.class);
                msg.setId(doc.getId());
                messages.add(msg);
            }

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sends a new message and updates both users' conversation metadata.
     */
    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        try {
            if (message.getSenderId() == null || message.getRecipientId() == null) {
                return ResponseEntity.badRequest().build();
            }

            Firestore db = getFirestore();
            
            if (message.getId() == null || message.getId().isBlank()) {
                message.setId(UUID.randomUUID().toString());
            }
            if (message.getTimestamp() == null) {
                message.setTimestamp(Instant.now().toEpochMilli());
            }

            String chatId = getChatId(message.getSenderId(), message.getRecipientId());

            // 1. Save message to chat history
            db.collection("chats").document(chatId)
              .collection("messages").document(message.getId()).set(message).get();

            // 2. Update sender's conversation metadata
            DocumentReference senderConvRef = db.collection("users").document(message.getSenderId())
                    .collection("conversations").document(message.getRecipientId());
            DocumentSnapshot senderConvDoc = senderConvRef.get().get();
            
            Conversation senderConv = new Conversation(
                message.getRecipientId(), 
                message.getRecipientId(), // In a real app we'd fetch the recipient's name from DB if not known
                message, 
                true // Sender already accepts the chat
            );
            senderConvRef.set(senderConv);

            // 3. Update recipient's conversation metadata
            DocumentReference recipientConvRef = db.collection("users").document(message.getRecipientId())
                    .collection("conversations").document(message.getSenderId());
            DocumentSnapshot recipientConvDoc = recipientConvRef.get().get();
            
            boolean isAccepted = false;
            // If recipient already accepted it before, keep it accepted
            if (recipientConvDoc.exists()) {
                Conversation existing = recipientConvDoc.toObject(Conversation.class);
                if (existing != null && existing.isAccepted()) {
                    isAccepted = true;
                }
            }
            
            Conversation recipientConv = new Conversation(
                message.getSenderId(), 
                message.getSenderName(),
                message, 
                isAccepted
            );
            recipientConvRef.set(recipientConv);

            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Accepts a message request.
     */
    @PutMapping("/{userId}/accept/{otherUserId}")
    public ResponseEntity<Void> acceptRequest(@PathVariable String userId, @PathVariable String otherUserId) {
        try {
            Firestore db = getFirestore();
            DocumentReference ref = db.collection("users").document(userId)
                    .collection("conversations").document(otherUserId);
            
            DocumentSnapshot doc = ref.get().get();
            if (doc.exists()) {
                ref.update("isAccepted", true, "accepted", true).get();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Declines (deletes) a message request completely.
     */
    @PutMapping("/{userId}/decline/{otherUserId}")
    public ResponseEntity<Void> declineRequest(@PathVariable String userId, @PathVariable String otherUserId) {
        try {
            Firestore db = getFirestore();
            DocumentReference ref = db.collection("users").document(userId)
                    .collection("conversations").document(otherUserId);
            ref.delete().get();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
