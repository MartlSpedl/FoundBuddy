package com.example.foundbuddy.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foundbuddy.data.BackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BackendStatusState(
    val isLoading: Boolean = false,
    val message: String = "Noch nicht getestet"
)

class BackendStatusViewModel(
    private val repo: BackendRepository = BackendRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BackendStatusState())
    val state: StateFlow<BackendStatusState> = _state

    fun testConnection() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, message = "Teste Verbindung...")
            val result = repo.pingBackend()
            _state.value = if (result.isSuccess) {
                BackendStatusState(isLoading = false, message = "Backend OK: ${result.getOrNull()}")
            } else {
                BackendStatusState(isLoading = false, message = "Fehler: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
