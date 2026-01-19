package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foundbuddy.controller.LoginResult
import com.example.foundbuddy.controller.RegisterResult
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Fehler pro Feld
    var usernameErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var passwordErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    var generalError by remember { mutableStateOf("") }

    // Registrierung erfolgreich - Email-Bestätigung anzeigen
    var showEmailConfirmation by remember { mutableStateOf(false) }
    var registeredEmail by remember { mutableStateOf("") }

    // Email nicht verifiziert
    var showEmailNotVerified by remember { mutableStateOf(false) }
    var unverifiedEmail by remember { mutableStateOf("") }

    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    // Passwort zurücksetzen
    var showResetPassword by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetInfo by remember { mutableStateOf("") }

    // Wenn der User erfolgreich eingeloggt wurde → weiterleiten
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    // Fehler zurücksetzen wenn Modus wechselt
    LaunchedEffect(isRegister) {
        usernameErrors = emptyList()
        emailErrors = emptyList()
        passwordErrors = emptyList()
        generalError = ""
        showEmailConfirmation = false
        showEmailNotVerified = false

        // Reset-Dialog sauber schließen
        showResetPassword = false
        resetInfo = ""
    }

    // Email-Bestätigung Dialog
    if (showEmailConfirmation) {
        AlertDialog(
            onDismissRequest = { showEmailConfirmation = false },
            title = { Text("Registrierung erfolgreich!") },
            text = {
                Column {
                    Text("Wir haben dir eine Bestätigungs-E-Mail an")
                    Text(
                        registeredEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("gesendet.")
                    Spacer(Modifier.height(8.dp))
                    Text("Bitte klicke auf den Link in der E-Mail, um dein Konto zu aktivieren.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEmailConfirmation = false
                    isRegister = false
                    email = registeredEmail
                    password = ""
                }) {
                    Text("Zur Anmeldung")
                }
            }
        )
    }

    // Email nicht verifiziert Dialog
    if (showEmailNotVerified) {
        AlertDialog(
            onDismissRequest = { showEmailNotVerified = false },
            title = { Text("E-Mail nicht bestätigt") },
            text = {
                Column {
                    Text("Deine E-Mail-Adresse wurde noch nicht bestätigt.")
                    Spacer(Modifier.height(8.dp))
                    Text("Bitte prüfe dein Postfach und klicke auf den Bestätigungslink.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmailNotVerified = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val success = userViewModel.resendVerificationEmail(unverifiedEmail)
                            isLoading = false
                            if (success) {
                                generalError = ""
                                showEmailNotVerified = false
                                showEmailConfirmation = true
                                registeredEmail = unverifiedEmail
                            } else {
                                generalError = "Fehler beim Senden der E-Mail"
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("E-Mail erneut senden")
                }
            }
        )
    }

    // >>> Passwort zurücksetzen Dialog
    if (showResetPassword) {
        AlertDialog(
            onDismissRequest = { showResetPassword = false },
            title = { Text("Passwort zurücksetzen") },
            text = {
                Column {
                    Text(
                        "Gib deine E-Mail-Adresse ein. Wenn ein Account existiert, senden wir dir eine Reset-Mail."
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("E-Mail") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (resetInfo.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            resetInfo,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val mail = resetEmail.trim()
                            if (mail.isBlank()) {
                                resetInfo = "Bitte E-Mail eingeben."
                                return@launch
                            }

                            isLoading = true
                            val ok = userViewModel.requestPasswordReset(mail)
                            isLoading = false

                            // Security Best Practice: nicht verraten, ob Account existiert
                            resetInfo = if (ok) {
                                "Wenn ein Account existiert, wurde eine E-Mail gesendet."
                            } else {
                                "Senden fehlgeschlagen. Bitte später erneut versuchen."
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Reset-Mail senden")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showResetPassword = false
                        resetInfo = ""
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegister) "Registrieren" else "Anmelden",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(40.dp))

        if (isRegister) {
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameErrors = emptyList()
                },
                label = { Text("Benutzername") },
                isError = usernameErrors.isNotEmpty(),
                supportingText = if (usernameErrors.isNotEmpty()) {
                    { Text(usernameErrors.joinToString("\n"), color = MaterialTheme.colorScheme.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailErrors = emptyList()
            },
            label = { Text("E-Mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailErrors.isNotEmpty(),
            supportingText = if (emailErrors.isNotEmpty()) {
                { Text(emailErrors.joinToString("\n"), color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordErrors = emptyList()
            },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordErrors.isNotEmpty(),
            supportingText = if (passwordErrors.isNotEmpty()) {
                {
                    Column {
                        passwordErrors.forEach { error ->
                            Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            } else if (isRegister) {
                { Text("Mind. 8 Zeichen, Groß-/Kleinbuchstaben, Zahl, Sonderzeichen", fontSize = 11.sp) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // >>> "Passwort vergessen?" Button nur im Login-Modus
        if (!isRegister) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        resetEmail = email // übernimmt die E-Mail aus dem Login-Feld
                        resetInfo = ""
                        showResetPassword = true
                    },
                    enabled = !isLoading
                ) {
                    Text("Passwort vergessen?")
                }
            }
        }

        if (generalError.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                generalError,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // Fehler zurücksetzen
                usernameErrors = emptyList()
                emailErrors = emptyList()
                passwordErrors = emptyList()
                generalError = ""

                scope.launch {
                    isLoading = true
                    if (isRegister) {
                        when (val result = userViewModel.register(username, email, password)) {
                            is RegisterResult.Success -> {
                                registeredEmail = email
                                showEmailConfirmation = true
                                // Felder zurücksetzen
                                username = ""
                                password = ""
                            }
                            is RegisterResult.ValidationErrors -> {
                                usernameErrors = result.errors["username"] ?: emptyList()
                                emailErrors = result.errors["email"] ?: emptyList()
                                passwordErrors = result.errors["password"] ?: emptyList()
                            }
                            is RegisterResult.Error -> {
                                generalError = result.message
                            }
                        }
                    } else {
                        when (val result = userViewModel.login(email, password)) {
                            is LoginResult.Success -> {
                                // onLoginSuccess wird durch LaunchedEffect aufgerufen
                            }
                            is LoginResult.InvalidCredentials -> {
                                generalError = "Falsche E-Mail oder Passwort"
                            }
                            is LoginResult.EmailNotVerified -> {
                                unverifiedEmail = result.email
                                showEmailNotVerified = true
                            }
                        }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (isRegister) "Registrieren" else "Anmelden",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { isRegister = !isRegister },
            enabled = !isLoading
        ) {
            Text(
                if (isRegister) "Schon einen Account? Anmelden"
                else "Kein Account? Registrieren"
            )
        }
    }
}
