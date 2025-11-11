package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.foundbuddy.model.Item
import com.example.foundbuddy.model.ItemRepository

class HomeViewModel : ViewModel() {
    private val _items = MutableLiveData<List<Item>>(ItemRepository.items)
    val items: LiveData<List<Item>> = _items

    fun refreshItems() {
        _items.value = ItemRepository.items
    }
}