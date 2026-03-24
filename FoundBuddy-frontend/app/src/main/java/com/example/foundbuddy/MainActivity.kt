package com.example.foundbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.view.*
import com.example.foundbuddy.ui.theme.*
import com.example.foundbuddy.ui.components.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val api = ApiClient.retrofit.create(FoundBuddyApi::class.java)
        val repository = FoundItemRepository(this, api)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            val homeViewModel: HomeViewModel = viewModel()
            val navController: NavHostController = rememberNavController()

            // Repository im ViewModel setzen
            LaunchedEffect(Unit) {
                homeViewModel.setRepository(repository)
            }

            val isDarkMode by userViewModel.isDarkMode.collectAsState(
                initial = isSystemInDarkTheme()
            )

            FoundBuddyTheme(darkTheme = isDarkMode) {

                val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
                val isSessionRestoring by userViewModel.isSessionRestoring.collectAsState(initial = true)
                val isLoggedIn = currentUser != null
                val scope = rememberCoroutineScope()

                // ⏳ Warte auf Session-Wiederherstellung bevor navigiert wird
                if (isSessionRestoring) {
                    Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@FoundBuddyTheme
                }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        homeViewModel.loadItems()
                        currentUser?.id?.let { userId ->
                            homeViewModel.loadFavorites(userId)
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) "main" else "auth"
                ) {
                    composable("auth") {
                        AuthScreen(
                            userViewModel = userViewModel,
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        var selectedTab by remember { mutableStateOf("feed") }
                        Scaffold(
                            bottomBar = {
                                // Glassmorphism Bottom Nav
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                    tonalElevation = 0.dp,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        val indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        
                                        NavigationBarItem(
                                            selected = selectedTab == "feed",
                                            onClick = { selectedTab = "feed" },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Home,
                                                    contentDescription = "Feed",
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text("Entdecken") }
                                        )

                                        NavigationBarItem(
                                            selected = selectedTab == "favorites",
                                            onClick = { selectedTab = "favorites" },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Favoriten",
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text("Gemerkt") }
                                        )

                                        NavigationBarItem(
                                            selected = selectedTab == "upload",
                                            onClick = { selectedTab = "upload" },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    contentDescription = "Upload",
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            },
                                            label = { Text("Posten") }
                                        )

                                        NavigationBarItem(
                                            selected = selectedTab == "messages",
                                            onClick = { selectedTab = "messages" },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            icon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_message),
                                                    contentDescription = "Nachrichten",
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text("Nachrichten") }
                                        )

                                        NavigationBarItem(
                                            selected = selectedTab == "profile",
                                            onClick = { selectedTab = "profile" },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Profil",
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text("Profil") }
                                        )
                                    }
                                }
                            }
                        ) { padding ->
                            when (selectedTab) {
                                "feed" -> FeedScreen(
                                    vm = homeViewModel,
                                    userViewModel = userViewModel,
                                    navController = navController,
                                    onItemClick = { id ->
                                        homeViewModel.loadStatusHistory(id)
                                        navController.navigate("detail/$id")
                                    },
                                    modifier = Modifier.padding(padding)
                                )

                                "favorites" -> FavoritesScreen(
                                    vm = homeViewModel,
                                    userViewModel = userViewModel,
                                    navController = navController,
                                    onItemClick = { id: String ->
                                        homeViewModel.loadStatusHistory(id)
                                        navController.navigate("detail/$id")
                                    },
                                    modifier = Modifier.padding(padding)
                                )

                                "upload" -> UploadScreen(
                                    userViewModel = userViewModel,
                                    onUpload = { newItem ->
                                        scope.launch {
                                            // Lade alle Daten neu vom Backend statt lokal hinzuzufügen
                                            homeViewModel.refreshItems(repository.getAll())
                                        }
                                        selectedTab = "feed"
                                    },
                                    modifier = Modifier.padding(padding)
                                )

                                "messages" -> ChatListScreen(
                                    vm = homeViewModel,
                                    onConversationClick = { id, name ->
                                        navController.navigate("chat_detail/$id/$name")
                                    },
                                    modifier = Modifier.padding(padding)
                                )

                                "profile" -> Box(modifier = Modifier.padding(padding)) {
                                    ProfileScreen(
                                        userViewModel = userViewModel,
                                        homeViewModel = homeViewModel,
                                        onLogout = {
                                            userViewModel.logout()
                                            navController.navigate("auth") {
                                                popUpTo("main") { inclusive = true }
                                            }
                                        },
                                        onItemClick = { id ->
                                            homeViewModel.loadStatusHistory(id)
                                            navController.navigate("detail/$id")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    composable("detail/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")

                        if (itemId.isNullOrBlank()) {
                            ItemDetailScreen(
                                itemId = "",
                                navController = navController,
                                vm = homeViewModel,
                                userViewModel = userViewModel
                            )
                        } else {
                            ItemDetailScreen(
                                itemId = itemId,
                                navController = navController,
                                vm = homeViewModel,
                                userViewModel = userViewModel
                            )
                        }
                    }

                    composable("chat_detail/{recipientId}/{recipientName}") { backStackEntry ->
                        val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                        val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""
                        ChatDetailScreen(
                            recipientId = recipientId,
                            recipientName = recipientName,
                            vm = homeViewModel,
                            userViewModel = userViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}