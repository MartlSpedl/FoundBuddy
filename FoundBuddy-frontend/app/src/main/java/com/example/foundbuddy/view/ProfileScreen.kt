package com.example.foundbuddy.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.LanguageManager
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.model.FoundItem
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    homeViewModel: HomeViewModel,
    onLogout: () -> Unit,
    onItemClick: (String) -> Unit = {}
) {
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val username by userViewModel.username.collectAsState(initial = "Gast")
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    val lang by userViewModel.language.collectAsState()

    // Achtung: kann null/leer/kaputt sein
    val profileImageUri = currentUser?.profileImage
    val isDarkMode by userViewModel.isDarkMode.collectAsState()
    val scope = rememberCoroutineScope()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editProfileImageUri by remember { mutableStateOf<String?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    // Load user data when the screen is first shown
    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId ->
            userViewModel.loadCurrentUser(userId)
        }
    }

    // Show error dialog if there's an error
    if (!errorMessage.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { userViewModel.clearErrorMessage() },
            title = { Text(LanguageManager.tr("error", lang)) },
            text = { Text(errorMessage ?: LanguageManager.tr("error_unknown", lang)) },
            confirmButton = {
                TextButton(onClick = { userViewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    // Show loading overlay when loading
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { newUri ->
            scope.launch {
                if (showEditProfileDialog) {
                    editProfileImageUri = newUri.toString()
                } else {
                    userViewModel.updateProfileImage(newUri.toString())
                }
            }
        }
    }

    // Echt-Daten aus HomeViewModel
    val allItems by homeViewModel.items.collectAsState(initial = emptyList())
    val userPosts = remember(allItems, currentUser) {
        allItems.filter { 
            val isOwner = it.uploaderId == currentUser?.id || 
                    (it.uploaderId.isBlank() && it.uploaderName == username)
            isOwner
        }
    }
    
    val postsCount = userPosts.size

                    // Register-State
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(LanguageManager.tr("found", lang), LanguageManager.tr("lost", lang))
    
    val displayPosts = remember(userPosts, selectedTabIndex) {
                    val status = if (selectedTabIndex == 0) LanguageManager.tr("found", lang) else LanguageManager.tr("lost", lang)
        userPosts.filter { it.status.equals(status, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // --- Header (Compact & Premium) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showSettingsSheet = true }) {
                Icon(Icons.Default.Settings, contentDescription = LanguageManager.tr("settings", lang), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // --- Avatar & Stats Row ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Instagram-like Avatar
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable(enabled = currentUser != null) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = LanguageManager.tr("default_profile_image", lang),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profilbild",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(32.dp))

            // Stats Block
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                StatBlock(value = postsCount, label = LanguageManager.tr("posts", lang))
            }
        }

        // --- Bio Section ---
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Text(
                text = LanguageManager.tr("bio_default", lang),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = currentUser?.bio ?: LanguageManager.tr("bio_tagline", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- Action Buttons (Rounded & Clean) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileActionButton(
                text = LanguageManager.tr("edit_profile", lang),
                onClick = {
                    editUsername = username
                    editBio = currentUser?.bio ?: ""
                    editProfileImageUri = profileImageUri
                    showEditProfileDialog = true
                },
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            ProfileActionButton(
                text = LanguageManager.tr("share", lang),
                onClick = { /* TODO */ },
                modifier = Modifier.weight(0.4f),
                color = MaterialTheme.colorScheme.secondary
            )
            if (currentUser != null) {
                Surface(
                    onClick = onLogout,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- Tabs ---
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(2.dp))

        if (displayPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = LanguageManager.tr("no_posts", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val rows = remember(displayPosts) { displayPosts.chunked(3) }
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { onItemClick(item.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.imagePath.isNullOrBlank()) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.imagePath)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            SettingsScreen(
                userViewModel = userViewModel,
                onClear = { /* TODO: Items löschen Logik */ },
                onLogout = {
                    showSettingsSheet = false
                    onLogout()
                }
            )
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            username = editUsername,
            bio = editBio,
            profileImageUri = editProfileImageUri ?: profileImageUri,
            language = lang,
            onUsernameChange = { editUsername = it },
            onBioChange = { editBio = it },
            onPickImage = { launcher.launch("image/*") },
            onSave = { newUsername, newBio, newImageUri ->
                scope.launch {
                    userViewModel.updateUsername(newUsername)
                    userViewModel.updateBio(newBio)
                    newImageUri?.let { userViewModel.updateProfileImage(it) }
                }
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun StatBlock(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditProfileDialog(
    username: String,
    bio: String,
    profileImageUri: String?,
    language: String,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onSave: (newUsername: String, newBio: String, newImageUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var currentUsername by remember { mutableStateOf(username) }
    var currentBio by remember { mutableStateOf(bio) }
    var currentImageUri by remember { mutableStateOf(profileImageUri) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = LanguageManager.tr("edit_profile", language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentImageUri.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = LanguageManager.tr("select_profile_image", language),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentImageUri)
                                .crossfade(true)
                                .build(),
                        contentDescription = LanguageManager.tr("profile_image", language),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = LanguageManager.tr("edit", language),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = currentUsername,
                    onValueChange = {
                        currentUsername = it
                        onUsernameChange(it)
                    },
                    label = { Text(LanguageManager.tr("username", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentBio,
                    onValueChange = {
                        currentBio = it
                        onBioChange(it)
                    },
                    label = { Text(LanguageManager.tr("bio", language)) },
                    placeholder = { Text(LanguageManager.tr("bio_placeholder", language)) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(LanguageManager.tr("cancel", language))
                    }
                    Button(
                        onClick = {
                            onSave(currentUsername, currentBio, currentImageUri)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = currentUsername.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(LanguageManager.tr("save", language))
                }
            }
        }
    }
}
}
