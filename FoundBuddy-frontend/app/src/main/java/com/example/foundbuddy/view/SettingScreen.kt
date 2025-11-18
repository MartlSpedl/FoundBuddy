package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val isDarkMode by userViewModel.isDarkMode
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Profil",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                userViewModel.updateUsername(it)
            },
            label = { Text("Benutzername", color = colors.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                userViewModel.updateEmail(it)
            },
            label = { Text("E-Mail-Adresse", color = colors.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface)
        )

        Button(
            onClick = {
                userViewModel.updateUsername(username)
                userViewModel.updateEmail(email)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text("Profil speichern", color = colors.onPrimary)
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = colors.outlineVariant)

        // Dark Mode Umschalten
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Dark Mode aktivieren",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onBackground
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = { userViewModel.toggleDarkMode() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primary,
                    checkedTrackColor = colors.primaryContainer
                )
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = colors.outlineVariant)

        Text(
            "App-Verwaltung",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground
        )

        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.errorContainer,
                contentColor = colors.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Löschen"
            )
            Spacer(Modifier.width(8.dp))
            Text("Alle Fundsachen löschen")
        }
    }
}
