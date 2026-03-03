package com.example.foundbuddy.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foundbuddy.controller.LoginResult
import com.example.foundbuddy.controller.RegisterResult
import com.example.foundbuddy.controller.UserViewModel
import kotlinx.coroutines.launch

// ─── Farben im Instagram-Stil ──────────────────────────────────────────────
private val IgBackground   = Color(0xFFFFFFFF)
private val IgFieldBg      = Color(0xFFFAFAFA)
private val IgBorder       = Color(0xFFDBDBDB)
private val IgTextPrimary  = Color(0xFF262626)
private val IgTextSecondary = Color(0xFF8E8E8E)
private val IgBlue         = Color(0xFF0095F6)
private val IgBlueDisabled = Color(0xFFB2DFFC)
private val IgDivider      = Color(0xFFDBDBDB)
private val IgError        = Color(0xFFED4956)

// Instagram-typischer Logofarb-Gradient
private val IgGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
)

@Composable
fun AuthScreen(
    userViewModel: UserViewModel,
    onLoginSuccess: () -> Unit
) {
    var email                  by remember { mutableStateOf("") }
    var password               by remember { mutableStateOf("") }
    var username               by remember { mutableStateOf("") }
    var isRegister             by remember { mutableStateOf(false) }
    var isLoading              by remember { mutableStateOf(false) }
    var passwordVisible        by remember { mutableStateOf(false) }

    var usernameErrors         by remember { mutableStateOf<List<String>>(emptyList()) }
    var emailErrors            by remember { mutableStateOf<List<String>>(emptyList()) }
    var passwordErrors         by remember { mutableStateOf<List<String>>(emptyList()) }
    var generalError           by remember { mutableStateOf("") }

    var showEmailConfirmation  by remember { mutableStateOf(false) }
    var registeredEmail        by remember { mutableStateOf("") }
    var showEmailNotVerified   by remember { mutableStateOf(false) }
    var unverifiedEmail        by remember { mutableStateOf("") }
    var showResetPassword      by remember { mutableStateOf(false) }
    var resetEmail             by remember { mutableStateOf("") }
    var resetInfo              by remember { mutableStateOf("") }

    val currentUser by userViewModel.currentUserFlow.collectAsState(initial = null)
    val scope       = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) onLoginSuccess()
    }
    LaunchedEffect(isRegister) {
        usernameErrors = emptyList(); emailErrors = emptyList()
        passwordErrors = emptyList(); generalError = ""
        showEmailConfirmation = false; showEmailNotVerified = false
        showResetPassword = false; resetInfo = ""
    }

    // ─── Dialoge ──────────────────────────────────────────────────────────

    if (showEmailConfirmation) {
        IgAlertDialog(
            title = "Registrierung erfolgreich!",
            text = "Wir haben eine Bestätigungs-E-Mail an $registeredEmail gesendet. " +
                    "Bitte klicke auf den Link in der E-Mail, um dein Konto zu aktivieren.",
            confirmText = "Zur Anmeldung",
            onConfirm = {
                showEmailConfirmation = false
                isRegister = false
                email = registeredEmail
                password = ""
            },
            onDismiss = { showEmailConfirmation = false }
        )
    }

    if (showEmailNotVerified) {
        IgAlertDialog(
            title = "E-Mail nicht bestätigt",
            text = "Deine E-Mail-Adresse wurde noch nicht bestätigt. " +
                    "Bitte prüfe dein Postfach und klicke auf den Bestätigungslink.",
            confirmText = "OK",
            onConfirm = { showEmailNotVerified = false },
            onDismiss = { showEmailNotVerified = false },
            dismissText = if (isLoading) null else "E-Mail erneut senden",
            onDismissClick = {
                scope.launch {
                    isLoading = true
                    val ok = userViewModel.resendVerificationEmail(unverifiedEmail)
                    isLoading = false
                    if (ok) {
                        generalError = ""
                        showEmailNotVerified = false
                        showEmailConfirmation = true
                        registeredEmail = unverifiedEmail
                    } else {
                        generalError = "Fehler beim Senden der E-Mail"
                    }
                }
            }
        )
    }

    if (showResetPassword) {
        AlertDialog(
            onDismissRequest = { showResetPassword = false },
            containerColor = IgBackground,
            title = {
                Text(
                    "Passwort zurücksetzen",
                    color = IgTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    Text(
                        "Gib deine E-Mail-Adresse ein. Wir senden dir einen Reset-Link.",
                        color = IgTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    IgTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = "E-Mail-Adresse",
                        keyboardType = KeyboardType.Email
                    )
                    if (resetInfo.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(resetInfo, color = IgTextSecondary, fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (resetEmail.isBlank()) { resetInfo = "Bitte E-Mail eingeben."; return@launch }
                            isLoading = true
                            val ok = userViewModel.requestPasswordReset(resetEmail.trim())
                            isLoading = false
                            resetInfo = if (ok)
                                "Reset-Mail wurde gesendet (falls ein Account existiert)."
                            else
                                "Senden fehlgeschlagen. Bitte versuche es später."
                        }
                    },
                    enabled = !isLoading
                ) { Text("Senden", color = IgBlue, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetPassword = false; resetInfo = "" }) {
                    Text("Abbrechen", color = IgTextPrimary)
                }
            }
        )
    }

    // ─── Hauptlayout ──────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IgBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(48.dp))

            // ── Logo ──────────────────────────────────────────────────────
            Text(
                text = "FoundBuddy",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive,
                color = IgTextPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // ── Felder ───────────────────────────────────────────────────
            if (isRegister) {
                IgTextField(
                    value    = username,
                    onValueChange = { username = it; usernameErrors = emptyList() },
                    placeholder  = "Benutzername",
                    isError      = usernameErrors.isNotEmpty(),
                    errorText    = usernameErrors.firstOrNull()
                )
                Spacer(Modifier.height(8.dp))
            }

            IgTextField(
                value         = email,
                onValueChange = { email = it; emailErrors = emptyList() },
                placeholder   = "E-Mail-Adresse",
                keyboardType  = KeyboardType.Email,
                isError       = emailErrors.isNotEmpty(),
                errorText     = emailErrors.firstOrNull()
            )
            Spacer(Modifier.height(8.dp))

            IgTextField(
                value         = password,
                onValueChange = { password = it; passwordErrors = emptyList() },
                placeholder   = "Passwort",
                isPassword    = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                isError       = passwordErrors.isNotEmpty(),
                errorText     = passwordErrors.firstOrNull()
                    ?: if (isRegister && passwordErrors.isEmpty()) null else null
            )

            // Passwort-Hinweis bei Registrierung
            if (isRegister && passwordErrors.isEmpty()) {
                Text(
                    "Mind. 8 Zeichen, Groß-/Kleinbuchstaben, Zahl, Sonderzeichen",
                    fontSize   = 11.sp,
                    color      = IgTextSecondary,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp)
                )
            }

            // Passwort vergessen – nur im Login
            if (!isRegister) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(
                        onClick  = { resetEmail = email; resetInfo = ""; showResetPassword = true },
                        enabled  = !isLoading,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Passwort vergessen?", color = IgBlue, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Fehlermeldung
            if (generalError.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    generalError,
                    color     = IgError,
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Haupt-Button ──────────────────────────────────────────────
            IgPrimaryButton(
                text      = if (isRegister) "Registrieren" else "Anmelden",
                isLoading = isLoading,
                enabled   = !isLoading
            ) {
                usernameErrors = emptyList(); emailErrors = emptyList()
                passwordErrors = emptyList(); generalError = ""

                scope.launch {
                    isLoading = true
                    if (isRegister) {
                        when (val result = userViewModel.register(username, email, password)) {
                            is RegisterResult.Success -> {
                                registeredEmail = email
                                showEmailConfirmation = true
                                username = ""; password = ""
                            }
                            is RegisterResult.ValidationErrors -> {
                                usernameErrors = result.errors["username"] ?: emptyList()
                                emailErrors    = result.errors["email"]    ?: emptyList()
                                passwordErrors = result.errors["password"] ?: emptyList()
                            }
                            is RegisterResult.Error       -> generalError = result.message
                            is RegisterResult.ServerError -> generalError = result.message
                        }
                    } else {
                        when (val result = userViewModel.login(email, password)) {
                            is LoginResult.Success          -> { /* LaunchedEffect übernimmt */ }
                            is LoginResult.InvalidCredentials -> generalError = "Falsche E-Mail oder Passwort"
                            is LoginResult.EmailNotVerified -> {
                                unverifiedEmail = result.email
                                showEmailNotVerified = true
                            }
                            is LoginResult.ServerError      -> generalError = result.message
                        }
                    }
                    isLoading = false
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── ODER-Trennlinie ───────────────────────────────────────────
            IgDividerRow()

            Spacer(Modifier.height(20.dp))

            // ── Zwischen Anmelden/Registrieren wechseln ───────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text     = if (isRegister) "Du hast bereits ein Konto?" else "Du hast noch kein Konto?",
                    color    = IgTextSecondary,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick  = { isRegister = !isRegister },
                    enabled  = !isLoading,
                    contentPadding = PaddingValues(start = 4.dp)
                ) {
                    Text(
                        text       = if (isRegister) "Anmelden" else "Registrieren",
                        color      = IgBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Wiederverwendbare Komponenten ─────────────────────────────────────────

@Composable
private fun IgTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(IgFieldBg)
                .border(
                    width = 1.dp,
                    color = if (isError) IgError else IgBorder,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            BasicIgField(
                value            = value,
                onValueChange    = onValueChange,
                placeholder      = placeholder,
                keyboardType     = keyboardType,
                isPassword       = isPassword,
                passwordVisible  = passwordVisible,
                onPasswordToggle = onPasswordToggle
            )
        }
        if (isError && errorText != null) {
            Text(
                text     = errorText,
                color    = IgError,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun BasicIgField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean,
    passwordVisible: Boolean,
    onPasswordToggle: (() -> Unit)?
) {
    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    TextField(
        value              = value,
        onValueChange      = onValueChange,
        placeholder        = { Text(placeholder, color = IgTextSecondary, fontSize = 14.sp) },
        singleLine         = true,
        visualTransformation = visualTransformation,
        keyboardOptions    = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = IgFieldBg,
            unfocusedContainerColor = IgFieldBg,
            disabledContainerColor  = IgFieldBg,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor        = IgTextPrimary,
            unfocusedTextColor      = IgTextPrimary,
            cursorColor             = IgBlue
        ),
        trailingIcon = if (isPassword && onPasswordToggle != null) {
            {
                TextButton(
                    onClick        = onPasswordToggle,
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    Text(
                        text       = if (passwordVisible) "Ausblenden" else "Anzeigen",
                        color      = IgTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun IgPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(8.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = IgBlue,
            disabledContainerColor = IgBlueDisabled
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .animateContentSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text       = text,
                color      = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
        }
    }
}

@Composable
private fun IgDividerRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = IgDivider,
            thickness = 1.dp
        )
        Text(
            text     = "  ODER  ",
            color    = IgTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            color     = IgDivider,
            thickness = 1.dp
        )
    }
}

@Composable
private fun IgAlertDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = IgBackground,
        title = {
            Text(title, color = IgTextPrimary, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(text, color = IgTextSecondary, fontSize = 14.sp)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = IgBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = if (dismissText != null && onDismissClick != null) {
            {
                TextButton(onClick = onDismissClick) {
                    Text(dismissText, color = IgTextPrimary)
                }
            }
        } else null
    )
}
