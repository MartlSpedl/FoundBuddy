package com.example.foundbuddy.network

import com.example.foundbuddy.model.AiSearchResult
import retrofit2.Response
import retrofit2.http.*

interface FoundBuddyApi {

    @GET("health")
    suspend fun health(): Response<String>

    // Sprint 5: Neue Endpoints
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
    ): Response<List<ItemResponse>>

    @GET("api/items/{itemId}/status-history")
    suspend fun getStatusHistory(
        @Path("itemId") itemId: String
    ): Response<List<StatusChangeResponse>>

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

data class ItemResponse(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val workflowStatus: String = "Gemeldet",
    val isFavorite: Boolean = false,
    val statusHistory: List<StatusChangeResponse> = emptyList(),
    val allowedEditors: List<String> = emptyList()
)

data class StatusChangeResponse(
    val userId: String,
    val username: String,
    val oldStatus: String,
    val newStatus: String,
    val timestamp: Long,
    val comment: String?
)