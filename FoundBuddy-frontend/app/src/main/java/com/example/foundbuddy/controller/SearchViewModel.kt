package com.example.foundbuddy.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.network.ApiClient
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _results = MutableLiveData<List<FoundItem>>(emptyList())
    val results: LiveData<List<FoundItem>> = _results
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val api: FoundBuddyApi = ApiClient.retrofit.create(FoundBuddyApi::class.java)

    fun search(query: String) {
        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }

        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response = api.aiSearch(mapOf("query" to query))
                if (response.isSuccessful) {
                    val aiResults = response.body() ?: emptyList()
                    _results.value = aiResults.map { it.item }
                } else {
                    _error.value = "Suche fehlgeschlagen: ${response.code()}"
                    _results.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Netzwerkfehler: ${e.message}"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
