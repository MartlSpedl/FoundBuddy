package com.example.foundbuddy.model

data class User(
    val id: String = System.currentTimeMillis().toString(),
    val username: String,
    val email: String,
    val password: String,
    val profileImage: String? = null
)
