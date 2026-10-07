package com.example.ch07.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch07.data.NoteEntity
import com.example.ch07.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteListUiState(
    val isLoading: Boolean = false,
    val notes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val recentlyDeleted: NoteEntity? = null,
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val errorMessage: String? = null
) {
    val isFiltering: Boolean
        get() = query.isNotBlank() || selectedTag != null
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NoteListViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val queryText = MutableStateFlow("")
    private val loadError = MutableStateFlow<String?>(null)
    private val recentlyDeleted = MutableStateFlow<NoteEntity?>(null)
    private val selectedTag = MutableStateFlow<String?>(null)

    private val searchResults: Flow<List<NoteEntity>> = queryText
        .debounce { query -> if (query.isBlank()) 0L else 300L }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllNotes()
            else repository.searchNotes(query)
        }
        .catch { e ->
            loadError.value = e.message ?: "Gagal memuat catatan"
            emit(emptyList())
        }

    private val tagFilterState = combine(selectedTag, repository.getAllNotes()) { selected, allNotes ->
        val available = allNotes.flatMap { it.tags }.distinct().sorted()
        val activeTag = if (selected != null && selected in available) selected else null
        activeTag to available
    }

    val uiState: StateFlow<NoteListUiState> = combine(
        searchResults,
        queryText,
        loadError,
        recentlyDeleted,
        tagFilterState
    ) { results, query, error, deleted, (activeTag, available) ->
        val filteredNotes = if (activeTag != null) {
            results.filter { activeTag in it.tags }
        } else {
            results
        }
        NoteListUiState(
            notes = filteredNotes,
            query = query,
            recentlyDeleted = deleted,
            availableTags = available,
            selectedTag = activeTag,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NoteListUiState(isLoading = true)
    )

    fun onQueryChange(newQuery: String) {
        queryText.value = newQuery
    }

    fun deleteNote(note: NoteEntity) {
        recentlyDeleted.value = note
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun undoDelete() {
        val noteToRestore = recentlyDeleted.value ?: return
        recentlyDeleted.value = null
        viewModelScope.launch { repository.insertNote(noteToRestore) }
    }

    fun onUndoDismissed() {
        recentlyDeleted.value = null
    }

    fun onTagSelected(tag: String?) {
        selectedTag.value = if (selectedTag.value == tag) null else tag
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }
}
