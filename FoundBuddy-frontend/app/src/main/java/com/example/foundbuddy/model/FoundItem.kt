package com.example.foundbuddy.model

import com.google.gson.annotations.SerializedName

data class FoundItem(
    val id: String,
    val title: String,
    val description: String?,

    // Backend -> imageUri, App -> imagePath
    @SerializedName("imageUri")
    val imagePath: String?,

    val status: String? = null,

    // Backend -> resolved, App -> isResolved
    @SerializedName("resolved")
    val isResolved: Boolean = false,

    val uploaderName: String = "Unbekannt",
    val likes: Int = 0,
    val likedByUser: Boolean = false,

    // Backend -> createdAt, App -> timestamp
    @SerializedName("createdAt")
    val timestamp: Long = System.currentTimeMillis()
)
