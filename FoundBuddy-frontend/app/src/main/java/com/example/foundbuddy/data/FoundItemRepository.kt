package com.example.foundbuddy.data

import android.content.Context
import com.example.foundbuddy.model.FoundItem
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoundItemRepository(private val context: Context) {

    /**
     * Base-URL des Backends.
     *
     * WICHTIG:
     * - Emulator (AVD): 10.0.2.2 statt localhost verwenden
     * - Physisches Gerät im WLAN: IP deines PCs, z. B. http://192.168.0.10:8080
     */
    // private val baseUrl: String = "http://10.0.2.2:8080"
    private val baseUrl: String = "http://10.139.211.167:8080"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // JSON-Adapter für die Backend-DTOs
    private val itemDtoType = Types.newParameterizedType(List::class.java, ItemDto::class.java)
    private val listAdapter = moshi.adapter<List<ItemDto>>(itemDtoType)
    private val itemAdapter = moshi.adapter(ItemDto::class.java)

    /**
     * Holt alle Items vom Backend und mappt sie auf das FoundItem-Modell fürs UI.
     */
    suspend fun getAll(): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/items")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000

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

            val url = java.net.URL("$baseUrl/api/items")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000

            connection.outputStream.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }

            // Antwort lesen, damit die Anfrage sauber abgeschlossen wird
            connection.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            // Bei Fehlern hier könntest du später ein Toast o.Ä. anzeigen
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL("$baseUrl/api/items")
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
            val url = java.net.URL("$baseUrl/api/items/$itemId/resolve")
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
     * Mapping Backend -> UI-Modell.
     */
    private fun ItemDto.toFoundItem(): FoundItem {
        val mappedStatus = when (status?.uppercase()) {
            "FOUND" -> "Gefunden"
            "LOST"  -> "Verloren"
            else    -> status ?: "Gefunden"
        }

        return FoundItem(
            id = id ?: java.util.UUID.randomUUID().toString(),
            title = title ?: "",
            description = description,
            imagePath = photoUri,
            status = mappedStatus,
            isResolved = false,          // Backend kennt das Feld noch nicht
            uploaderName = "Unbekannt",
            likes = 0,
            likedByUser = false,
            timestamp = timestamp ?: System.currentTimeMillis()
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

        return ItemDto(
            id = id,
            title = title,
            description = description,
            status = backendStatus,
            timestamp = timestamp,
            photoUri = imagePath
        )
    }
}

/**
 * DTO entsprechend dem Item-Model im Backend.
 * Muss außerhalb der Klasse stehen, damit Moshi-Codegen funktioniert.
 */
@JsonClass(generateAdapter = true)
data class ItemDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,   // "FOUND" oder "LOST"
    val timestamp: Long? = null,
    val photoUri: String? = null
)
