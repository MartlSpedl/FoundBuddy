package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.LanguageManager
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    vm: HomeViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by vm.favorites.collectAsState(initial = emptyList())
    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val lang by userViewModel.language.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = LanguageManager.tr("favorites", lang),
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(LanguageManager.tr("your_favorites", lang), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = LanguageManager.tr("no_favorites", lang),
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        LanguageManager.tr("no_favorites_yet", lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        LanguageManager.tr("mark_favorites_hint", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { item ->
                    FoundItemCard(
                        item = item,
                        vm = vm,
                        userViewModel = userViewModel,
                        onClick = { onItemClick(item.id) },
                        onLike = { vm.toggleLike(item.id) },
                        onMessageClick = {
                            val id = item.uploaderId.ifBlank { item.uploaderName }
                            navController.navigate("chat_detail/$id/${item.uploaderName}")
                        },
                        onFavorite = {
                            currentUser?.id?.let { userId ->
                                vm.toggleFavorite(item.id, userId)
                            }
                        }
                    )
                }
            }
        }
    }
}
