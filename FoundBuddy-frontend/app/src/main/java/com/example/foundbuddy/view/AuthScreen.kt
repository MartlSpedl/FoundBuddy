package com.example.foundbuddy.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foundbuddy.controller.UserViewModel

@Composable
fun AuthScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
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
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                errorMessage = ""
                if (isRegister) {
                    val success = userViewModel.register(username, email, password)
                    if (!success) {
                        errorMessage = "E-Mail schon vergeben oder Eingaben unvollständig!"
                    }
                } else {
                    val success = userViewModel.login(email, password)
                    if (!success) {
                        errorMessage = "Falsche Anmeldedaten!"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(if (isRegister) "Registrieren" else "Anmelden", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { isRegister = !isRegister }) {
            Text(
                if (isRegister) "Schon einen Account? Anmelden"
                else "Kein Account? Registrieren"
            )
        }
    }
}
