package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import com.example.foundbuddy.data.UserRepository
import com.example.foundbuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    suspend fun register(username: String, email: String, password: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank()) return false

        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = username,
            email = email,
            password = password,
            profileImage = null
        )

        val created = api.create(newUser)
        if (created != null) {
            _currentUserFlow.value = created
            _username.value = created.username
            _email.value = created.email
            return true
        }
        return false
    }

    suspend fun login(email: String, password: String): Boolean {
        val users = api.getAll()
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
