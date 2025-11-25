package com.example.foundbuddy.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.Item
import com.example.foundbuddy.model.ItemRepository

class SearchViewModel : ViewModel() {
    private val _results = MutableLiveData<List<Item>>(emptyList())
    val results: LiveData<List<Item>> = _results

    fun search(query: String) {
        _results.value = ItemRepository.search(query)
    }
}
