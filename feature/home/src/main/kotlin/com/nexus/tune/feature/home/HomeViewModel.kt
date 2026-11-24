package com.nexus.tune.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.mood.MoodRadioEngine
import com.nexus.tune.core.domain.usecase.GetHomeFeedUseCase
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.MoodCategory
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val playlists: List<Playlist>,
        val selectedMood: MoodCategory = MoodCategory.ALL
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getHomeFeedUseCase: GetHomeFeedUseCase,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedMood = MutableStateFlow(MoodCategory.ALL)
    val selectedMood: StateFlow<MoodCategory> = _selectedMood.asStateFlow()

    private var rawPlaylists: List<Playlist> = emptyList()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val result = getHomeFeedUseCase()) {
                is MusicResult.Success -> {
                    rawPlaylists = result.data
                    applyMoodFilter(_selectedMood.value)
                }
                is MusicResult.Failure -> {
                    _uiState.value = HomeUiState.Error(result.error.message)
                }
            }
        }
    }

    fun selectMood(mood: MoodCategory) {
        _selectedMood.value = mood
        applyMoodFilter(mood)
    }

    private fun applyMoodFilter(mood: MoodCategory) {
        if (rawPlaylists.isEmpty()) return
        val filtered = MoodRadioEngine.filterPlaylistsByMood(rawPlaylists, mood)
        _uiState.value = HomeUiState.Success(
            playlists = filtered,
            selectedMood = mood
        )
    }

    fun startMoodRadio(mood: MoodCategory = _selectedMood.value) {
        viewModelScope.launch {
            val allTracks = rawPlaylists.flatMap { it.tracks }
            val radioTracks = MoodRadioEngine.createMoodRadio(allTracks, mood, count = 20)
            if (radioTracks.isNotEmpty()) {
                manageQueueUseCase.playTrackNow(radioTracks.first(), radioTracks)
            }
        }
    }

    fun playTrack(track: Track, playlist: List<Track>) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, playlist)
        }
    }
}
