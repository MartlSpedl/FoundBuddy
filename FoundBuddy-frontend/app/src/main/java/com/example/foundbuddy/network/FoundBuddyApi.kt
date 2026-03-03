package com.example.foundbuddy.network

import com.example.foundbuddy.model.AiSearchResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FoundBuddyApi {

    @GET("api/health")
    suspend fun health(): Response<Map<String, Any>>

    // Items
    @GET("api/items")
    suspend fun getItems(): Response<List<ItemDto>>

    @POST("api/items")
    suspend fun createItem(@Body item: ItemDto): Response<ItemDto>

    @DELETE("api/items")
    suspend fun clearAll(): Response<Unit>

    @PUT("api/items/{itemId}/resolve")
    suspend fun resolveItem(@Path("itemId") itemId: String): Response<Unit>

    // Favorites & Status
    @PUT("api/items/{itemId}/favorite")
    suspend fun toggleFavorite(
        @Path("itemId") itemId: String,
        @Query("userId") userId: String
    ): Response<Unit>

    @PUT("api/items/{itemId}/status")
    suspend fun updateWorkflowStatus(
        @Path("itemId") itemId: String,
        @Body request: UpdateStatusRequest
    ): Response<Unit>

    @GET("api/users/{userId}/favorites")
    suspend fun getUserFavorites(
        @Path("userId") userId: String
    ): Response<List<ItemDto>>

    @GET("api/items/{itemId}/status-history")
    suspend fun getStatusHistory(
        @Path("itemId") itemId: String
    ): Response<List<StatusChangeDto>>

    // Image Upload
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ImageUploadResponse>

    // AI Search
    @POST("api/ai/search")
    suspend fun aiSearch(
        @Body body: Map<String, String>
    ): Response<List<AiSearchResult>>
}

data class UpdateStatusRequest(
    val newStatus: String,
    val userId: String,
    val username: String,
    val comment: String?
)

// Transport-DTOs entsprechend Backend
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ItemDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val timestamp: Long? = null,
    val photoUri: String? = null,
    val workflowStatus: String? = "Gemeldet",
    val isFavorite: Boolean? = false,
    val statusHistory: List<StatusChangeDto>? = emptyList(),
    val allowedEditors: List<String>? = emptyList()
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class StatusChangeDto(
    val userId: String? = null,
    val username: String? = null,
    val oldStatus: String? = null,
    val newStatus: String? = null,
    val timestamp: Long? = null,
    val comment: String? = null
)