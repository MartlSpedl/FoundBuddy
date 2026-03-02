package com.example.foundbuddy.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoundItem(
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("imageUri") val imagePath: String?,
    val status: String,          // "Gefunden" oder "Verloren"
    val isResolved: Boolean,
    val uploaderName: String = "Unbekannt",
    val likes: Int = 0,
    val likedByUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    // Sprint 5: Neue Felder
    val workflowStatus: String = "Gemeldet", // Gemeldet, In Kontakt, Abgeschlossen
    val isFavorite: Boolean = false,
    val statusHistory: List<StatusChange> = emptyList(),
    val allowedEditors: List<String> = emptyList() // User-IDs die Status ändern dürfen
)

// Neues Modell für Statusverlauf
@JsonClass(generateAdapter = true)
data class StatusChange(
    val userId: String,
    val username: String,
    val oldStatus: String,
    val newStatus: String,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String? = null
)