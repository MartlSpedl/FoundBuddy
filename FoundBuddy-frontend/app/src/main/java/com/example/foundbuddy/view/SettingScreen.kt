package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onClear: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf(userViewModel.username.value) }
    var email by remember { mutableStateOf(userViewModel.email.value) }
    val scope = rememberCoroutineScope()
    val isDarkMode by userViewModel.isDarkMode.collectAsState()
    val lang by userViewModel.language.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isLoading by userViewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (lang == "en") "Profile" else "Profil", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                scope.launch {
                    userViewModel.updateUsername(it)
                }

            },
            label = { Text(if (lang == "en") "Username" else "Benutzername") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text(if (lang == "en") "Email Address" else "E-Mail-Adresse") },
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
            Text(if (lang == "en") "Save Profile" else "Profil speichern")
        }

        HorizontalDivider()

        Text(if (lang == "en") "Language" else "Sprache", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = lang == "de",
                onClick = { userViewModel.setLanguage("de") },
                label = { Text("Deutsch") }
            )
            FilterChip(
                selected = lang == "en",
                onClick = { userViewModel.setLanguage("en") },
                label = { Text("English") }
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (lang == "en") "Dark Mode" else "Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDarkMode, onCheckedChange = { userViewModel.toggleDarkMode() })
        }

        HorizontalDivider()

        Text(if (lang == "en") "App Management" else "App-Verwaltung", style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.delete_icon),
                contentDescription = if (lang == "en") "Delete" else "Löschen"
            )
            Spacer(Modifier.width(8.dp))
            Text(if (lang == "en") "Delete All Items" else "Alle Fundsachen löschen")
        }

        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_message),
                contentDescription = if (lang == "en") "Logout" else "Abmelden"
            )
            Spacer(Modifier.width(8.dp))
            Text(if (lang == "en") "Logout" else "Abmelden")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.delete_icon),
                contentDescription = if (lang == "en") "Delete Account" else "Account löschen"
            )
            Spacer(Modifier.width(8.dp))
            Text(if (lang == "en") "Delete Account" else "Account löschen")
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (lang == "en") "Delete Account?" else "Account löschen?") },
            text = {
                Text(
                    if (lang == "en")
                        "Are you sure you want to delete your account? This action cannot be undone."
                    else
                        "Möchtest du deinen Account wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (userViewModel.deleteAccount()) {
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (lang == "en") "Delete" else "Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(if (lang == "en") "Cancel" else "Abbrechen")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
