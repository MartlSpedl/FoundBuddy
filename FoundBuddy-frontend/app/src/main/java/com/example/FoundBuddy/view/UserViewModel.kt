package com.example.FoundBuddy.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FoundBuddy.data.UserRepository
import com.example.FoundBuddy.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel zur Verwaltung des aktuellen Benutzers.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _user = MutableStateFlow<User?>(null)
    /** Aktueller Benutzer als StateFlow für Compose. */
    val user: StateFlow<User?> = _user

    init {
        // Beim Start versuchen, gespeicherten Benutzer zu laden
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
    }

    /** Speichert den Benutzer und aktualisiert den State. */
    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
            _user.value = user
        }
    }
}
