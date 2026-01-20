package com.example.foundbuddy.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageUploadResponse(
    val imageUrl: String
)
