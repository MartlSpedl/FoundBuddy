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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val username by userViewModel.username.collectAsState(initial = "Gast")
    val email by userViewModel.email.collectAsState(initial = "nicht angemeldet")
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()

    // Achtung: kann null/leer/kaputt sein
    val profileImageUri = currentUser?.profileImage
    val isDarkMode by userViewModel.isDarkMode.collectAsState()
    val scope = rememberCoroutineScope()

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
            title = { Text("Fehler") },
            text = { Text(errorMessage ?: "Ein unbekannter Fehler ist aufgetreten") },
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
                userViewModel.updateProfileImage(newUri.toString())
            }
        }
    }

    // Platzhalter-Stats (ersetzen wenn du echte Daten hast)
    val postsCount = 0
    val followersCount = 0
    val followingCount = 0

    // Grid Platzhalter
    val gridItems = remember { List(12) { it } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Avatar + Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val hasImage = !profileImageUri.isNullOrBlank()

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable(enabled = currentUser != null) { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (hasImage) {
                    val imageUri = remember(profileImageUri) {
                        try {
                            val uri = Uri.parse(profileImageUri)
                            if (uri.scheme != null) uri.toString() else null
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (imageUri != null) {
                        // Dekodiere URL falls nötig (Firebase URLs sind oft URL-encodiert)
                        val decodedUrl = try {
                            if (imageUri.contains("%2F") || imageUri.contains("%3A")) {
                                java.net.URLDecoder.decode(imageUri, "UTF-8")
                            } else {
                                imageUri
                            }
                        } catch (e: Exception) {
                            println("LOGCAT: ProfileScreen URL Dekodierung fehlgeschlagen: ${e.message}")
                            imageUri
                        }
                        
                        println("LOGCAT: ProfileScreen - Dekodierte URL: $decodedUrl")
                        
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(decodedUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profilbild",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.profile_icon),
                            contentDescription = "Standardbild",
                            tint = Color.Gray,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.profile_icon),
                        contentDescription = "Standardbild",
                        tint = Color.Gray,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.width(18.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBlock(value = postsCount, label = "Beiträge")
                StatBlock(value = followersCount, label = "Follower")
                StatBlock(value = followingCount, label = "Folge ich")
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Lost & Found mit FoundBuddy",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        // Buttons (optional, ohne Funktion)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { /* später */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("Profil bearbeiten")
            }

            OutlinedButton(
                onClick = { /* später */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("Profil teilen")
            }
        }

        Spacer(Modifier.height(14.dp))

        // Dark Mode Row (stabil)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = {
                    scope.launch {
                        try {
                            userViewModel.toggleDarkMode()
                        } catch (_: Exception) {}
                    }
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Logout Button: nur wenn eingeloggt
        if (currentUser != null) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Abmelden")
                Spacer(Modifier.width(10.dp))
                Text("Abmelden")
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Beiträge",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        // IMPORTANT:
        // Kein LazyVerticalGrid innerhalb einer Column(verticalScroll), sonst kann Compose
        // "infinite height constraints" werfen und crasht.
        // Deshalb bauen wir das Grid hier als normales (nicht scrollbares) Layout.
        val rows = remember(gridItems) { gridItems.chunked(3) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowItems.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.profile_icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // falls letzte Reihe < 3 Items hat: mit Platzhaltern auffüllen
                    repeat(3 - rowItems.size) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun StatBlock(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
