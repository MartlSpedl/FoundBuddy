package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.data.RegistrationResult
import com.example.foundbuddy.data.UserRepository
import com.example.foundbuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope

/**
 * Ergebnis einer Registrierung für die UI
 */
sealed class RegisterResult {
    data class Success(val user: User, val emailSent: Boolean = true) : RegisterResult()
    data class ValidationErrors(val errors: Map<String, List<String>>) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

/**
 * Ergebnis eines Login-Versuchs
 */
sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data class EmailNotVerified(val email: String) : LoginResult()
}

class UserViewModel : ViewModel() {
    private val api = UserRepository()

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

    private val users = mutableListOf<User>()

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

        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = username,
            email = email,
            password = password,
            profileImage = null
        )

        return when (val result = api.create(newUser)) {
            is RegistrationResult.Success -> {
                // User wurde erstellt, aber noch nicht einloggen (Email muss erst bestätigt werden)
                RegisterResult.Success(result.user)
            }
            is RegistrationResult.ValidationError -> {
                RegisterResult.ValidationErrors(result.errors)
            }
            is RegistrationResult.Error -> {
                RegisterResult.Error(result.message)
            }
        }
    }

    /**
     * Login - prüft auch ob Email verifiziert ist
     */
    suspend fun login(email: String, password: String): LoginResult {
        val users = api.getAll()
        val user = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        } ?: return LoginResult.InvalidCredentials

        if (!user.emailVerified) {
            return LoginResult.EmailNotVerified(user.email)
        }

        _currentUserFlow.value = user
        _username.value = user.username
        _email.value = user.email
        return LoginResult.Success
    }

    suspend fun resendVerificationEmail(email: String): Boolean {
        return api.resendVerificationEmail(email)
    }

    /**
     * Passwort zurücksetzen (Request)
     *
     * Hinweis: Das Backend muss dafür einen Endpoint bereitstellen.
     */
    suspend fun requestPasswordReset(email: String): Boolean {
        return api.requestPasswordReset(email)
    }


    fun logout() {
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
    
    // Load the current user's data
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
    
    // Clear any error messages
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
