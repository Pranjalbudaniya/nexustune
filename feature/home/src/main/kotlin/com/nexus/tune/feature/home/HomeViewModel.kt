package com.nexus.tune.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.usecase.GetHomeFeedUseCase
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val playlists: List<Playlist>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getHomeFeedUseCase: GetHomeFeedUseCase,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val result = getHomeFeedUseCase()) {
                is MusicResult.Success -> {
                    _uiState.value = HomeUiState.Success(result.data)
                }
                is MusicResult.Failure -> {
                    _uiState.value = HomeUiState.Error(result.error.message)
                }
            }
        }
    }

    fun playTrack(track: Track, playlist: List<Track>) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, playlist)
        }
    }
}
