package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val users = mutableListOf<User>()

    fun register(username: String, email: String, password: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank()) return false
        if (users.any { it.email.equals(email, ignoreCase = true) }) return false

        val newUser = User(username = username, email = email.lowercase(), password = password)
        users.add(newUser)

        _currentUserFlow.value = newUser
        _username.value = username
        _email.value = email.lowercase()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val user = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        } ?: return false

        _currentUserFlow.value = user
        _username.value = user.username
        _email.value = user.email
        return true
    }

    fun logout() {
        _currentUserFlow.value = null
        _username.value = ""
        _email.value = ""
    }

    fun updateUsername(newName: String) {
        if (newName.isNotBlank()) {
            _username.value = newName
            _currentUserFlow.value = _currentUserFlow.value?.copy(username = newName)
        }
    }

    fun updateProfileImage(uri: String?) {
        _currentUserFlow.value = _currentUserFlow.value?.copy(profileImage = uri)
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
