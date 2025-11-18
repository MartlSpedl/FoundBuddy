package com.example.foundbuddy.controller

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.foundbuddy.model.User

class UserViewModel : ViewModel() {

    var currentUser = mutableStateOf<User?>(null)
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var isDarkMode = mutableStateOf(false)

    private val users = mutableListOf<User>()

    fun register(username: String, email: String, password: String): Boolean {
        if (users.any { it.email == email }) return false
        val newUser = User(username = username, email = email, password = password)
        users.add(newUser)
        currentUser.value = newUser
        this.username.value = newUser.username
        this.email.value = newUser.email
        return true
    }

    fun login(email: String, password: String): Boolean {
        val user = users.find { it.email == email && it.password == password }
        if (user != null) {
            currentUser.value = user
            username.value = user.username
            this.email.value = user.email
            return true
        }
        return false
    }

    fun logout() {
        currentUser.value = null
    }

    fun updateUsername(newName: String) {
        username.value = newName
        currentUser.value = currentUser.value?.copy(username = newName)
    }

    fun updateEmail(newEmail: String) {
        email.value = newEmail
        currentUser.value = currentUser.value?.copy(email = newEmail)
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun updateProfileImage(uri: String?) {
        currentUser.value = currentUser.value?.copy(profileImage = uri)
    }
}
