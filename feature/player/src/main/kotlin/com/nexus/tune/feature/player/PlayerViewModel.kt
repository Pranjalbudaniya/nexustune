package com.nexus.tune.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.TrackLyrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    private val _lyrics = MutableStateFlow<TrackLyrics?>(null)
    val lyrics: StateFlow<TrackLyrics?> = _lyrics.asStateFlow()

    private val _lyricsOffsetMs = MutableStateFlow(0L)
    val lyricsOffsetMs: StateFlow<Long> = _lyricsOffsetMs.asStateFlow()

    fun adjustLyricsOffset(deltaMs: Long) {
        _lyricsOffsetMs.value += deltaMs
    }

    fun resetLyricsOffset() {
        _lyricsOffsetMs.value = 0L
    }

    fun loadLyrics(track: Track) {
        viewModelScope.launch {
            when (val result = musicRepository.getLyrics(track.id, track.title, track.artist, track.durationSeconds)) {
                is MusicResult.Success -> _lyrics.value = result.data
                is MusicResult.Failure -> _lyrics.value = null
            }
        }
    }

    fun toggleLike(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleLike(track)
        }
    }
}

