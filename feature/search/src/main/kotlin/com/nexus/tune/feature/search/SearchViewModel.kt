package com.nexus.tune.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.domain.usecase.SearchMusicUseCase
import com.nexus.tune.core.model.SearchResult
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val result: SearchResult) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val searchMusicUseCase: SearchMusicUseCase,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _suggestions.value = emptyList()
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _suggestions.value = searchMusicUseCase.getSuggestions(newQuery)
            performSearch(newQuery)
        }
    }

    fun performSearch(queryToSearch: String) {
        if (queryToSearch.isBlank()) return
        _uiState.value = SearchUiState.Loading
        viewModelScope.launch {
            when (val result = searchMusicUseCase(queryToSearch)) {
                is MusicResult.Success -> {
                    _uiState.value = SearchUiState.Success(result.data)
                }
                is MusicResult.Failure -> {
                    _uiState.value = SearchUiState.Error(result.error.message)
                }
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, listOf(track))
        }
    }
}

