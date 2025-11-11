package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.foundbuddy.model.FoundItem

@Composable
fun DetailScreen(
    item: FoundItem,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zurück-Button
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDB8FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Zurück", color = Color(0xFF4A2D68))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bild aus dem item anzeigen
        if (!item.imagePath.isNullOrEmpty()) {
            AsyncImage(
                model = item.imagePath,
                contentDescription = item.toString(),
                modifier = Modifier
                    .size(250.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback, wenn kein Bild vorhanden
            Surface(
                color = Color(0xFFF5E7FF),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(250.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Kein Bild verfügbar", color = Color(0xFF7A4B9A))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Item-Informationen
        Text(
            text = item.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF4A2D68)
        )
    }
}
