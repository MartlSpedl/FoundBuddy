package com.example.foundbuddy.network

import okhttp3.Response


interface FoundBuddyApi {

    // Wichtig: Passe diesen Endpoint an dein Backend an!
    // Ich nehme /health als einfachen Test.

    @GET("health")
    suspend fun health(): Response<String>
}
