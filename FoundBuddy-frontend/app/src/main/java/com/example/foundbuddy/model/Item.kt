package com.example.foundbuddy.model

data class Item(
    val id: String,
    val title: String,
    val description: String,
    val status: ItemStatus,
    val timestamp: Long,
    val photoUri: String? = null
)

enum class ItemStatus { FOUND, LOST }
