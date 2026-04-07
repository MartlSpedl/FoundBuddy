package com.example.foundbuddy.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoundItem(
    val id: String,
    val title: String,
    val description: String?,
    @Json(name = "imageUri") val imagePath: String?,
    val status: String,
    @Json(name = "isResolved") val isResolved: Boolean,
    val uploaderName: String = "Unbekannt",
    val uploaderId: String = "",
    val likes: Int = 0,
    @Json(name = "likedByUser") val likedByUser: Boolean = false,
    @Json(name = "createdAt") val timestamp: Long = System.currentTimeMillis(),
    val workflowStatus: String = "Gemeldet",
    val favoritedBy: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val statusHistory: List<StatusChange> = emptyList(),
    val allowedEditors: List<String> = emptyList(),
    val comments: List<Comment> = emptyList()
)

// Neues Modell für Statusverlauf
@JsonClass(generateAdapter = true)
data class StatusChange(
    @Json(name = "userId") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "oldStatus") val oldStatus: String,
    @Json(name = "newStatus") val newStatus: String,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String? = null
)
