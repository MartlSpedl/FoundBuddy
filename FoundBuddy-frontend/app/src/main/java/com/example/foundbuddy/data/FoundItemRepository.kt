package com.example.foundbuddy.data

import android.content.Context
import com.example.foundbuddy.model.FoundItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.File

class FoundItemRepository(private val context: Context) {

    private val file = File(context.filesDir, "found_items.json")
    private val moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, FoundItem::class.java)
    private val adapter = moshi.adapter<List<FoundItem>>(type)

    suspend fun getAll(): List<FoundItem> {
        if (!file.exists()) return emptyList()
        return file.readText().takeIf { it.isNotEmpty() }?.let {
            adapter.fromJson(it)
        } ?: emptyList()
    }

    suspend fun addItem(item: FoundItem) {
        val current = getAll().toMutableList()
        current.add(item)
        file.writeText(adapter.toJson(current))
    }

    suspend fun clearAll() {
        file.writeText("")
    }

    suspend fun markAsResolved(itemId: String) {
        val current = getAll().toMutableList()
        val updated = current.map {
            if (it.id == itemId) it.copy(isResolved = true) else it
        }
        file.writeText(adapter.toJson(updated))
    }
}

