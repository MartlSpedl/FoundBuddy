package com.example.FoundBuddy

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
import com.example.FoundBuddy.controller.UserViewModel
import com.example.FoundBuddy.data.FoundItemRepository
import com.example.FoundBuddy.model.FoundItem
import com.example.FoundBuddy.view.FeedScreen
import com.example.FoundBuddy.view.SettingsScreen
import com.example.FoundBuddy.view.UploadScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = FoundItemRepository(this)

        setContent {
            var screen by remember { mutableStateOf("feed") }
            var items by remember { mutableStateOf(listOf<FoundItem>()) }
            val scope = rememberCoroutineScope()

            // Hole das UserViewModel (Compose sorgt für die richtige ViewModelFactory)
            val userViewModel: UserViewModel = viewModel()

            LaunchedEffect(Unit) {
                items = repo.getAll()
            }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF7B68EE),
                    secondary = Color(0xFF8A7FF5),
                    background = Color(0xFFF5F7FF),
                    surface = Color.White
                )
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = screen == "feed",
                                onClick = { screen = "feed" },
                                icon = {
                                    Icon(
                                        painter = painterResource(R.drawable.home_icon),
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
                                        painter = painterResource(R.drawable.camera_icon),
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
                                        painter = painterResource(R.drawable.profile_icon),
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
                        "feed" -> FeedScreen(
                            items = items,
                            modifier = Modifier.padding(paddingValues)
                        )

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

                        "settings" -> SettingsScreen(
                            userViewModel = userViewModel,
                            onClear = {
                                scope.launch {
                                    repo.clearAll()
                                    items = emptyList()
                                }
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
