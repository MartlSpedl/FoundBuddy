package com.example.foundbuddy.controller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.User

class UserViewModel : ViewModel() {

    // Aktuell eingeloggter Benutzer (Frontend-only)
    var currentUser = mutableStateOf<User?>(null)

    // Profileinstellungen
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var darkModeEnabled = mutableStateOf(false)

    // Dummy-Daten für Registrierung / Login
    private val userList = mutableListOf<User>()

    // Registrierung
    fun register(username: String, email: String, password: String): Boolean {
        if (userList.any { it.email == email }) return false
        val newUser = User(username = username, email = email, password = password)
        userList.add(newUser)
        currentUser.value = newUser
        this.username.value = newUser.username
        this.email.value = newUser.email
        return true
    }

    // Login
    fun login(email: String, password: String): Boolean {
        val user = userList.find { it.email == email && it.password == password }
        if (user != null) {
            currentUser.value = user
            username.value = user.username
            this.email.value = user.email
            return true
        }
        return false
    }

    // Logout
    fun logout() {
        currentUser.value = null
    }

    // Profil aktualisieren
    fun updateUsername(newName: String) {
        username.value = newName
        currentUser.value = currentUser.value?.copy(username = newName)
    }

    fun updateEmail(newEmail: String) {
        email.value = newEmail
        currentUser.value = currentUser.value?.copy(email = newEmail)
    }

    // Dark Mode umschalten
    fun toggleDarkMode() {
        darkModeEnabled.value = !darkModeEnabled.value
    }
}
