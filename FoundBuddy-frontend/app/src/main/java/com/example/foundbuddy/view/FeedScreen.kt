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
    val aiItems by vm.searchResults.collectAsState(initial = emptyList())
    val isSearching by vm.isSearching.collectAsState(initial = false)

    var searchQuery by remember { mutableStateOf("") }

    // Wenn Suchfeld leer -> normaler Feed
    // Wenn Suchfeld nicht leer -> AI-Ergebnisse
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

    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)

    Column(modifier = modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("FoundBuddy", fontWeight = FontWeight.Bold) }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                vm.searchAi(it) // <<< DAS ist der Switch auf Bildsuche
            },
            placeholder = { Text("Suche nach Bildinhalt…") },
            leadingIcon = { Icon(Icons.Filled.Search, "Suche") },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                }
            },
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
            // Gefunden Items
            if (foundItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Gefundene Gegenstände (${foundItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(foundItems) { item ->
                    FoundItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLike = { 
                            vm.toggleLike(item.id)
                        },
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

            // Verlorene Items
            if (lostItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Verlorene Gegenstände (${lostItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(lostItems) { item ->
                    FoundItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLike = { 
                            vm.toggleLike(item.id)
                        },
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

            // Andere Items
            if (otherItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Andere (${otherItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(otherItems) { item ->
                    FoundItemCard(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onLike = { 
                            vm.toggleLike(item.id)
                        },
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
