package com.example.foundbuddy.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val content: String,
    val referencedItemId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Conversation(
    val participantId: String,
    val participantName: String,
    val participantProfileImage: String? = null,
    val lastMessage: Message,
    val isAccepted: Boolean = true
)
