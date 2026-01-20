package com.example.foundbuddy.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FoundItem(
    val id: String,
    val title: String,
    val description: String?,

    @Json(name = "imageUri")
    val imagePath: String?,

    // status existiert im Backend FoundItem vermutlich NICHT -> default geben, sonst Parsing-Fehler
    val status: String = "Gefunden",

    @Json(name = "resolved")
    val isResolved: Boolean = false,

    // diese Felder kommen vom Backend vermutlich nicht -> defaults sind wichtig
    val uploaderName: String = "Unbekannt",
    val likes: Int = 0,
    val likedByUser: Boolean = false,

    @Json(name = "createdAt")
    val timestamp: Long = System.currentTimeMillis()
)
