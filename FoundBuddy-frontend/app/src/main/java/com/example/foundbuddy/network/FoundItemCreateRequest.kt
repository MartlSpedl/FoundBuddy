package com.example.foundbuddy.network

data class FoundItemCreateRequest(
    val id: String,
    val title: String,
    val description: String?,
    val imageUri: String?,   // WICHTIG: Backend erwartet imageUri
    val createdAt: Long,     // WICHTIG: Backend erwartet createdAt
    val resolved: Boolean
)
