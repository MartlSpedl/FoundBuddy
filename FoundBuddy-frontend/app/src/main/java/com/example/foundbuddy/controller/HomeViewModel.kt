package com.example.foundbuddy.controller

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.Comment
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.StatusChange
import com.example.foundbuddy.model.Conversation
import com.example.foundbuddy.model.Message
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableStateFlow<List<FoundItem>>(emptyList())
    val items: StateFlow<List<FoundItem>> = _items

    private val _favorites = MutableStateFlow<List<FoundItem>>(emptyList())
    val favorites: StateFlow<List<FoundItem>> = _favorites

    private var repo: FoundItemRepository? = null

    private val _searchResults = MutableStateFlow<List<FoundItem>>(emptyList())
    val searchResults: StateFlow<List<FoundItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- Sprint 6: Chat/DM System ---
    private val sharedPreferences = application.getSharedPreferences("foundbuddy_chat", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val convListAdapter = moshi.adapter<List<Conversation>>(Types.newParameterizedType(List::class.java, Conversation::class.java))
    private val msgListAdapter = moshi.adapter<List<Message>>(Types.newParameterizedType(List::class.java, Message::class.java))

    init {
        loadChatData()
    }
    private val _conversations = MutableStateFlow<List<com.example.foundbuddy.model.Conversation>>(emptyList())
    val conversations: StateFlow<List<com.example.foundbuddy.model.Conversation>> = _conversations.asStateFlow()

    private val conversationMessages = mutableMapOf<String, MutableStateFlow<List<com.example.foundbuddy.model.Message>>>()

    fun getMessages(participantId: String): StateFlow<List<com.example.foundbuddy.model.Message>> =
        conversationMessages.getOrPut(participantId) { MutableStateFlow(emptyList()) }.asStateFlow()


    // Message Requests (Anfragen)
    private val _messageRequests = MutableStateFlow<List<com.example.foundbuddy.model.Conversation>>(emptyList())
    val messageRequests: StateFlow<List<com.example.foundbuddy.model.Conversation>> = _messageRequests.asStateFlow()

    fun loadConversationsFromBackend(userId: String) {
        viewModelScope.launch {
            try {
                repo?.getUserConversations(userId)?.let { backendConversations ->
                    // Convert backend conversations to frontend model
                    val frontendConversations = backendConversations.map { backendConv ->
                        com.example.foundbuddy.model.Conversation(
                            participantId = backendConv.participantId,
                            participantName = backendConv.participantName,
                            lastMessage = com.example.foundbuddy.model.Message(
                                id = backendConv.lastMessage?.id ?: "",
                                senderId = backendConv.lastMessage?.senderId ?: "",
                                senderName = backendConv.lastMessage?.senderName ?: "",
                                recipientId = backendConv.lastMessage?.recipientId ?: "",
                                content = backendConv.lastMessage?.content ?: "",
                                timestamp = backendConv.lastMessage?.timestamp ?: System.currentTimeMillis()
                            ),
                            isAccepted = backendConv.isAccepted
                        )
                    }
                    
                    // Separate accepted conversations and requests
                    val accepted = frontendConversations.filter { it.isAccepted }
                    val requests = frontendConversations.filter { !it.isAccepted }
                    
                    _conversations.value = accepted
                    _messageRequests.value = requests
                    
                    saveChatData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMessagesFromBackend(userId: String, otherUserId: String) {
        viewModelScope.launch {
            try {
                repo?.getMessages(userId, otherUserId)?.let { backendMessages ->
                    val frontendMessages = backendMessages.map { backendMsg ->
                        com.example.foundbuddy.model.Message(
                            id = backendMsg.id ?: "",
                            senderId = backendMsg.senderId ?: "",
                            senderName = backendMsg.senderName ?: "",
                            recipientId = backendMsg.recipientId ?: "",
                            content = backendMsg.content ?: "",
                            timestamp = backendMsg.timestamp ?: System.currentTimeMillis()
                        )
                    }
                    
                    // Update the messages flow
                    val messagesFlow = conversationMessages.getOrPut(otherUserId) { MutableStateFlow(emptyList()) }
                    messagesFlow.value = frontendMessages
                    
                    saveChatData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptRequestFromBackend(userId: String, otherUserId: String) {
        viewModelScope.launch {
            try {
                val success = repo?.acceptRequest(userId, otherUserId) ?: false
                if (success) {
                    acceptRequest(otherUserId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun declineRequestFromBackend(userId: String, otherUserId: String) {
        viewModelScope.launch {
            try {
                val success = repo?.declineRequest(userId, otherUserId) ?: false
                if (success) {
                    declineRequest(otherUserId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptRequest(participantId: String) {
        val req = _messageRequests.value.find { it.participantId == participantId } ?: return
        _messageRequests.value = _messageRequests.value.filter { it.participantId != participantId }
        val current = _conversations.value.toMutableList()
        if (current.none { it.participantId == participantId }) current.add(0, req)
        _conversations.value = current
        saveChatData()
    }

    fun declineRequest(participantId: String) {
        _messageRequests.value = _messageRequests.value.filter { it.participantId != participantId }
        saveChatData()
    }
    fun setRepository(repo: FoundItemRepository) {
        this@HomeViewModel.repo = repo
    }

    // Wird aus MainActivity aufgerufen, wenn aus Repository geladen wurde
    fun refreshItems(newItems: List<FoundItem>) {
        _items.value = newItems
        // Favoriten aus den Items filtern
        _favorites.value = newItems.filter { it.isFavorite }
        _isLoading.value = false
    }

    fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repo?.getAll()?.let { newItems ->
                    refreshItems(newItems)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Laden: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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


    fun sendMessage(
        recipientId: String,
        recipientName: String,
        senderId: String,
        senderName: String,
        content: String
    ) {
        val newMessage = com.example.foundbuddy.model.Message(
            senderId = senderId,
            senderName = senderName,
            recipientId = recipientId,
            content = content
        )
        
        // Store messages for BOTH sender and recipient locally
        val recipientMessagesFlow = conversationMessages.getOrPut(recipientId) { MutableStateFlow(emptyList()) }
        recipientMessagesFlow.value = recipientMessagesFlow.value + newMessage
        
        val senderMessagesFlow = conversationMessages.getOrPut(senderId) { MutableStateFlow(emptyList()) }
        senderMessagesFlow.value = senderMessagesFlow.value + newMessage

        // Create conversation for the current user (sender)
        val currentConversations = _conversations.value.toMutableList()
        
        // Remove existing conversation with this participant if it exists
        val existingIndex = currentConversations.indexOfFirst { it.participantId == recipientId }
        if (existingIndex != -1) currentConversations.removeAt(existingIndex)
        
        // Add new conversation at the top
        val newConversation = com.example.foundbuddy.model.Conversation(
            participantId = recipientId,
            participantName = recipientName,
            lastMessage = newMessage
        )
        currentConversations.add(0, newConversation)
        
        _conversations.value = currentConversations

        // Save to backend
        viewModelScope.launch {
            try {
                repo?.sendMessage(newMessage)
            } catch (e: Exception) {
                // Handle error - maybe show a toast or error message
                e.printStackTrace()
            }
        }

        saveChatData()
    }
    private fun saveChatData() {
        val editor = sharedPreferences.edit()
        editor.putString("conversations", convListAdapter.toJson(_conversations.value))
        editor.putString("messageRequests", convListAdapter.toJson(_messageRequests.value))
        
        // Save individual message flows
        conversationMessages.forEach { (participantId, flow) ->
            editor.putString("messages_$participantId", msgListAdapter.toJson(flow.value))
        }
        editor.commit() // Use commit() instead of apply() for immediate saving
    }

    private fun loadChatData() {
        try {
            val convsJson = sharedPreferences.getString("conversations", null)
            if (convsJson != null) _conversations.value = convListAdapter.fromJson(convsJson) ?: emptyList()
            
            val reqsJson = sharedPreferences.getString("messageRequests", null)
            if (reqsJson != null) _messageRequests.value = convListAdapter.fromJson(reqsJson) ?: emptyList()
            
            // Get all possible participant IDs from conversations and message requests
            val conversationParticipants = _conversations.value.map { it.participantId }
            val requestParticipants = _messageRequests.value.map { it.participantId }
            
            // Also scan for any stored message keys to ensure we load all messages
            val allKeys = sharedPreferences.all.keys.filter { it.startsWith("messages_") }
            val messageParticipants = allKeys.map { it.removePrefix("messages_") }
            
            // Combine all participant IDs and load their messages
            val allParticipants = (conversationParticipants + requestParticipants + messageParticipants).distinct()
            allParticipants.forEach { participantId ->
                val msgsJson = sharedPreferences.getString("messages_$participantId", null)
                if (msgsJson != null) {
                    val msgs = msgListAdapter.fromJson(msgsJson) ?: emptyList()
                    conversationMessages[participantId] = MutableStateFlow(msgs)
                }
            }
        } catch (e: Exception) {
            // Handle parse errors on schema change by clearing
            sharedPreferences.edit().clear().apply()
        }
    }
}
