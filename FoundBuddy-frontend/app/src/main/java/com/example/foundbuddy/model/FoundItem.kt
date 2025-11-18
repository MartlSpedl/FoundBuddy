package com.example.foundbuddy.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class FoundItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val color: String? = null,
    val location: String? = null,
    val date: String? = null,
    val imagePath: String? = null,
    val status: String = "Gefunden", // oder "Verloren"
    val isResolved: Boolean = false // NEU: zurückgegeben / zurückbekommen
)

