package com.example.ch06.home

import com.example.ch06.data.Article

// Single source of truth untuk layar Home
data class HomeUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    // Teks pencarian yang sedang diketik hidup di UiState, bukan di composable
    val query: String = "",
    val errorMessage: String? = null
) {
    // Properti turunan (getter), BUKAN field yang disinkronkan manual:
    // true hanya jika tidak loading, tidak error, dan daftar hasil kosong.
    val isEmptyResult: Boolean
        get() = !isLoading && errorMessage == null && articles.isEmpty()
}
