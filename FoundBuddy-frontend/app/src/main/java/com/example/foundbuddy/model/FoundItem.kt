package com.example.foundbuddy.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoundItem(
    val id: String,
    val title: String,
    val description: String?,
    val imagePath: String?,
    val status: String,          // "Gefunden" oder "Verloren"
    val isResolved: Boolean,
    val uploaderName: String = "Unbekannt",
    val likes: Int = 0,
    val likedByUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
