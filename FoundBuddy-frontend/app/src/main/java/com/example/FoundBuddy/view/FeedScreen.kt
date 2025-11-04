package com.example.FoundBuddy.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.FoundBuddy.R
import com.example.FoundBuddy.model.FoundItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(items: List<FoundItem>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        // Material3‑Komponente wird direkt verwendet
        TopAppBar(
            title = { Text("FoundBuddy") },
            actions = {
                Icon(
                    painter = painterResource(R.drawable.search_icon),
                    contentDescription = "Suche",
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(12.dp))
            }
        )

        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Noch keine Fundsachen 📦", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { /* TODO: Detail */ },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUri),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(2.dp)
                        )
                        Text(
                            item.title,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
