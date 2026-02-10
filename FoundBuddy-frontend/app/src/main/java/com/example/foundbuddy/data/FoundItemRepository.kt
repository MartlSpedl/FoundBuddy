package com.example.foundbuddy.data

import android.content.Context
import android.util.Log
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.model.AiSearchResult
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.StatusChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.foundbuddy.network.ItemDto
import com.example.foundbuddy.network.StatusChangeDto
import com.example.foundbuddy.network.UpdateStatusRequest

class FoundItemRepository(private val context: Context, private val api: FoundBuddyApi) {

    /**
     * Holt alle Items vom Backend und mappt sie auf das FoundItem-Modell fürs UI.
     */
    suspend fun getAll(): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getItems()
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "GET /api/items failed HTTP ${resp.code()} body=$err")
                return@withContext emptyList()
            }
            val items = resp.body().orEmpty()
            items.map { it.toFoundItem() }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "getAll error", e)
            emptyList()
        }
    }

    /**
     * Schickt ein neues Item per POST an das Backend.
     */
    suspend fun addItem(item: FoundItem) = withContext(Dispatchers.IO) {
        try {
            val dto = item.toItemDto()
            val resp = api.createItem(dto)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "POST /api/items failed HTTP ${resp.code()} body=$err")
            }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "addItem error", e)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            val resp = api.clearAll()
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "DELETE /api/items failed HTTP ${resp.code()} body=$err")
            }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "clearAll error", e)
        }
    }

    suspend fun markAsResolved(itemId: String) = withContext(Dispatchers.IO) {
        try {
            val resp = api.resolveItem(itemId)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "PUT /api/items/$itemId/resolve failed HTTP ${resp.code()} body=$err")
            }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "markAsResolved error", e)
        }
    }

    /**
     * Markiert ein Item als Favorit
     */
    suspend fun toggleFavorite(itemId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = api.toggleFavorite(itemId, userId)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "PUT /api/items/$itemId/favorite failed HTTP ${resp.code()} body=$err")
            }
            resp.isSuccessful
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "toggleFavorite error", e)
            false
        }
    }

    /**
     * Aktualisiert den Workflow-Status
     */
    suspend fun updateWorkflowStatus(
        itemId: String,
        newStatus: String,
        userId: String,
        username: String,
        comment: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val resp = api.updateWorkflowStatus(
                itemId,
                UpdateStatusRequest(
                    newStatus = newStatus,
                    userId = userId,
                    username = username,
                    comment = comment
                )
            )
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "PUT /api/items/$itemId/status failed HTTP ${resp.code()} body=$err")
            }
            resp.isSuccessful
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "updateWorkflowStatus error", e)
            false
        }
    }

    /**
     * Holt Favoriten für einen bestimmten User
     */
    suspend fun getFavorites(userId: String): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getUserFavorites(userId)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "GET /api/users/$userId/favorites failed HTTP ${resp.code()} body=$err")
                return@withContext emptyList()
            }
            resp.body().orEmpty().map { it.toFoundItem() }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "getFavorites error", e)
            emptyList()
        }
    }

    /**
     * Holt den Statusverlauf für ein Item
     */
    suspend fun getStatusHistory(itemId: String): List<StatusChange> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getStatusHistory(itemId)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "GET /api/items/$itemId/status-history failed HTTP ${resp.code()} body=$err")
                return@withContext emptyList()
            }
            resp.body().orEmpty().map { it.toStatusChange() }
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "getStatusHistory error", e)
            emptyList()
        }
    }

    /**
     * Mapping Backend -> UI-Modell.
     */
    private fun ItemDto.toFoundItem(): FoundItem {
        val mappedStatus = when (status?.uppercase()) {
            "FOUND" -> "Gefunden"
            "LOST"  -> "Verloren"
            else    -> status ?: "Gefunden"
        }

        // Mapping für StatusHistory
        val history = statusHistory?.map { dto ->
            StatusChange(
                userId = dto.userId ?: "",
                username = dto.username ?: "Unbekannt",
                oldStatus = dto.oldStatus ?: "",
                newStatus = dto.newStatus ?: "",
                timestamp = dto.timestamp ?: System.currentTimeMillis(),
                comment = dto.comment
            )
        } ?: emptyList()

        return FoundItem(
            id = id ?: java.util.UUID.randomUUID().toString(),
            title = title ?: "",
            description = description,
            imagePath = photoUri,
            status = mappedStatus,
            isResolved = false,
            uploaderName = "Unbekannt",
            likes = 0,
            likedByUser = false,
            timestamp = timestamp ?: System.currentTimeMillis(),
            // Sprint 5: Neue Felder
            workflowStatus = workflowStatus ?: "Gemeldet",
            isFavorite = isFavorite ?: false,
            statusHistory = history,
            allowedEditors = allowedEditors ?: emptyList()
        )
    }

    /**
     * Mapping UI-Modell -> Backend-DTO.
     */
    private fun FoundItem.toItemDto(): ItemDto {
        val backendStatus = when (status.lowercase()) {
            "gefunden" -> "FOUND"
            "verloren" -> "LOST"
            else       -> "FOUND"
        }

        // Mapping für StatusHistory
        val historyDto = statusHistory.map { change ->
            StatusChangeDto(
                userId = change.userId,
                username = change.username,
                oldStatus = change.oldStatus,
                newStatus = change.newStatus,
                timestamp = change.timestamp,
                comment = change.comment
            )
        }

        return ItemDto(
            id = id,
            title = title,
            description = description,
            status = backendStatus,
            timestamp = timestamp,
            photoUri = imagePath,
            // Sprint 5: Neue Felder
            workflowStatus = workflowStatus,
            isFavorite = isFavorite,
            statusHistory = historyDto,
            allowedEditors = allowedEditors
        )
    }

    private fun StatusChangeDto.toStatusChange(): StatusChange {
        return StatusChange(
            userId = this.userId ?: "",
            username = this.username ?: "Unbekannt",
            oldStatus = this.oldStatus ?: "",
            newStatus = this.newStatus ?: "",
            timestamp = this.timestamp ?: System.currentTimeMillis(),
            comment = this.comment
        )
    }

    /**
     * Lädt ein Bild hoch und gibt die URL zurück
     */
    suspend fun uploadImageAndGetUrl(imageUri: android.net.Uri): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: throw Exception("Konnte Bild nicht öffnen")
            val bytes = inputStream.use { it.readBytes() }
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val fileName = "image_${System.currentTimeMillis()}.${mimeType.substringAfter('/', "jpg")}"

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val resp = api.uploadImage(part)
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "POST /api/images failed HTTP ${resp.code()} body=$err")
                return@withContext "https://via.placeholder.com/600x400"
            }
            val body = resp.body()
            body?.imageUrl ?: "https://via.placeholder.com/600x400"
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "uploadImageAndGetUrl error", e)
            "https://via.placeholder.com/600x400"
        }
    }

    /**
     * Erstellt ein neues FoundItem im Backend
     */
    suspend fun createFoundItem(item: FoundItem): FoundItem = withContext(Dispatchers.IO) {
        try {
            val resp = api.createItem(item.toItemDto())
            if (!resp.isSuccessful) {
                val err = resp.errorBody()?.string()
                Log.e("FoundItemRepository", "POST /api/items failed HTTP ${resp.code()} body=$err")
                throw IllegalStateException("Erstellen des Items fehlgeschlagen: HTTP ${resp.code()} ${err ?: ""}")
            }
            val body = resp.body() ?: throw IllegalStateException("Antwort leer")
            body.toFoundItem()
        } catch (e: Exception) {
            Log.e("FoundItemRepository", "createFoundItem error", e)
            throw e
        }
    }

    suspend fun aiSearch(query: String): List<AiSearchResult> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        val resp = api.aiSearch(mapOf("query" to q))
        if (!resp.isSuccessful) {
            val err = resp.errorBody()?.string()
            Log.e("FoundItemRepository", "POST /api/ai/search failed HTTP ${resp.code()} body=$err")
            throw IllegalStateException("AI-Suche fehlgeschlagen: HTTP ${resp.code()} ${err ?: ""}")
        }
        return resp.body() ?: emptyList()
    }

}