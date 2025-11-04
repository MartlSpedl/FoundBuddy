package com.example.FoundBuddy.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.FoundBuddy.model.Item
import com.example.FoundBuddy.model.ItemRepository

class HomeViewModel : ViewModel() {
    private val _items = MutableLiveData<List<Item>>(ItemRepository.items)
    val items: LiveData<List<Item>> = _items

    fun refreshItems() {
        _items.value = ItemRepository.items
    }
}