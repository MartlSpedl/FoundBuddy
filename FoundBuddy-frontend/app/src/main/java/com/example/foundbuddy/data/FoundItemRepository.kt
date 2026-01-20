package com.example.foundbuddy.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.network.FoundItemCreateRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class FoundItemRepository(private val context: Context) {

    private val api: FoundBuddyApi =
        ApiClient.retrofit.create(FoundBuddyApi::class.java)

    suspend fun getAll(): List<FoundItem> {
        val resp = api.getFoundItems()
        if (!resp.isSuccessful) {
            val err = resp.errorBody()?.string()
            Log.e("FoundItemRepository", "GET /api/found-items failed HTTP ${resp.code()} body=$err")
            throw IllegalStateException("GET items fehlgeschlagen: HTTP ${resp.code()} ${err ?: ""}")
        }
        return resp.body() ?: emptyList()
    }

    /**
     * Lädt ein lokal ausgewähltes Bild (content://...) zum Backend hoch und gibt eine öffentliche http(s) URL zurück.
     * Diese URL muss anschließend als imagePath im FoundItem gespeichert werden.
     */
    suspend fun uploadImageAndGetUrl(imageUri: Uri): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Kann Bild nicht lesen: $imageUri")

        val mime = context.contentResolver.getType(imageUri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())

        // schönerer Dateiname (hilft beim Debug im Storage)
        val ext = when {
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val filename = "foundbuddy_${UUID.randomUUID()}.$ext"

        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = filename,
            body = requestBody
        )

        Log.d("FoundItemRepository", "POST /api/upload imageUri=$imageUri mime=$mime filename=$filename bytes=${bytes.size}")

        val resp = api.uploadImage(part)
        if (!resp.isSuccessful) {
            val err = resp.errorBody()?.string()
            Log.e("FoundItemRepository", "Upload failed HTTP ${resp.code()} body=$err")
            throw IllegalStateException("Upload fehlgeschlagen: HTTP ${resp.code()} ${err ?: ""}")
        }

        val body = resp.body() ?: throw IllegalStateException("Upload Antwort ist leer")
        val url = body.imageUrl.trim()

        if (url.isBlank()) throw IllegalStateException("Upload hat keine imageUrl geliefert (leer)")
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalStateException("Upload hat keine http(s) URL geliefert: $url")
        }

        Log.d("FoundItemRepository", "Upload ok -> imageUrl=$url")
        return url
    }

    /**
     * Erstellt ein FoundItem im Backend.
     * Wichtig: item.imagePath MUSS eine http(s) URL sein (aus uploadImageAndGetUrl),
     * NICHT content:// oder file://
     */
    suspend fun createFoundItem(item: com.example.foundbuddy.model.FoundItem): com.example.foundbuddy.model.FoundItem {

        val url: String = item.imagePath
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("imageUrl ist leer (Upload hat keine URL geliefert)")

        // Hard validation: Backend blockt content:// und file://
        if (url.startsWith("content://") || url.startsWith("file://")) {
            throw IllegalStateException("imageUrl ist lokal (${url.take(20)}...), Upload zu Firebase hat nicht stattgefunden")
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw IllegalStateException("imageUrl ist keine http(s) URL: $url")
        }

        val req = FoundItemCreateRequest(
            id = item.id,
            title = item.title,
            description = item.description,
            imageUri = url,                 // <- jetzt garantiert String (nicht nullable)
            createdAt = item.timestamp,
            resolved = item.isResolved
        )

        Log.d("FoundItemRepository", "POST /api/found-items -> $req")

        val resp = api.createFoundItem(req)

        if (!resp.isSuccessful) {
            val err = resp.errorBody()?.string()
            Log.e("FoundItemRepository", "POST failed HTTP ${resp.code()} body=$err")
            throw IllegalStateException("POST failed HTTP ${resp.code()} ${err ?: ""}")
        }

        return resp.body() ?: throw IllegalStateException("Create Antwort ist leer")
    }

}
