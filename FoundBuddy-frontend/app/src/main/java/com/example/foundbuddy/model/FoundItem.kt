package com.example.foundbuddy.model

data class FoundItem(
    val id: String = "",
    val title: String,
    val description: String? = null,
    val color: String? = null,
    val location: String? = null,
    val date: String? = null,
    val imagePath: String? = null,
    val status: String = "Gefunden" // Neu: "Gefunden" oder "Verloren"
)
