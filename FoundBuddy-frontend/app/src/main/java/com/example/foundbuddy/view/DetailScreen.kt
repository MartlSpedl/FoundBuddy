package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foundbuddy.model.FoundItem

@Composable
fun DetailScreen(
    item: FoundItem,
    onBack: () -> Unit,
    onResolve: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onBack) {
            Text("Zurück")
        }

        Spacer(modifier = Modifier.height(16.dp))

        item.imagePath?.let { imagePath ->
            // Dekodiere URL falls nötig (Firebase URLs sind oft URL-encodiert)
            val decodedUrl = try {
                if (imagePath.contains("%2F") || imagePath.contains("%3A")) {
                    java.net.URLDecoder.decode(imagePath, "UTF-8")
                } else {
                    imagePath
                }
            } catch (e: Exception) {
                println("LOGCAT: URL Dekodierung fehlgeschlagen: ${e.message}")
                imagePath
            }
            
            println("LOGCAT: DetailScreen - Dekodierte URL: $decodedUrl")
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(decodedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.title,
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(item.title, style = MaterialTheme.typography.titleLarge)
        item.description?.let { Text("Beschreibung: $it") }

        Spacer(modifier = Modifier.height(16.dp))

        if (!item.isResolved) {
            Button(onClick = { onResolve(item.id) }) {
                Text(if (item.status == "Gefunden") "Zurückgegeben" else "Zurückbekommen")
            }
        } else {
            Text("✅ Bereits zurückgegeben", color = Color(0xFF4CAF50))
        }
    }
}
