package com.example.foundbuddy.network

import com.example.foundbuddy.data.UploadImageResponse
import com.example.foundbuddy.model.FoundItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface FoundBuddyApi {

    @GET("health")
    suspend fun health(): retrofit2.Response<String>

    @GET("api/found-items")
    suspend fun getFoundItems(): Response<List<FoundItem>>

    // ✅ Backend erwartet FoundItem-Form (imageUri, createdAt, resolved)
    @POST("api/found-items")
    suspend fun createFoundItem(@Body body: FoundItemCreateRequest): Response<FoundItem>

    @Multipart
    @POST("api/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<UploadImageResponse>
}

/**
 * DTO passend zu deinem Backend FoundItemController:
 * - imageUri statt imagePath
 * - createdAt statt timestamp
 * - resolved statt isResolved
 *
 * title/description sind safe. Alles andere ignoriert Backend (oder existiert dort nicht).
 */
data class FoundItemCreateRequest(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val imageUri: String? = null,
    val createdAt: Long? = null,
    val resolved: Boolean = false
)
