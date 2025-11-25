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
                    text = item.uploaderName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = { },
                    label = { Text(if (item.status == "Gefunden") "Gefunden" else "Verloren") }
                )
            }

            Spacer(Modifier.height(12.dp))

            ZoomImage(
                url = item.imagePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            item.description?.let {
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
