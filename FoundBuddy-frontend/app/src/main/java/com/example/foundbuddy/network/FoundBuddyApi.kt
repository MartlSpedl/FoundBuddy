package com.example.foundbuddy.network

import com.example.foundbuddy.data.UploadImageResponse
import com.example.foundbuddy.model.FoundItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FoundBuddyApi {

    @GET("health")
    suspend fun health(): Response<String>

    // ✅ Items laden
    @GET("api/found-items")
    suspend fun getFoundItems(): Response<List<FoundItem>>

    // ✅ Item speichern
    @POST("api/found-items")
    suspend fun createFoundItem(@Body item: FoundItem): Response<FoundItem>

    // ✅ Bild Upload
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadImageResponse>
}
