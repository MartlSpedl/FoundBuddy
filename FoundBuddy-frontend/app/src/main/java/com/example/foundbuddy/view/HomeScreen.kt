package com.example.foundbuddy.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.model.Item
import com.example.foundbuddy.model.ItemStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(padding: PaddingValues, vm: HomeViewModel) {
    val items by vm.items.observeAsState(emptyList())

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("FoundBuddy", fontWeight = FontWeight.Bold) }) }) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                ItemCard(item)
            }
        }
    }
}

@Composable
fun ItemCard(item: Item) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header mit Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (item.status == ItemStatus.FOUND) "Gefunden" else "Verloren") }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "vor ${System.currentTimeMillis() - item.timestamp}ms", // Einfache Zeitangabe
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Titel
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Beschreibung
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bild, wenn vorhanden
            item.photoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Item Bild",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
