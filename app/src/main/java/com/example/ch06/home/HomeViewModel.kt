package com.example.ch06.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch06.data.Article
import com.example.ch06.data.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Daftar LENGKAP hasil load. Privat: UI hanya boleh melihat hasil yang sudah difilter.
    private var allArticles: List<Article> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.getArticles() }
                .onSuccess { articles ->
                    allArticles = articles
                    // Pakai query yang SAAT INI ada, agar teks yang sudah diketik
                    // tidak hilang (dan hasilnya tetap terfilter) saat refresh() dipanggil.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articles = filter(allArticles, it.query)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal memuat artikel"
                        )
                    }
                }
        }
    }

    // Event dari UI: UI hanya melaporkan teks; keputusan filter dibuat di sini.
    fun onQueryChange(newQuery: String) {
        _uiState.update {
            it.copy(
                query = newQuery,
                articles = filter(allArticles, newQuery)
            )
        }
    }

    // Aturan filter: judul ATAU kategori mengandung query, tidak peka huruf besar/kecil.
    // Query kosong/blank = tampilkan semua.
    private fun filter(source: List<Article>, query: String): List<Article> {
        if (query.isBlank()) return source
        return source.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }
}
