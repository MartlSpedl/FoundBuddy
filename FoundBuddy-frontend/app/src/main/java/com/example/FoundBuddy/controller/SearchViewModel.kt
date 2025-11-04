package com.example.FoundBuddy.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.FoundBuddy.model.Item
import com.example.FoundBuddy.model.ItemRepository

class SearchViewModel : ViewModel() {
    private val _results = MutableLiveData<List<Item>>(emptyList())
    val results: LiveData<List<Item>> = _results

    fun search(query: String) {
        _results.value = ItemRepository.search(query)
    }
}