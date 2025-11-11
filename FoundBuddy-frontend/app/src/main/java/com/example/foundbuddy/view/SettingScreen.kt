package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.controller.UserViewModel

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { userViewModel.username }
    var email by remember { userViewModel.email }
    var darkMode by remember { userViewModel.darkModeEnabled }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                userViewModel.updateUsername(it)
            },
            label = { Text("Benutzername") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                userViewModel.updateEmail(it)
            },
            label = { Text("E-Mail-Adresse") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                userViewModel.updateUsername(username)
                userViewModel.updateEmail(email)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A4B9A))
        ) {
            Text("Profil speichern", color = Color.White)
        }

        Divider(Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dark Mode aktivieren", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = darkMode,
                onCheckedChange = {
                    darkMode = it
                    userViewModel.toggleDarkMode()
                }
            )
        }

        Divider(Modifier.padding(vertical = 12.dp))

        Text("App-Verwaltung", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Löschen",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(8.dp))
            Text("Alle Fundsachen löschen")
        }
    }
}
