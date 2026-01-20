package com.example.foundbuddy.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FoundItemRepository(app.applicationContext)

    private val _items = MutableStateFlow<List<FoundItem>>(emptyList())
    val items: StateFlow<List<FoundItem>> = _items.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Comments storage - in a real app this would be in a repository/database
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _items.value = repo.getAll()
                _errorMessage.value = null
            } catch (e: Exception) {
                // Wichtig: NICHT crashen lassen
                val errorMsg = if (e.message?.contains("Unable to resolve host") == true 
                    || e.message?.contains("Unknown host") == true
                    || e.message?.contains("Network") == true) {
                    "Keine Internetverbindung. Bitte überprüfe deine Verbindung."
                } else {
                    e.message ?: "Unbekannter Fehler beim Laden"
                }
                _errorMessage.value = errorMsg
                // Keep existing items if available, don't set to empty
                if (_items.value.isEmpty()) {
                    _items.value = emptyList()
                }
            }
        }
    }

    fun toggleLike(itemId: String) {
        // Wenn du ein Like-Endpoint hast: hier callen.
        // Sonst (zumindest) UI-seitig togglen:
        val current = _items.value
        val updated = current.map { item ->
            if (item.id != itemId) item
            else item.copy(
                likedByUser = !item.likedByUser,
                likes = if (!item.likedByUser) item.likes + 1 else (item.likes - 1).coerceAtLeast(0)
            )
        }
        _items.value = updated
    }

    fun getItemById(itemId: String): FoundItem? {
        return _items.value.find { it.id == itemId }
    }

    fun addComment(itemId: String, text: String) {
        viewModelScope.launch {
            val currentComments = _comments.value[itemId] ?: emptyList()
            val newComment = Comment(
                author = "Anonymous", // In a real app, this would be the current user
                text = text,
                timestamp = System.currentTimeMillis()
            )
            val updatedComments = currentComments + newComment
            val updatedMap = _comments.value.toMutableMap()
            updatedMap[itemId] = updatedComments
            _comments.value = updatedMap
        }
    }

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            minutes < 1 -> "gerade eben"
            minutes < 60 -> "$minutes Minute${if (minutes != 1L) "n" else ""} her"
            hours < 24 -> "$hours Stunde${if (hours != 1L) "n" else ""} her"
            days < 7 -> "$days Tag${if (days != 1L) "e" else ""} her"
            else -> "vor langer Zeit"
        }
    }
}
