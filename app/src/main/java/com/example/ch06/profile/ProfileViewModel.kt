package com.example.ch06.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    repository: ProfileRepository
) : ViewModel() {

    // Data profil bersifat lokal dan sinkron, jadi state awal langsung dibaca dari repository
    private val _uiState = MutableStateFlow(
        repository.getProfile().let {
            ProfileUiState(
                username = it.username,
                notificationsEnabled = it.notificationsEnabled
            )
        }
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onUsernameChange(newName: String) {
        _uiState.update { it.copy(username = newName) }
    }

    fun onToggleNotification(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }
}
