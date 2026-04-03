package com.example.foundbuddy.controller

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.network.ApiClient
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val TAG = "SearchViewModel"
    
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
                val response = api.aiSearch(mapOf("description" to query))
                if (response.isSuccessful) {
                    val aiResults = response.body() ?: emptyList()
                    _results.value = aiResults.map { it.item }
                    Log.d(TAG, "Search returned ${aiResults.size} results")
                } else {
                    val errorMsg = "Suche fehlgeschlagen: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    _error.value = errorMsg
                    _results.value = emptyList()
                }
            } catch (e: JsonDataException) {
                val errorMsg = "JSON Fehler: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
                _results.value = emptyList()
            } catch (e: Exception) {
                val errorMsg = "Netzwerkfehler: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
