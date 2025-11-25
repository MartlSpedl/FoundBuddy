package com.example.foundbuddy.data

import android.content.Context
import com.example.foundbuddy.model.FoundItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FoundItemRepository(private val context: Context) {

    private val file = File(context.filesDir, "found_items.json")
    private val moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, FoundItem::class.java)
    private val adapter = moshi.adapter<List<FoundItem>>(type)

    suspend fun getAll(): List<FoundItem> = withContext(Dispatchers.IO) {
        readAllFromDisk()
    }

    suspend fun addItem(item: FoundItem) = withContext(Dispatchers.IO) {
        val current = readAllFromDisk().toMutableList()
        current.add(item)
        writeAllToDisk(current)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        if (file.exists()) {
            file.writeText("")
        }
    }

    suspend fun markAsResolved(itemId: String) = withContext(Dispatchers.IO) {
        val current = readAllFromDisk()
        val updated = current.map {
            if (it.id == itemId) it.copy(isResolved = true) else it
        }
        writeAllToDisk(updated)
    }

    private fun readAllFromDisk(): List<FoundItem> {
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            if (json.isBlank()) emptyList()
            else adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun writeAllToDisk(items: List<FoundItem>) {
        try {
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            file.writeText(adapter.toJson(items))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
