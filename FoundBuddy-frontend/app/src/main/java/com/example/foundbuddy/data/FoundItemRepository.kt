package com.example.foundbuddy.data

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class FoundItemApiRepository(
    private val context: Context,
    private val api: ApiService = ApiClient.api
) {

    suspend fun uploadImageAndGetUrl(imageUri: Uri): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Kann Bild nicht lesen")

        val mime = context.contentResolver.getType(imageUri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())

        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "upload.jpg",
            body = requestBody
        )

        val resp = api.uploadImage(part)
        if (!resp.isSuccessful) {
            throw IllegalStateException("Upload fehlgeschlagen: HTTP ${resp.code()}")
        }

        val body = resp.body() ?: throw IllegalStateException("Upload Antwort ist leer")
        if (body.imageUrl.isBlank()) throw IllegalStateException("Upload hat keine imageUrl geliefert")

        return body.imageUrl
    }

    suspend fun createFoundItem(item: com.example.foundbuddy.model.FoundItem): com.example.foundbuddy.model.FoundItem {
        val resp = api.createFoundItem(item)
        if (!resp.isSuccessful) {
            throw IllegalStateException("Item create fehlgeschlagen: HTTP ${resp.code()}")
        }
        return resp.body() ?: throw IllegalStateException("Create Antwort ist leer")
    }
}
