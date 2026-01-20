package com.example.foundbuddy.data

import com.example.foundbuddy.model.FoundItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ✅ 1) Bild hochladen (Multipart)
    @Multipart
    @POST("api/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ✅ 2) Item anlegen (du hast evtl schon eine Variante davon)
    @POST("api/found-items")
    suspend fun createFoundItem(
        @Body item: FoundItem
    ): Response<FoundItem>

    // Optional falls du Items lädst:
    @GET("api/found-items")
    suspend fun getFoundItems(): Response<List<FoundItem>>
}
