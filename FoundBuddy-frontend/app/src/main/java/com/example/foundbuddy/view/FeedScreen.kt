package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.ui.components.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    vm: HomeViewModel,
    userViewModel: UserViewModel,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val items by vm.items.collectAsState(initial = emptyList())
    val aiItems by vm.searchResults.collectAsState(initial = emptyList())
    val isSearching by vm.isSearching.collectAsState(initial = false)
    val isLoading by vm.isLoading.collectAsState(initial = true)
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val username by userViewModel.username.collectAsState(initial = "Buddy")

    var searchQuery by remember { mutableStateOf("") }

    val listToShow = remember(items, aiItems, searchQuery) {
        if (searchQuery.isBlank()) items
        else aiItems
    }

    val filteredItems = remember(listToShow) {
        listToShow.filter { !it.isResolved }
    }

    val foundItems = filteredItems.filter { it.status.equals("Gefunden", ignoreCase = true) }
    val lostItems = filteredItems.filter { it.status.equals("Verloren", ignoreCase = true) }
    val otherItems = filteredItems.filter { item ->
        !item.status.equals("Gefunden", ignoreCase = true) &&
                !item.status.equals("Verloren", ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "FoundBuddy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Greeting Header ---
            item {
                GreetingHeader(
                    username = username,
                    profileImageUri = currentUser?.profileImage
                )
            }

            // Search Bar
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        vm.searchAi(it)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { 
                        Text(
                            "Finde verlorene Schätze...", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = "Suche",
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp, 
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

            if (isLoading && searchQuery.isBlank()) {
                items(3) {
                    FoundItemShimmer()
                }
            } else {
                if (foundItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Gefundene Gegenstände (${foundItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(foundItems) { item ->
                        FoundItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onLike = { vm.toggleLike(item.id) },
                            userViewModel = userViewModel,
                            onFavorite = {
                                currentUser?.let { user ->
                                    vm.toggleFavorite(item.id, user.id)
                                }
                            },
                            vm = vm
                        )
                    }
                }

                if (lostItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Verlorene Gegenstände (${lostItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(lostItems) { item ->
                        FoundItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onLike = { vm.toggleLike(item.id) },
                            userViewModel = userViewModel,
                            onFavorite = {
                                currentUser?.let { user ->
                                    vm.toggleFavorite(item.id, user.id)
                                }
                            },
                            vm = vm
                        )
                    }
                }

                if (otherItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Andere (${otherItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(otherItems) { item ->
                        FoundItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onLike = { vm.toggleLike(item.id) },
                            userViewModel = userViewModel,
                            onFavorite = {
                                currentUser?.let { user ->
                                    vm.toggleFavorite(item.id, user.id)
                                }
                            },
                            vm = vm
                        )
                    }
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "Noch keine Beiträge…" else "Keine Ergebnisse",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(
    username: String,
    profileImageUri: String?
) {
    val currentTime = remember { Calendar.getInstance() }
    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    
    val greeting = when {
        hour in 5..11 -> "Guten Morgen"
        hour in 12..17 -> "Guten Tag"
        hour in 18..22 -> "Guten Abend"
        else -> "Hallo"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text(
                text = "Willkommen zurück, $username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(52.dp)
        ) {
            if (profileImageUri.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp).fillMaxSize()
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
    }
}
