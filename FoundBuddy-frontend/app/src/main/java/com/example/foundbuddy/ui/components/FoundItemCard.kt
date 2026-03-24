package com.example.foundbuddy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.model.User
import com.example.foundbuddy.view.ZoomImage

@Composable
fun FoundItemCard(
    item: FoundItem,
    onClick: () -> Unit,
    onLike: () -> Unit,
    onMessageClick: (() -> Unit)? = null,
    userViewModel: UserViewModel? = null,
    onFavorite: (() -> Unit)? = null,
    vm: HomeViewModel
) {
    val currentUser by if (userViewModel != null) userViewModel.currentUserFlow.collectAsState(initial = null) else remember { mutableStateOf(null) }
    val isOwner = currentUser?.id == item.uploaderId || (item.uploaderId.isBlank() && currentUser?.username == item.uploaderName)

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

                // Status-Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                if (item.status == "Gefunden") "Gefunden" else "Verloren",
                                fontSize = 10.sp
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(item.workflowStatus, fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(vm.getStatusColor(item.workflowStatus)).copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
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
                Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }

            Spacer(Modifier.height(12.dp))

            // Erweiterte Action-Row mit Favoriten
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like-Button
                IconButton(onClick = onLike, modifier = Modifier.size(24.dp)) {
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
                Text("${item.likes}", style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.width(16.dp))

                // Favoriten-Button
                if (userViewModel != null && onFavorite != null) {
                    IconButton(onClick = onFavorite, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(
                                if (item.isFavorite) R.drawable.ic_star_filled
                                else R.drawable.ic_star_outline
                            ),
                            contentDescription = if (item.isFavorite)
                                "Aus Favoriten entfernen"
                            else
                                "Zu Favoriten hinzufügen",
                            tint = if (item.isFavorite)
                                Color(0xFFFFD700)
                            else
                                LocalContentColor.current
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // NEU: Nachricht-Button
                if (!isOwner && onMessageClick != null) {
                    Button(
                        onClick = onMessageClick,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_message), // Ensure this icon exists or use a default
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Nachricht", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    vm.formatTimeAgo(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
