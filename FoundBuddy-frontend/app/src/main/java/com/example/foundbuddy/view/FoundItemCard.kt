package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.R
import com.example.foundbuddy.model.FoundItem

@Composable
fun FoundItemCard(
    item: FoundItem,
    onClick: () -> Unit,
    onLike: () -> Unit
) {
    // Defensive check - if item is null, show placeholder
    if (item.id.isBlank()) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ungültiges Item", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    // Wichtig: Backend liefert oft imageUri (http/https). Manche alten Items haben imagePath.
    val imageUrl = item.imagePath?.trim().takeIf { !it.isNullOrBlank() }

    val statusLower = (item.status ?: "").trim().lowercase()
    val statusLabel = when (statusLower) {
        "gefunden" -> "Gefunden"
        "verloren" -> "Verloren"
        else -> if (item.status.isNullOrBlank()) "Unbekannt" else item.status.trim()
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.uploaderName ?: "Unbekannt",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = { },
                    label = { Text(statusLabel) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (statusLower) {
                            "verloren" -> MaterialTheme.colorScheme.errorContainer
                            "gefunden" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            if (imageUrl != null) {
                ZoomImage(
                    url = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                // Fallback, damit du sofort siehst: URL fehlt wirklich (statt "nichts passiert")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kein Bild",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                item.title ?: "Ohne Titel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        painter = painterResource(
                            if (item.likedByUser) R.drawable.ic_heart_filled
                            else R.drawable.ic_heart_outline
                        ),
                        contentDescription = "Like",
                        tint = if (item.likedByUser)
                            MaterialTheme.colorScheme.error
                        else
                            LocalContentColor.current
                    )
                }
                Text("${item.likes} Likes", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
