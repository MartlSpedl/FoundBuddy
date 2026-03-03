package com.example.foundbuddy.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.data.SessionStore
import com.example.foundbuddy.data.UserOperationResult
import com.example.foundbuddy.data.UserRepository
import com.example.foundbuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Ergebnis einer Registrierung für die UI
 */
sealed class RegisterResult {
    data class Success(val user: User, val emailSent: Boolean = true) : RegisterResult()
    data class ValidationErrors(val errors: Map<String, List<String>>) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
    data class ServerError(val message: String) : RegisterResult()
}

/**
 * Ergebnis eines Login-Versuchs
 */
sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data class EmailNotVerified(val email: String) : LoginResult()
    data class ServerError(val message: String) : LoginResult()
}

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val api = UserRepository()
    private val sessionStore = SessionStore(application.applicationContext)

    // User state
    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // UI state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // ✅ Beim App-Start Session wiederherstellen (wenn vorhanden)
        viewModelScope.launch {
            restoreSession()
        }
    }

    private suspend fun restoreSession() {
        val userId = sessionStore.loadUserId() ?: return
        try {
            val user = api.getCurrentUser(userId)
            if (user != null) {
                _currentUserFlow.value = user
                _username.value = user.username
                _email.value = user.email
            } else {
                sessionStore.clear()
            }
        } catch (_: Exception) {
            // Wenn Backend gerade nicht erreichbar ist, lassen wir die Session-ID mal drin.
            // User bleibt dann ggf. "ausgeloggt", bis wieder Netzwerk da ist.
        }
    }

    /**
     * Registriert einen neuen User mit Validierung
     */
    suspend fun register(username: String, email: String, password: String): RegisterResult {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            val errors = mutableMapOf<String, List<String>>()
            if (username.isBlank()) errors["username"] = listOf("Benutzername darf nicht leer sein")
            if (email.isBlank()) errors["email"] = listOf("E-Mail darf nicht leer sein")
            if (password.isBlank()) errors["password"] = listOf("Passwort darf nicht leer sein")
            return RegisterResult.ValidationErrors(errors)
        }

        // Server WarmUp vor Registrierung
        if (!api.isServerReady()) {
            return RegisterResult.ServerError(api.friendlyServerStartingMessage())
        }

        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = username,
            email = email,
            password = password,
            profileImage = null
        )

        return when (val result = api.create(newUser)) {
            is UserOperationResult.Success -> RegisterResult.Success(newUser)
            is UserOperationResult.Error -> RegisterResult.Error(result.message)
        }
    }

    /**
     * Login - prüft auch ob Email verifiziert ist.
     *
     * Kein isServerReady()-Check mehr vor dem Login – stattdessen wird getAll()
     * direkt aufgerufen (hat jetzt 90s Timeout für Render Cold Start).
     * Wenn die User-Liste leer ist, zeigen wir eine klare Fehlermeldung.
     */
    suspend fun login(email: String, password: String): LoginResult {
        try {
            // Eingabe bereinigen (Leerzeichen am Anfang/Ende entfernen)
            val cleanEmail = email.trim()
            val cleanPassword = password.trim()

            // Hole alle Benutzer vom Backend (wartet bis zu 90s auf Cold Start)
            val users = api.getAll()

            // Wenn die Liste leer ist → Server schläft gerade oder Netzwerkfehler
            if (users.isEmpty()) {
                return LoginResult.ServerError(
                    "Server startet gerade (Render Cold Start). Bitte warte 30–60 Sekunden und versuche es erneut."
                )
            }

            println("DEBUG: Gefundene Benutzer: ${users.size}")

            // Suche nach Benutzer mit passender E-Mail (case-insensitive)
            val userByEmail = users.find {
                it.email.equals(cleanEmail, ignoreCase = true)
            }

            if (userByEmail == null) {
                println("DEBUG: Kein Benutzer mit E-Mail '$cleanEmail' gefunden")
                return LoginResult.InvalidCredentials
            }

            // Prüfe Passwort
            if (userByEmail.password != cleanPassword) {
                println("DEBUG: Falsches Passwort für Benutzer: $cleanEmail")
                return LoginResult.InvalidCredentials
            }

            // Prüfe Email-Verifizierung
            if (!userByEmail.emailVerified) {
                println("DEBUG: E-Mail nicht verifiziert für Benutzer: ${userByEmail.email}")
                return LoginResult.EmailNotVerified(userByEmail.email)
            }

            // Login erfolgreich - User im ViewModel speichern
            _currentUserFlow.value = userByEmail
            _username.value = userByEmail.username
            _email.value = userByEmail.email

            // Session speichern
            sessionStore.saveUserId(userByEmail.id)

            println("DEBUG: Login erfolgreich für Benutzer: ${userByEmail.username}")
            return LoginResult.Success

        } catch (e: Exception) {
            println("DEBUG: Login-Fehler: ${e.message}")
            e.printStackTrace()
            return LoginResult.ServerError("Verbindungsfehler: ${e.message}")
        }
    }

    suspend fun resendVerificationEmail(email: String): Boolean {
        return api.resendVerificationEmail(email)
    }

    suspend fun requestPasswordReset(email: String): Boolean {
        return api.requestPasswordReset(email)
    }

    fun logout() {
        viewModelScope.launch {
            sessionStore.clear()
        }
        _currentUserFlow.value = null
        _username.value = ""
        _email.value = ""
    }

    suspend fun updateUsername(newName: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val user = _currentUserFlow.value ?: run {
                _errorMessage.value = "Kein Benutzer angemeldet"
                return false
            }

            val updated = user.copy(username = newName)
            val success = api.update(updated)

            if (success) {
                _currentUserFlow.value = updated
                _username.value = newName
                true
            } else {
                _errorMessage.value = "Benutzername konnte nicht aktualisiert werden"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Fehler beim Aktualisieren des Benutzernamens: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateProfileImage(uri: String?): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val user = _currentUserFlow.value ?: run {
                _errorMessage.value = "Kein Benutzer angemeldet"
                return false
            }

            val updated = user.copy(profileImage = uri)
            val success = api.update(updated)

            if (success) {
                _currentUserFlow.value = updated
                true
            } else {
                _errorMessage.value = "Profilbild konnte nicht aktualisiert werden"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Fehler beim Aktualisieren des Profilbilds: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    suspend fun loadCurrentUser(userId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val user = api.getCurrentUser(userId)
            user?.let {
                _currentUserFlow.value = it
                _username.value = it.username
                _email.value = it.email
            } ?: run {
                _errorMessage.value = "Benutzerdaten konnten nicht geladen werden"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Fehler beim Laden des Benutzers: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
