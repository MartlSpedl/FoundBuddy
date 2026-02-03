package com.example.foundbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

            val colors = if (isDarkMode) darkColorScheme(
                primary = Color(0xFFBB86FC),
                secondary = Color(0xFF03DAC6),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onSurface = Color(0xFFEAEAEA)
            ) else lightColorScheme(
                primary = Color(0xFF7B68EE),
                secondary = Color(0xFF8A7FF5),
                background = Color(0xFFF5F7FF),
                surface = Color.White,
                onSurface = Color(0xFF1E1E1E)
            )

            MaterialTheme(colorScheme = colors) {

                val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
                val isLoggedIn = currentUser != null
                val scope = rememberCoroutineScope()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        homeViewModel.refreshItems(repository.getAll())
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
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = selectedTab == "feed",
                                        onClick = { selectedTab = "feed" },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.home_icon),
                                                contentDescription = "Feed",
                                                tint = Color.Unspecified
                                            )
                                        },
                                        label = { Text("Feed") }
                                    )

                                    // Sprint 5: Neue Favoriten-Tab
                                    NavigationBarItem(
                                        selected = selectedTab == "favorites",
                                        onClick = { selectedTab = "favorites" },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_star_filled),
                                                contentDescription = "Favoriten",
                                                tint = Color.Unspecified
                                            )
                                        },
                                        label = { Text("Favoriten") }
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == "upload",
                                        onClick = { selectedTab = "upload" },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.camera_icon),
                                                contentDescription = "Upload",
                                                tint = Color.Unspecified
                                            )
                                        },
                                        label = { Text("Upload") }
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == "profile",
                                        onClick = { selectedTab = "profile" },
                                        icon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.profile_icon),
                                                contentDescription = "Profil",
                                                tint = Color.Unspecified
                                            )
                                        },
                                        label = { Text("Profil") }
                                    )
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
                                    onUpload = { newItem ->
                                        scope.launch {
                                            repository.addItem(newItem)
                                            homeViewModel.refreshItems(repository.getAll())
                                        }
                                        selectedTab = "feed"
                                    },
                                    modifier = Modifier.padding(padding)
                                )

                                "profile" -> Box(modifier = Modifier.padding(padding)) {
                                    ProfileScreen(
                                        userViewModel = userViewModel,
                                        onLogout = {
                                            userViewModel.logout()
                                            navController.navigate("auth") {
                                                popUpTo("main") { inclusive = true }
                                            }
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
                }
            }
        }
    }
}