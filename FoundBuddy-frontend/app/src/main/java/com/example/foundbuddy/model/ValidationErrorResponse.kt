package com.example.foundbuddy.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>
)
