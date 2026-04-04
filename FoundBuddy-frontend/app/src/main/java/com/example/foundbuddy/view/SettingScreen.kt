package com.example.foundbuddy.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.R
import com.example.foundbuddy.controller.LanguageManager
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    var showDsgvoDialog by remember { mutableStateOf(false) }
    val isLoading by userViewModel.isLoading.collectAsState()
    val lmLang = lang

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageManager.tr("settings", lmLang),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = LanguageManager.tr("account_settings", lmLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        scope.launch {
                            userViewModel.updateUsername(it)
                        }
                    },
                    label = { Text(LanguageManager.tr("username", lmLang)) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("language", lmLang)) },
                    supportingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    },
                    leadingContent = {
                        Icon(Icons.Default.Language, contentDescription = null)
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = LanguageManager.tr("appearance", lmLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("dark_mode", lmLang)) },
                    trailingContent = {
                        Switch(checked = isDarkMode, onCheckedChange = { userViewModel.toggleDarkMode() })
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = LanguageManager.tr("app_management", lmLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("delete_all_items", lmLang)) },
                    supportingContent = { Text("Alle lokal gespeicherten Items entfernen") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.delete_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = {
                        FilledTonalButton(onClick = onClear) {
                            Text(LanguageManager.tr("delete", lmLang))
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("logout", lmLang)) },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = {
                        FilledTonalButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(LanguageManager.tr("logout", lmLang))
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            LanguageManager.tr("delete_account", lmLang),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    supportingContent = {
                        Text(
                            "Account und alle Daten dauerhaft löschen",
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    trailingContent = {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(LanguageManager.tr("delete", lmLang))
                        }
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = LanguageManager.tr("legal", lmLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("copyright", lmLang)) },
                    supportingContent = { Text("© 2025 FoundBuddy Team. All rights reserved.") },
                    leadingContent = {
                        Icon(Icons.Default.Copyright, contentDescription = null)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                ListItem(
                    headlineContent = { Text(LanguageManager.tr("dsgvo", lmLang)) },
                    supportingContent = { Text("Informationen zur Verarbeitung deiner Daten") },
                    leadingContent = {
                        Icon(Icons.Default.Policy, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { showDsgvoDialog = true },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${LanguageManager.tr("version", lmLang)} 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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

    if (showDsgvoDialog) {
        AlertDialog(
            onDismissRequest = { showDsgvoDialog = false },
            title = { Text(LanguageManager.tr("privacy_policy_title", lmLang)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (lang == "de") {
                            """Verantwortlicher für die Datenverarbeitung:
FoundBuddy Team
E-Mail: support@foundbuddy.app

1. Erhobene Daten
Wir erheben folgende personenbezogene Daten:
• Profilinformationen (Benutzername, E-Mail-Adresse)
• Hochgeladene Bilder und Beschreibungen
• Chat-Nachrichten mit anderen Nutzern

2. Zweck der Verarbeitung
Ihre Daten werden verwendet für:
• Bereitstellung der Lost & Found Funktionen
• Kommunikation zwischen Nutzern
• Verbesserung unserer Dienste

3. Speicherdauer
Daten werden so lange gespeichert, wie Ihr Account aktiv ist. Sie können Ihren Account jederzeit löschen, wobei Ihre Daten dann entfernt werden.

4. Ihre Rechte
Sie haben das Recht auf:
• Auskunft über Ihre gespeicherten Daten
• Berichtigung unrichtiger Daten
• Löschung Ihrer Daten
• Beschwerde bei einer Aufsichtsbehörde

5. Kontakt
Bei Fragen zum Datenschutz kontaktieren Sie uns unter:
support@foundbuddy.app"""
                        } else {
                            """Controller for data processing:
FoundBuddy Team
Email: support@foundbuddy.app

1. Data Collected
We collect the following personal data:
• Profile information (username, email address)
• Uploaded images and descriptions
• Chat messages with other users

2. Purpose of Processing
Your data is used for:
• Providing Lost & Found functionality
• Communication between users
• Improving our services

3. Storage Duration
Data is stored as long as your account is active. You can delete your account at any time, after which your data will be removed.

4. Your Rights
You have the right to:
• Access your stored data
• Correction of incorrect data
• Deletion of your data
• Complaint to a supervisory authority

5. Contact
For privacy questions, contact us at:
support@foundbuddy.app"""
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDsgvoDialog = false }) {
                    Text(LanguageManager.tr("close", lmLang))
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
