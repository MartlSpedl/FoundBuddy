package com.example.foundbuddy.view


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.foundbuddy.controller.LanguageManager
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.model.User
import com.example.foundbuddy.ui.components.ChatDialog


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
    val lang by userViewModel.language.collectAsState()

    // Sprint 5: Status-Änderungs-Dialog
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedNewStatus by remember { mutableStateOf("") }
    var statusComment by remember { mutableStateOf("") }
    
    // Chat-Dialog State
    var showChatDialog by remember { mutableStateOf(false) }

    // Refresh item on screen open
    LaunchedEffect(itemId) {
        vm.refreshItem(itemId)
    }

    val isOwner = currentUser?.let { user ->
        item?.let { currentItem ->
            currentItem.uploaderId == user.id || (currentItem.uploaderId.isBlank() && currentItem.uploaderName == user.username)
        }
    } ?: false

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(item?.title ?: LanguageManager.tr("details", lang)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LanguageManager.tr("back", lang))
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
                                putExtra(android.content.Intent.EXTRA_TEXT, String.format(LanguageManager.tr("uploaded_by", lang), currentItem.uploaderName) + "\n\n${currentItem.description ?: ""}")
                                if (currentItem.imagePath?.isNotBlank() == true) {
                                    // In a real app, you might want to share the image URI too
                                    // putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(currentItem.imagePath))
                                }
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, LanguageManager.tr("share_via", lang)))
                        }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = LanguageManager.tr("share", lang),
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
                                LanguageManager.tr("remove_from_favorites", lang)
                            else
                                LanguageManager.tr("add_to_favorites", lang),
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
                    placeholder = { Text(LanguageManager.tr("add_comment", lang)) },
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
                    Text(LanguageManager.tr("send", lang))
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
                Text(LanguageManager.tr("item_not_found", lang), style = MaterialTheme.typography.titleMedium)
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
                            label = { Text(vm.translateStatus(item.status, lang)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (item.status.lowercase()) {
                                    "verloren" -> MaterialTheme.colorScheme.errorContainer
                                    "gefunden" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )

                        if (isOwner) {
                            AssistChip(
                                onClick = { showStatusDialog = true },
                                label = { Text(vm.translateStatus(item.workflowStatus, lang)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(vm.getStatusColor(item.workflowStatus)).copy(alpha = 0.2f)
                                )
                            )
                        } else {
                            AssistChip(
                                onClick = { },
                                label = { Text(vm.translateStatus(item.workflowStatus, lang)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(vm.getStatusColor(item.workflowStatus)).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        String.format(LanguageManager.tr("uploaded_by", lang), item.uploaderName) + " • ${vm.formatTimeAgo(item.timestamp, lang)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isOwner && item.uploaderId.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                navController.navigate("chat_detail/${item.uploaderId}/${item.uploaderName}/${item.id}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_message),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(LanguageManager.tr("send_message_button", lang), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sprint 5: Status-Workflow Abschnitt
                Spacer(Modifier.height(24.dp))
                Text(
                    LanguageManager.tr("status_history", lang),
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
                        String.format(LanguageManager.tr("current_status", lang), vm.translateStatus(item.workflowStatus, lang)),
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
                            LanguageManager.tr("no_status_changes", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        item.statusHistory.forEachIndexed { index, change ->
                            StatusChangeItem(change = change, vm = vm, isLast = index == item.statusHistory.size - 1, lang = lang)
                            if (index < item.statusHistory.size - 1) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    LanguageManager.tr("comments", lang),
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
                            text = vm.formatTimeAgo(comment.timestamp, lang),
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
            title = { Text(LanguageManager.tr("change_status", lang)) },
            text = {
                Column {
                    Text(String.format(LanguageManager.tr("current_status_label", lang), vm.translateStatus(item.workflowStatus, lang)))
                    Spacer(Modifier.height(16.dp))

                    val possibleStatuses = vm.getNextPossibleStatus(item.workflowStatus)

                    if (possibleStatuses.isEmpty()) {
                        Text(
                            LanguageManager.tr("no_more_status_changes", lang),
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
                                    vm.translateStatus(status, lang),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = statusComment,
                            onValueChange = { statusComment = it },
                            label = { Text(LanguageManager.tr("comment_optional", lang)) },
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
                    Text(LanguageManager.tr("update_status", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStatusDialog = false
                    selectedNewStatus = ""
                    statusComment = ""
                }) {
                    Text(LanguageManager.tr("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun StatusChangeItem(
    change: com.example.foundbuddy.model.StatusChange,
    vm: HomeViewModel,
    isLast: Boolean,
    lang: String = "de"
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
                String.format(LanguageManager.tr("status_change", lang), change.oldStatus, change.newStatus),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                String.format(LanguageManager.tr("by_user", lang), change.username),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            change.comment?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    String.format(LanguageManager.tr("comment_label", lang), it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                vm.formatTimeAgo(change.timestamp, lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
