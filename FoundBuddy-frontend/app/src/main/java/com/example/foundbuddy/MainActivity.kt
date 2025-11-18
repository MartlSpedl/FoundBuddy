package com.example.foundbuddy

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foundbuddy.controller.UserViewModel
import com.example.foundbuddy.data.FoundItemRepository
import com.example.foundbuddy.model.FoundItem
import com.example.foundbuddy.view.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = FoundItemRepository(this)

        setContent {
            val userViewModel: UserViewModel = viewModel()
            var isLoggedIn by remember { mutableStateOf(false) }

            val isDarkMode by userViewModel.isDarkMode
            val scope = rememberCoroutineScope()
            var items by remember { mutableStateOf(listOf<FoundItem>()) }

            if (!isLoggedIn) {
                AuthScreen(
                    userViewModel = userViewModel,
                    onLoginSuccess = {
                        isLoggedIn = true
                        scope.launch { items = repo.getAll() }
                    }
                )
            } else {
                // Dynamisches Theme
                val colorScheme = if (isDarkMode) darkColorScheme(
                    primary = Color(0xFFBB86FC),
                    onPrimary = Color.Black,
                    secondary = Color(0xFF03DAC6),
                    background = Color(0xFF121212),
                    onBackground = Color(0xFFEAEAEA),
                    surface = Color(0xFF1E1E1E),
                    onSurface = Color(0xFFEAEAEA),
                    error = Color(0xFFCF6679),
                    onError = Color.Black
                ) else lightColorScheme(
                    primary = Color(0xFF7B68EE),
                    onPrimary = Color.White,
                    secondary = Color(0xFF8A7FF5),
                    background = Color(0xFFF5F7FF),
                    onBackground = Color(0xFF1E1E1E),
                    surface = Color.White,
                    onSurface = Color(0xFF1E1E1E),
                    error = Color(0xFFB00020),
                    onError = Color.White
                )

                MaterialTheme(colorScheme = colorScheme) {
                    var screen by remember { mutableStateOf("feed") }

                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = screen == "feed",
                                    onClick = { screen = "feed" },
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.home_icon),
                                            contentDescription = "Feed",
                                            tint = Color.Unspecified
                                        )
                                    },
                                    label = { Text("Feed") }
                                )
                                NavigationBarItem(
                                    selected = screen == "upload",
                                    onClick = { screen = "upload" },
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
                                    selected = screen == "settings",
                                    onClick = { screen = "settings" },
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
                    ) { paddingValues ->
                        when (screen) {
                            "feed" -> FeedScreen(items = items, modifier = Modifier.padding(paddingValues))
                            "upload" -> UploadScreen(
                                onUpload = { newItem ->
                                    scope.launch {
                                        repo.addItem(newItem)
                                        items = repo.getAll()
                                    }
                                    screen = "feed"
                                },
                                modifier = Modifier.padding(paddingValues)
                            )
                            "settings" -> ProfileScreen(
                                userViewModel = userViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
