package com.example.foundbuddy.data

import android.content.Context
import android.util.Log
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.model.AiSearchResult
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.StatusChange
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

class FoundItemRepository(private val context: Context, private val api: FoundBuddyApi) {

    private val baseUrl: String = "https://martlspedl-foundbuddy-backend.hf.space"
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // JSON-Adapter für die Backend-DTOs
    private val itemDtoType = Types.newParameterizedType(List::class.java, ItemDto::class.java)
    private val listAdapter = moshi.adapter<List<ItemDto>>(itemDtoType)
    private val itemAdapter = moshi.adapter(ItemDto::class.java)
    private val statusChangeListType = Types.newParameterizedType(List::class.java, StatusChangeDto::class.java)
    private val statusChangeListAdapter = moshi.adapter<List<StatusChangeDto>>(statusChangeListType)

    /**
     * Holt alle Items vom Backend und mappt sie auf das FoundItem-Modell fürs UI.
     */
    suspend fun getAll(): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/found-items")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000

            val responseCode = connection.responseCode
            if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: ""
                Log.w("FoundItemRepository", "getAll() HTTP $responseCode: $errorBody")
                return@withContext emptyList()
            }

            connection.inputStream.use { input ->
                val body = input.bufferedReader().use { it.readText() }
                val dtoList = listAdapter.fromJson(body) ?: emptyList()
                dtoList.map { it.toFoundItem() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // bei Fehler: leere Liste zurückgeben
        }
    }

    /**
     * Schickt ein neues Item per POST an das Backend.
     */
    suspend fun addItem(item: FoundItem) = withContext(Dispatchers.IO) {
        try {
            val dto = item.toItemDto()
            val json = itemAdapter.toJson(dto)

            val url = java.net.URL("$baseUrl/api/found-items")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000

            connection.outputStream.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }

            // Antwort lesen, damit die Anfrage sauber abgeschlossen wird
            connection.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/found-items")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAsResolved(itemId: String) = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/found-items/$itemId/resolve")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "PUT"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Markiert ein Item als Favorit
     */
    suspend fun toggleFavorite(itemId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val encodedUserId = java.net.URLEncoder.encode(userId, "UTF-8")
            val url = java.net.URL("$baseUrl/api/items/$itemId/favorite?userId=$encodedUserId")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "PUT"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            connection.inputStream.use { it.readBytes() }
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
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
            val url = java.net.URL("$baseUrl/api/items/$itemId/status")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val requestBody = """
                {
                    "newStatus": "$newStatus",
                    "userId": "$userId",
                    "username": "$username",
                    "comment": ${if (comment != null) "\"$comment\"" else "null"}
                }
            """.trimIndent()

            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            connection.inputStream.use { it.readBytes() }
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Holt Favoriten für einen bestimmten User
     */
    suspend fun getFavorites(userId: String): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            val encodedUserId = java.net.URLEncoder.encode(userId, "UTF-8")
            val url = java.net.URL("$baseUrl/api/users/$encodedUserId/favorites")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            connection.inputStream.use { input ->
                val body = input.bufferedReader().use { it.readText() }
                val dtoList = listAdapter.fromJson(body) ?: emptyList()
                dtoList.map { it.toFoundItem() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Holt den Statusverlauf für ein Item
     */
    suspend fun getStatusHistory(itemId: String): List<StatusChange> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/items/$itemId/status-history")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            connection.inputStream.use { input ->
                val body = input.bufferedReader().use { it.readText() }
                val dtoList = statusChangeListAdapter.fromJson(body) ?: emptyList()
                dtoList.map { it.toStatusChange() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Mapping Backend -> UI-Modell.
     */
    private fun ItemDto.toFoundItem(): FoundItem {
        // Backend now stores status in German directly ("Gefunden"/"Verloren")
        // Still handle English values for backward compatibility
        val mappedStatus = when (status?.uppercase()) {
            "FOUND" -> "Gefunden"
            "LOST"  -> "Verloren"
            else    -> status ?: "Gefunden"
        }

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
            imagePath = imageUri,   // ← was photoUri, backend field is imageUri
            status = mappedStatus,
            isResolved = isResolved ?: false,
            uploaderName = uploaderName ?: "Unbekannt",
            uploaderId = uploaderId ?: "",
            likes = likes ?: 0,
            likedByUser = false,
            timestamp = timestamp ?: System.currentTimeMillis(),
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
        // Backend now stores status in German directly
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
            status = status,         // already German: "Gefunden" or "Verloren"
            timestamp = timestamp,
            imageUri = imagePath,    // ← was photoUri = imagePath
            uploaderName = uploaderName,
            uploaderId = uploaderId,
            isResolved = isResolved,
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
            println("LOGCAT: ===== START IMAGE UPLOAD =====")
            println("LOGCAT: URI: $imageUri")
            
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: throw Exception("Konnte Bild nicht öffnen")
            
            // Lese die Bilddaten
            val bytes = inputStream.use { it.readBytes() }
            println("LOGCAT: ${bytes.size} Bytes gelesen")
            
            // Bestimme MIME-Type und Dateinamen
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val extension = mimeType.substringAfterLast("/", missingDelimiterValue = "jpeg")
            val fileName = "image_${System.currentTimeMillis()}.$extension"
            println("LOGCAT: MIME-Type: $mimeType, Filename: $fileName")
            
            // Erstelle Multipart-Request für Backend-Upload
            val boundary = "----${System.currentTimeMillis()}"
            val uploadUrl = "$baseUrl/api/images"
            println("LOGCAT: Upload URL: $uploadUrl")
            
            val url = java.net.URL(uploadUrl)
            val conn = url.openConnection() as HttpURLConnection
            
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 30_000
            conn.readTimeout = 30_000
            
            println("LOGCAT: Sende Request...")
            
            // Baue Multipart-Body
            val outputStream = conn.outputStream
            val writer = outputStream.writer()
            
            // File part
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
            writer.append("Content-Type: $mimeType\r\n")
            writer.append("\r\n")
            writer.flush()
            
            println("LOGCAT: Schreibe ${bytes.size} Bytes...")
            outputStream.write(bytes)
            outputStream.flush()
            
            writer.append("\r\n")
            writer.append("--$boundary--\r\n")
            writer.flush()
            writer.close()
            
            val responseCode = conn.responseCode
            println("LOGCAT: Response Code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.use { it.bufferedReader().readText() }
                println("LOGCAT: Response Body: $response")
                
                // Parse JSON Response { "imageUrl": "..." }
                val json = org.json.JSONObject(response)
                val imageUrl = json.getString("imageUrl")
                
                // Validiere die URL
                if (!imageUrl.startsWith("http")) {
                    throw Exception("Ungültige Bild-URL erhalten: $imageUrl")
                }
                
                println("LOGCAT: ===== UPLOAD SUCCESS =====")
                println("LOGCAT: Firebase URL: $imageUrl")
                
                // Teste die URL (optional)
                try {
                    val testConn = java.net.URL(imageUrl).openConnection() as HttpURLConnection
                    testConn.requestMethod = "HEAD"
                    testConn.connectTimeout = 5_000
                    testConn.readTimeout = 5_000
                    val testResponseCode = testConn.responseCode
                    println("LOGCAT: URL-Test Response Code: $testResponseCode")
                    if (testResponseCode != HttpURLConnection.HTTP_OK) {
                        println("LOGCAT: WARNUNG: URL nicht erreichbar, aber trotzdem verwenden")
                    }
                } catch (e: Exception) {
                    println("LOGCAT: URL-Test fehlgeschlagen: ${e.message}")
                }
                
                return@withContext imageUrl
            } else {
                val errorResponse = conn.errorStream?.use { it.bufferedReader().readText() } ?: "Unknown error"
                println("LOGCAT: Error Response: $errorResponse")
                throw Exception("Upload failed: $responseCode - $errorResponse")
            }
            
        } catch (e: Exception) {
            println("LOGCAT: ===== UPLOAD ERROR =====")
            println("LOGCAT: Fehler: ${e.message}")
            e.printStackTrace()
            // Fallback auf Platzhalter
            val fallbackUrl = "https://via.placeholder.com/400x300/cccccc/666666?text=Bild+fehlgeschlagen"
            println("LOGCAT: Fallback URL: $fallbackUrl")
            println("LOGCAT: ===== END ERROR =====")
            return@withContext fallbackUrl
        }
    }

    /**
     * Erstellt ein neues FoundItem im Backend
     */
    suspend fun createFoundItem(item: FoundItem): FoundItem = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/found-items")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000

            val json = itemAdapter.toJson(item.toItemDto())
            connection.outputStream.use { it.write(json.toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use {
                    val response = it.bufferedReader().readText()
                    val createdDto = itemAdapter.fromJson(response)
                    createdDto?.toFoundItem() ?: throw Exception("Fehler beim Parsen der Antwort")
                }
            } else {
                throw Exception("Server-Fehler: $responseCode")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erstellen des Items fehlgeschlagen: ${e.message}")
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

/**
 * DTO entsprechend dem Item-Model im Backend.
 */
@JsonClass(generateAdapter = true)
data class ItemDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,   // "Gefunden" oder "Verloren"
    val timestamp: Long? = null,
    val imageUri: String? = null, // matches backend FoundItem.imageUri
    val uploaderName: String? = null,
    val uploaderId: String? = null,
    val likes: Int? = 0,
    val isResolved: Boolean? = false,
    // Sprint 5: Neue Felder
    val workflowStatus: String? = "Gemeldet",
    val isFavorite: Boolean? = false,
    val statusHistory: List<StatusChangeDto>? = emptyList(),
    val allowedEditors: List<String>? = emptyList()
)

// DTO für Statusänderungen
@JsonClass(generateAdapter = true)
data class StatusChangeDto(
    val userId: String? = null,
    val username: String? = null,
    val oldStatus: String? = null,
    val newStatus: String? = null,
    val timestamp: Long? = null,
    val comment: String? = null
)