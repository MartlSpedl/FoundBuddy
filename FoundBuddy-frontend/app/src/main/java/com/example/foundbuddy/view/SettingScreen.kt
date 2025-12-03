package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf(userViewModel.username.value) }
    var email by remember { mutableStateOf(userViewModel.email.value) }
    val scope = rememberCoroutineScope()
    val isDarkMode by userViewModel.isDarkMode.collectAsState()

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
                scope.launch {
                    userViewModel.updateUsername(it)
                }

            },
            label = { Text("Benutzername") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("E-Mail-Adresse") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    userViewModel.updateUsername(username)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Profil speichern")
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Mode aktivieren", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDarkMode, onCheckedChange = { userViewModel.toggleDarkMode() })
        }

        HorizontalDivider()

        Text("App-Verwaltung", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.delete_icon),
                contentDescription = "Löschen"
            )
            Spacer(Modifier.width(8.dp))
            Text("Alle Fundsachen löschen")
        }
    }
}
