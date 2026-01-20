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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foundbuddy.controller.HomeViewModel
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.view.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FoundItemRepository(this)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            val homeViewModel: HomeViewModel = viewModel()
            val navController: NavHostController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

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

                // ✅ Wenn Login-State wechselt, routen wir sauber um
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        // Daten laden
                        homeViewModel.refreshItems(repository.getAll())

                        // Wenn wir gerade noch im Auth sind -> rüber ins Main
                        if (currentRoute == "auth") {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    } else {
                        // Wenn ausgeloggt und wir nicht im Auth sind -> zurück
                        if (currentRoute != null && currentRoute != "auth") {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "auth"
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
                                                painterResource(id = R.drawable.home_icon),
                                                contentDescription = "Feed",
                                                tint = Color.Unspecified
                                            )
                                        },
                                        label = { Text("Feed") }
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == "upload",
                                        onClick = { selectedTab = "upload" },
                                        icon = {
                                            Icon(
                                                painterResource(id = R.drawable.camera_icon),
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
                                                painterResource(id = R.drawable.profile_icon),
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
                                    navController = navController,
                                    onItemClick = { id -> navController.navigate("detail/$id") },
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
                        val itemId = backStackEntry.arguments?.getString("itemId").orEmpty()
                        ItemDetailScreen(
                            itemId = itemId,
                            navController = navController,
                            vm = homeViewModel
                        )
                    }
                }
            }
        }
    }
}
