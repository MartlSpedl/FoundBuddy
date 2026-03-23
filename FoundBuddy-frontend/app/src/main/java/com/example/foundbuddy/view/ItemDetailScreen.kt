package com.example.foundbuddy.view


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    navController: NavController,
    vm: HomeViewModel,
    userViewModel: UserViewModel
) {
    val item = vm.getItemById(itemId)
    var commentText by remember { mutableStateOf("") }
    val comments by vm.getComments(itemId).collectAsState(initial = emptyList())
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)

    // Sprint 5: Status-Änderungs-Dialog
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedNewStatus by remember { mutableStateOf("") }
    var statusComment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(item?.title ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    // Share-Button
                    item?.let { currentItem ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(onClick = {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "Schau mal, was ich bei FoundBuddy gefunden habe: ${currentItem.title}\n\n${currentItem.description ?: ""}")
                                if (currentItem.imagePath?.isNotBlank() == true) {
                                    // In a real app, you might want to share the image URI too
                                    // putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(currentItem.imagePath))
                                }
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Teilen via"))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Teilen",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Favoriten-Button
                    IconButton(onClick = {
                        currentUser?.id?.let { userId ->
                            vm.toggleFavorite(itemId, userId)
                        }
                    }) {
                        Icon(
                            imageVector = if (item?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (item?.isFavorite == true)
                                "Aus Favoriten entfernen"
                            else
                                "Zu Favoriten hinzufügen",
                            tint = if (item?.isFavorite == true) Color(0xFFFFD700) else LocalContentColor.current
                        )
                    }
                    
                    // Status-Änderungs-Button (nur wenn Uploader)
                    currentUser?.let { user ->
                        item?.let { currentItem ->
                            // Nur der Uploader darf den Status ändern (oder Fallback für alte Items ohne ID)
                            val isOwner = currentItem.uploaderId == user.id || (currentItem.uploaderId.isBlank() && currentItem.uploaderName == user.username)
                            
                            if (isOwner) {
                                IconButton(onClick = { showStatusDialog = true }) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(vm.getStatusColor(currentItem.workflowStatus)))
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Kommentar hinzufügen…") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            vm.addComment(itemId, commentText.trim())
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Text("Senden")
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Item nicht gefunden", style = MaterialTheme.typography.titleMedium)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                ZoomImage(
                    url = item.imagePath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    item.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(16.dp))

                    // Status-Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(item.status.uppercase()) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (item.status.lowercase()) {
                                    "verloren" -> MaterialTheme.colorScheme.errorContainer
                                    "gefunden" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )

                        AssistChip(
                            onClick = { showStatusDialog = true },
                            label = { Text(item.workflowStatus) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(vm.getStatusColor(item.workflowStatus)).copy(alpha = 0.2f)
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Hochgeladen von ${item.uploaderName} • ${vm.formatTimeAgo(item.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sprint 5: Status-Workflow Abschnitt
                Spacer(Modifier.height(24.dp))
                Text(
                    "Status-Verlauf",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))

                // Aktueller Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(vm.getStatusColor(item.workflowStatus)))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Aktuell: ${item.workflowStatus}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status-Verlauf
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (item.statusHistory.isEmpty()) {
                        Text(
                            "Noch keine Statusänderungen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        item.statusHistory.forEachIndexed { index, change ->
                            StatusChangeItem(change = change, vm = vm, isLast = index == item.statusHistory.size - 1)
                            if (index < item.statusHistory.size - 1) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Kommentare",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            items(comments) { comment ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.author,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = vm.formatTimeAgo(comment.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    // Sprint 5: Status-Änderungs-Dialog
    if (showStatusDialog && item != null && currentUser != null) {
        AlertDialog(
            onDismissRequest = {
                showStatusDialog = false
                selectedNewStatus = ""
                statusComment = ""
            },
            title = { Text("Status ändern") },
            text = {
                Column {
                    Text("Aktueller Status: ${item.workflowStatus}")
                    Spacer(Modifier.height(16.dp))

                    val possibleStatuses = vm.getNextPossibleStatus(item.workflowStatus)

                    if (possibleStatuses.isEmpty()) {
                        Text(
                            "Keine weiteren Statusänderungen möglich.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        possibleStatuses.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedNewStatus = status }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedNewStatus == status,
                                    onClick = { selectedNewStatus = status }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    status,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = statusComment,
                            onValueChange = { statusComment = it },
                            label = { Text("Kommentar (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val user = currentUser ?: return@TextButton
                        val currentItem = item ?: return@TextButton
                        
                        if (selectedNewStatus.isNotBlank() &&
                            selectedNewStatus != currentItem.workflowStatus &&
                            vm.getNextPossibleStatus(currentItem.workflowStatus).contains(selectedNewStatus)
                        ) {
                            vm.updateWorkflowStatus(
                                itemId = currentItem.id,
                                newStatus = selectedNewStatus,
                                userId = user.id,
                                username = user.username,
                                comment = if (statusComment.isNotBlank()) statusComment else null
                            )
                            showStatusDialog = false
                            selectedNewStatus = ""
                            statusComment = ""
                        }
                    },
                    enabled = selectedNewStatus.isNotBlank() &&
                            selectedNewStatus != (item?.workflowStatus ?: "") &&
                            vm.getNextPossibleStatus(item?.workflowStatus ?: "").contains(selectedNewStatus)
                ) {
                    Text("Status aktualisieren")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStatusDialog = false
                    selectedNewStatus = ""
                    statusComment = ""
                }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun StatusChangeItem(
    change: com.example.foundbuddy.model.StatusChange,
    vm: HomeViewModel,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "${change.oldStatus} → ${change.newStatus}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "von ${change.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            change.comment?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Kommentar: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                vm.formatTimeAgo(change.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
