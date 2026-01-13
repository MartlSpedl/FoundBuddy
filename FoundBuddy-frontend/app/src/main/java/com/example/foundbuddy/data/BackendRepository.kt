package com.example.foundbuddy.data

import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi

class BackendRepository {

    private val api: FoundBuddyApi =
        ApiClient.retrofit.create(FoundBuddyApi::class.java)

    suspend fun pingBackend(): Result<String> {
        return try {
            val res = api.health()
            if (res.isSuccessful) {
                Result.success(res.body() ?: "OK (empty body)")
            } else {
                Result.failure(IllegalStateException("HTTP ${res.code()}: ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
