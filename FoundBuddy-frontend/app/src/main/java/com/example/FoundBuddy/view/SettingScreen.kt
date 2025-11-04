package com.example.FoundBuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.FoundBuddy.R
import com.example.FoundBuddy.controller.UserViewModel
import com.example.FoundBuddy.model.User

/**
 * Einstellungen: Profil bearbeiten und Fundsachen löschen.
 *
 * @param userViewModel ViewModel zum Laden/Speichern des Benutzers
 * @param onClear Callback zum Löschen aller Fundsachen
 */
@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Aktueller Benutzer aus dem ViewModel
    val userState by userViewModel.user.collectAsState()

    var userName by remember { mutableStateOf(userState?.userName ?: "") }
    var fullName by remember { mutableStateOf(userState?.fullName ?: "") }
    var bio by remember { mutableStateOf(userState?.bio ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Benutzername") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Wenn schon ein User existiert, kopieren wir die ID und das Profilbild
                val newUser = userState?.copy(
                    userName = userName,
                    fullName = fullName,
                    bio = bio
                ) ?: User(
                    userName = userName,
                    fullName = fullName,
                    bio = bio
                )
                userViewModel.saveUser(newUser)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Profil speichern")
        }

        Divider(Modifier.padding(vertical = 12.dp))

        // Bereich zum Löschen aller gespeicherten Fundsachen
        Text("App-Verwaltung", style = MaterialTheme.typography.headlineSmall)
        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.delete_icon),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Text("Alle Fundsachen löschen")
        }
    }
}
