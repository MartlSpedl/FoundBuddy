package com.example.foundbuddy.model

data class AiSearchResult(
    val item: FoundItem,
    val score: Double,
    val clipScore: Double,
    val textScore: Double,
    val recencyScore: Double
)
