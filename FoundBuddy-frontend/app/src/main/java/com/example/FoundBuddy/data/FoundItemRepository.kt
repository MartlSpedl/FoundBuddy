package com.example.FoundBuddy.data

import android.content.Context
import com.example.FoundBuddy.model.FoundItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FoundItemRepository(private val context: Context) {

    // Wichtig: KotlinJsonAdapterFactory, sonst crasht Moshi bei Kotlin-Datenklassen
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, FoundItem::class.java)
    private val adapter = moshi.adapter<List<FoundItem>>(listType)
    private val file = File(context.filesDir, "found_items.json")

    suspend fun getAll(): List<FoundItem> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        val json = file.readText()
        adapter.fromJson(json) ?: emptyList()
    }

    suspend fun addItem(item: FoundItem) = withContext(Dispatchers.IO) {
        val all = getAll().toMutableList()
        all.add(0, item)
        file.writeText(adapter.toJson(all))
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        file.delete()
    }
}
