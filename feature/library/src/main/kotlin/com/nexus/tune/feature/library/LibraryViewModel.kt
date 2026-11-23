package com.nexus.tune.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    val likedTracks: StateFlow<List<Track>> = musicRepository.getLikedTracks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historyTracks: StateFlow<List<Track>> = musicRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun playTrack(track: Track, queue: List<Track>) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, queue)
        }
    }
}

