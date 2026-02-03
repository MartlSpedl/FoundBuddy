package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel

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
    var searchQuery by remember { mutableStateOf("") }
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)

    val filteredItems = remember(items, searchQuery) {
        items.filter { !it.isResolved }.filter { item ->
            searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description?.contains(searchQuery, ignoreCase = true) == true ||
                    item.workflowStatus.contains(searchQuery, ignoreCase = true)
        }
    }

    val foundItems = filteredItems.filter { it.status.equals("Gefunden", ignoreCase = true) }
    val lostItems = filteredItems.filter { it.status.equals("Verloren", ignoreCase = true) }

    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text("FoundBuddy", fontWeight = FontWeight.Bold)
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Suche nach Titel, Beschreibung oder Status…") },
            leadingIcon = { Icon(Icons.Filled.Search, "Suche") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (foundItems.isNotEmpty()) {
                item {
                    Text(
                        "Gefundene Sachen",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(foundItems) { item ->
                    FoundItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLike = { vm.toggleLike(item.id) },
                        userViewModel = userViewModel,
                        onFavorite = {
                            currentUser?.id?.let { userId ->
                                vm.toggleFavorite(item.id, userId)
                            }
                        },
                        vm = vm
                    )
                }
            }

            if (lostItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Verlorene Sachen",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(lostItems) { item ->
                    FoundItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLike = { vm.toggleLike(item.id) },
                        userViewModel = userViewModel,
                        onFavorite = {
                            currentUser?.id?.let { userId ->
                                vm.toggleFavorite(item.id, userId)
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