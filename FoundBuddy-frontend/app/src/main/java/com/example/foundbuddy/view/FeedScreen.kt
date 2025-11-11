package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.R
import com.example.foundbuddy.model.FoundItem

@Composable
fun FeedScreen(
    items: List<FoundItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val foundItems = items.filter { it.status == "Gefunden" && it.title.contains(query, true) }
    val lostItems = items.filter { it.status == "Verloren" && it.title.contains(query, true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FoundBuddy",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Suche...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.width(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (foundItems.isNotEmpty()) {
                item {
                    Text("Gefundene Sachen", style = MaterialTheme.typography.titleMedium)
                }
                items(foundItems) { item ->
                    ItemCard(item)
                }
            }

            if (lostItems.isNotEmpty()) {
                item {
                    Text("Verlorene Sachen", style = MaterialTheme.typography.titleMedium)
                }
                items(lostItems) { item ->
                    ItemCard(item)
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: FoundItem) {
    Surface(
        color = Color(0xFFF5E7FF),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.imagePath.isNullOrEmpty()) {
                AsyncImage(
                    model = item.imagePath,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 12.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = null,
                    tint = Color(0xFF7A4B9A),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 12.dp)
                )
            }

            Column {
                Text(
                    text = item.title,
                    color = Color(0xFF4A2D68),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!item.description.isNullOrEmpty()) {
                    Text(
                        text = item.description,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
