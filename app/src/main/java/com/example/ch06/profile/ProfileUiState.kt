package com.example.ch06.profile

// Single source of truth untuk layar Profil
data class ProfileUiState(
    val username: String = "",
    val notificationsEnabled: Boolean = false
)
