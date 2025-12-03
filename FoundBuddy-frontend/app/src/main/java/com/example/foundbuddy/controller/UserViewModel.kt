package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.data.RegistrationResult
import com.example.foundbuddy.data.UserRepository
import com.example.foundbuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

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

    fun logout() {
        _currentUserFlow.value = null
        _username.value = ""
        _email.value = ""
    }

    suspend fun updateUsername(newName: String) {
        val user = _currentUserFlow.value ?: return
        val updated = user.copy(username = newName)
        api.update(updated)
        _currentUserFlow.value = updated
        _username.value = newName
    }

    suspend fun updateProfileImage(uri: String?) {
        val user = _currentUserFlow.value ?: return
        val updated = user.copy(profileImage = uri)
        api.update(updated)
        _currentUserFlow.value = updated
    }


    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
