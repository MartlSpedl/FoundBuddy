package com.example.foundbuddy.network

data class FoundItemCreateRequest(
    val id: String,
    val title: String,
    val description: String?,
    val imageUri: String?,
    val createdAt: Long,
    val resolved: Boolean,
    val status: String? // ✅ "Gefunden" oder "Verloren"
)
