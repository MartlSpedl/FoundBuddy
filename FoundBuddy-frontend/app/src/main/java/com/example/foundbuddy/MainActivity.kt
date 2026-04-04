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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.LanguageManager
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.network.ApiClient
import com.example.foundbuddy.network.FoundBuddyApi
import com.example.foundbuddy.view.*
import com.example.foundbuddy.ui.theme.*
import com.example.foundbuddy.ui.components.*
import kotlinx.coroutines.delay
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
            val lang by userViewModel.language.collectAsState()

            LaunchedEffect(lang) {
                LanguageManager.setLanguage(lang)
            }

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

                val unreadCount by homeViewModel.unreadCount.collectAsState()
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        homeViewModel.loadItems()
                        currentUser?.id?.let { userId ->
                            homeViewModel.loadFavorites(userId)
                            while (true) {
                                homeViewModel.loadConversationsFromBackend(userId)
                                delay(15000)
                            }
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
                                                    contentDescription = LanguageManager.tr("discover", lang),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text(LanguageManager.tr("discover", lang)) }
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
                                                    contentDescription = LanguageManager.tr("favorites", lang),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text(LanguageManager.tr("favorites", lang)) }
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
                                                    contentDescription = LanguageManager.tr("upload", lang),
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            },
                                            label = { Text(LanguageManager.tr("upload", lang)) }
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
                                                Box {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_message),
                                                        contentDescription = LanguageManager.tr("messages", lang),
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                    if (unreadCount > 0) {
                                                        Badge(
                                                            modifier = Modifier.align(Alignment.TopEnd)
                                                        ) {
                                                            Text(
                                                                text = if (unreadCount > 99) "99+"
                                                                       else unreadCount.toString(),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            label = { Text(LanguageManager.tr("messages", lang)) }
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
                                                    contentDescription = LanguageManager.tr("profile", lang),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            },
                                            label = { Text(LanguageManager.tr("profile", lang)) }
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

                                "messages" -> {
                                    LaunchedEffect(Unit) {
                                        userViewModel.currentUserFlow.collect { user ->
                                            user?.id?.let { userId ->
                                                while (true) {
                                                    homeViewModel.loadConversationsFromBackend(userId)
                                                    delay(5000)
                                                }
                                            }
                                        }
                                    }
                                    
                                    ChatListScreen(
                                        vm = homeViewModel,
                                        userViewModel = userViewModel,
                                        onConversationClick = { id, name ->
                                            navController.navigate("chat_detail/$id/$name/")
                                        },
                                        modifier = Modifier.padding(padding)
                                    )
                                }

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

                    composable("chat_detail/{recipientId}/{recipientName}/{referencedItemId}") { backStackEntry ->
                        val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                        val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""
                        val referencedItemId = backStackEntry.arguments?.getString("referencedItemId")
                        ChatDetailScreen(
                            recipientId = recipientId,
                            recipientName = recipientName,
                            initialItemId = referencedItemId,
                            vm = homeViewModel,
                            userViewModel = userViewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToItem = { itemId ->
                                homeViewModel.loadStatusHistory(itemId)
                                navController.navigate("detail/$itemId")
                            }
                        )
                    }
                }
            }
        }
    }
}