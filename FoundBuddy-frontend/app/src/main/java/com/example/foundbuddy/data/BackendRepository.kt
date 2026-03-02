package com.example.foundbuddy.data

import com.example.foundbuddy.network.FoundBuddyApi

class BackendRepository(private val api: FoundBuddyApi) {

    suspend fun pingBackend(): Result<String> {
        return try {
            val res = api.health()
            if (res.isSuccessful) {
                val status = res.body()?.get("status")?.toString() ?: "ok"
                Result.success("Backend OK: $status")
            } else {
                Result.failure(IllegalStateException("HTTP ${res.code()}: ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
