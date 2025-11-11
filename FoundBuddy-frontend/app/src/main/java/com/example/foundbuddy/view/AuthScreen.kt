package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.foundbuddy.controller.UserViewModel

@Composable
fun AuthScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegisterMode) "Account erstellen" else "Anmelden",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4A2D68)
        )

        Spacer(Modifier.height(24.dp))

        if (isRegisterMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || (isRegisterMode && username.isBlank())) {
                    error = "Bitte alle Felder ausfüllen"
                } else {
                    if (isRegisterMode) {
                        val ok = userViewModel.register(username, email, password)
                        if (ok) onLoginSuccess() else error = "E-Mail wird bereits verwendet"
                    } else {
                        val ok = userViewModel.login(email, password)
                        if (ok) onLoginSuccess() else error = "Falsche E-Mail oder Passwort"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A4B9A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegisterMode) "Registrieren" else "Login", color = Color.White)
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))

        ClickableText(
            text = AnnotatedString(
                if (isRegisterMode)
                    "Ich habe schon ein Konto → Anmelden"
                else
                    "Noch kein Konto? Jetzt registrieren"
            ),
            onClick = { isRegisterMode = !isRegisterMode },
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7A4B9A))
        )
    }
}
