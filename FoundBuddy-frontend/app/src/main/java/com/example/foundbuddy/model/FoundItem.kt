package com.example.foundbuddy.model

import com.google.gson.annotations.SerializedName

data class FoundItem(
    val id: String,

    val title: String,

    val description: String? = null,

    // Backend liefert "imageUri", UI nutzt item.imagePath -> deshalb Mapping:
    @SerializedName("imageUri")
    val imagePath: String? = null,

    // "Gefunden" / "Verloren"
    val status: String = "",

    // Backend nutzt oft "resolved"
    @SerializedName("resolved")
    val isResolved: Boolean = false,

    val uploaderName: String = "Unbekannt",

    val likes: Int = 0,

    val likedByUser: Boolean = false,

    // Backend nutzt oft "createdAt"
    @SerializedName("createdAt")
    val timestamp: Long = System.currentTimeMillis()
)
