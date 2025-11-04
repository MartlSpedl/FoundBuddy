package com.example.FoundBuddy.model

import java.util.UUID

data class FoundItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val imageUri: String,
    val createdAt: Long = System.currentTimeMillis()
)
