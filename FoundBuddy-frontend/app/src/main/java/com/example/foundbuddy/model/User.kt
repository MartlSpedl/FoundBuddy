package com.example.foundbuddy.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String = System.currentTimeMillis().toString(),
    val username: String,
    val email: String,
    val password: String,
    val profileImage: String? = null,
    val emailVerified: Boolean = false,
    val verificationToken: String? = null
)
