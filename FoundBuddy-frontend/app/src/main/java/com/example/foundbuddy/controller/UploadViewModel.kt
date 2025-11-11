package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.Item
import com.example.foundbuddy.model.ItemRepository
import com.example.foundbuddy.model.ItemStatus

class UploadViewModel : ViewModel() {
    fun uploadItem(title: String, description: String, isFound: Boolean, photoUri: String?) {
        val item = Item(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            status = if (isFound) ItemStatus.FOUND else ItemStatus.LOST,
            timestamp = System.currentTimeMillis(),
            photoUri = photoUri
        )
        ItemRepository.addItem(item)
    }
}
