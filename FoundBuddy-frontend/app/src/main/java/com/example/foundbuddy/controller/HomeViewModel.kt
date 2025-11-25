package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.Comment
import com.example.foundbuddy.model.FoundItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<FoundItem>>(emptyList())
    val items: StateFlow<List<FoundItem>> = _items

    // Wird aus MainActivity aufgerufen, wenn aus Repository geladen wurde
    fun refreshItems(newItems: List<FoundItem>) {
        _items.value = newItems
    }

    private val commentMap = mutableMapOf<String, MutableStateFlow<List<Comment>>>()

    fun getComments(itemId: String): StateFlow<List<Comment>> =
        commentMap.getOrPut(itemId) { MutableStateFlow(emptyList()) }

    fun addComment(itemId: String, text: String, author: String = "User") {
        val flow = commentMap.getOrPut(itemId) { MutableStateFlow(emptyList()) }
        val newList = flow.value + Comment(author, text, System.currentTimeMillis())
        flow.value = newList
    }

    fun toggleLike(itemId: String) {
        val list = _items.value.toMutableList()
        val index = list.indexOfFirst { it.id == itemId }
        if (index == -1) return

        val item = list[index]
        list[index] = if (item.likedByUser) {
            item.copy(likedByUser = false, likes = item.likes - 1)
        } else {
            item.copy(likedByUser = true, likes = item.likes + 1)
        }
        _items.value = list
    }

    fun getItemById(id: String): FoundItem? =
        _items.value.firstOrNull { it.id == id }

    fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            seconds < 60 -> "vor ${seconds}s"
            minutes < 60 -> "vor ${minutes}min"
            hours < 24 -> "vor ${hours}h"
            else -> "vor ${days}d"
        }
    }
}
