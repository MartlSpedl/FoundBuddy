package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.Comment
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.StatusChange
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<FoundItem>>(emptyList())
    val items: StateFlow<List<FoundItem>> = _items

    private val _favorites = MutableStateFlow<List<FoundItem>>(emptyList())
    val favorites: StateFlow<List<FoundItem>> = _favorites

    private var repo: FoundItemRepository? = null

    private val _searchResults = MutableStateFlow<List<FoundItem>>(emptyList())
    val searchResults: StateFlow<List<FoundItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setRepository(repo: FoundItemRepository) {
        this@HomeViewModel.repo = repo
    }

    // Wird aus MainActivity aufgerufen, wenn aus Repository geladen wurde
    fun refreshItems(newItems: List<FoundItem>) {
        _items.value = newItems
        // Favoriten aus den Items filtern
        _favorites.value = newItems.filter { it.isFavorite }
    }

    fun refreshFavorites(newFavorites: List<FoundItem>) {
        _favorites.value = newFavorites
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            repo?.getFavorites(userId)?.let { favorites ->
                _favorites.value = favorites
            }
        }
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

    // Sprint 5: Favoriten-Funktionalität
    fun toggleFavorite(itemId: String, currentUserId: String?) {
        if (currentUserId == null) return

        val list = _items.value.toMutableList()
        val index = list.indexOfFirst { it.id == itemId }
        if (index == -1) return

        val item = list[index]
        val isCurrentlyFavorite = item.isFavorite

        list[index] = item.copy(isFavorite = !isCurrentlyFavorite)
        _items.value = list

        // Favoriten-Liste aktualisieren
        val newFavorites = _items.value.filter { it.isFavorite }
        _favorites.value = newFavorites

        // Backend-Call
        viewModelScope.launch {
            repo?.toggleFavorite(itemId, currentUserId)
        }
    }

    // Sprint 5: Status-Update-Funktionalität
    fun updateWorkflowStatus(
        itemId: String,
        newStatus: String,
        userId: String,
        username: String,
        comment: String? = null
    ) {
        val list = _items.value.toMutableList()
        val index = list.indexOfFirst { it.id == itemId }
        if (index == -1) return

        val item = list[index]

        // Prüfen ob User berechtigt ist
        if (item.allowedEditors.isNotEmpty() && !item.allowedEditors.contains(userId)) {
            return // User nicht berechtigt
        }

        val statusChange = StatusChange(
            userId = userId,
            username = username,
            oldStatus = item.workflowStatus,
            newStatus = newStatus,
            comment = comment
        )

        val updatedHistory = item.statusHistory + statusChange
        list[index] = item.copy(
            workflowStatus = newStatus,
            statusHistory = updatedHistory
        )
        _items.value = list

        // Backend-Call
        viewModelScope.launch {
            repo?.updateWorkflowStatus(itemId, newStatus, userId, username, comment)
        }
    }

    fun loadStatusHistory(itemId: String) {
        viewModelScope.launch {
            repo?.getStatusHistory(itemId)?.let { history ->
                val list = _items.value.toMutableList()
                val index = list.indexOfFirst { it.id == itemId }
                if (index != -1) {
                    val item = list[index]
                    list[index] = item.copy(statusHistory = history)
                    _items.value = list
                }
            }
        }
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

    // Hilfsfunktion für Status-Farbe
    fun getStatusColor(status: String): Long = when (status) {
        "Gemeldet" -> 0xFF2196F3  // Blau
        "In Kontakt" -> 0xFFFF9800  // Orange
        "Abgeschlossen" -> 0xFF4CAF50  // Grün
        else -> 0xFF757575  // Grau
    }

    // Hilfsfunktion für nächsten möglichen Status
    fun getNextPossibleStatus(currentStatus: String): List<String> {
        return when (currentStatus) {
            "Gemeldet" -> listOf("In Kontakt", "Abgeschlossen")
            "In Kontakt" -> listOf("Abgeschlossen")
            else -> emptyList()
        }
    }

    fun searchAi(query: String) {
        searchJob?.cancel()

        val q = query.trim()
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                // kleines Debounce, damit nicht bei jedem Buchstaben ein Request rausgeht
                delay(350)

                val results = repo?.aiSearch(q)
                // wir zeigen nur die Items an (Scores könnten wir später auch anzeigen)
                _searchResults.value = results?.map { it.item } ?: emptyList()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Fehler bei der KI-Suche"
            } finally {
                _isSearching.value = false
            }
        }
    }

}