package com.example.foundbuddy.model

object ItemRepository {
    private val itemsList = mutableListOf<Item>()

    val items: List<Item> get() = itemsList.toList()

    fun search(query: String): List<Item> {
        return itemsList.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        }
    }

    fun addItem(item: Item) {
        itemsList.add(item)
    }
}
