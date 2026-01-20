package com.example.foundbuddy.network

import com.example.foundbuddy.data.UploadImageResponse
import com.example.foundbuddy.model.FoundItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FoundBuddyApi {

    // Health
    @GET("health")
    suspend fun health(): Response<String>

    // Items
    @GET("api/found-items")
    suspend fun getFoundItems(): Response<List<FoundItem>>

    @POST("api/found-items")
    suspend fun createFoundItem(
        @Body req: FoundItemCreateRequest
    ): Response<FoundItem>

    @PUT("api/found-items/{id}/resolve")
    suspend fun resolveFoundItem(
        @Path("id") id: String
    ): Response<FoundItem>

    // Image Upload (Backend liefert { "imageUrl": "https://..." })
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>
}
