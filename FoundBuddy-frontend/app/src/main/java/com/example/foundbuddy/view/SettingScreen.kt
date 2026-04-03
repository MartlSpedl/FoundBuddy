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
import com.example.foundbuddy.controller.LanguageManager
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
    val scope = rememberCoroutineScope()
    val isDarkMode by userViewModel.isDarkMode.collectAsState()
    val lang by userViewModel.language.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isLoading by userViewModel.isLoading.collectAsState()
    val lmLang = lang

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(LanguageManager.tr("profile", lmLang), style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                scope.launch {
                    userViewModel.updateUsername(it)
                }

            },
            label = { Text(LanguageManager.tr("username", lmLang)) },
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
            Text(LanguageManager.tr("save_profile", lmLang))
        }

        HorizontalDivider()

        Text(LanguageManager.tr("language", lmLang), style = MaterialTheme.typography.titleMedium)
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
            Text(LanguageManager.tr("dark_mode", lmLang), style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isDarkMode, onCheckedChange = { userViewModel.toggleDarkMode() })
        }

        HorizontalDivider()

        Text(LanguageManager.tr("app_management", lmLang), style = MaterialTheme.typography.headlineSmall)

        Button(
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.delete_icon),
                contentDescription = LanguageManager.tr("delete", lmLang)
            )
            Spacer(Modifier.width(8.dp))
            Text(LanguageManager.tr("delete_all_items", lmLang))
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
                contentDescription = LanguageManager.tr("logout", lmLang)
            )
            Spacer(Modifier.width(8.dp))
            Text(LanguageManager.tr("logout", lmLang))
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
                contentDescription = LanguageManager.tr("delete_account", lmLang)
            )
            Spacer(Modifier.width(8.dp))
            Text(LanguageManager.tr("delete_account", lmLang))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(LanguageManager.tr("delete_account", lmLang) + "?") },
            text = {
                Text(LanguageManager.tr("delete_account_confirm", lmLang))
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
                    Text(LanguageManager.tr("delete", lmLang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(LanguageManager.tr("cancel", lmLang))
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
