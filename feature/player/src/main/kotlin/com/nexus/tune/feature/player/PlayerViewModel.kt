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

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _audioOutputDevice = MutableStateFlow(com.nexus.tune.core.model.AudioOutputDevice())
    val audioOutputDevice: StateFlow<com.nexus.tune.core.model.AudioOutputDevice> = _audioOutputDevice.asStateFlow()

    private val _visualizerData = MutableStateFlow(com.nexus.tune.core.model.VisualizerData())
    val visualizerData: StateFlow<com.nexus.tune.core.model.VisualizerData> = _visualizerData.asStateFlow()

    private val _visualizerMode = MutableStateFlow(com.nexus.tune.core.model.VisualizerMode.SPECTRUM_BARS)
    val visualizerMode: StateFlow<com.nexus.tune.core.model.VisualizerMode> = _visualizerMode.asStateFlow()

    fun updateVisualizerData(data: com.nexus.tune.core.model.VisualizerData) {
        _visualizerData.value = data
    }

    fun setVisualizerMode(mode: com.nexus.tune.core.model.VisualizerMode) {
        _visualizerMode.value = mode
    }

    fun updateAudioOutputDevice(device: com.nexus.tune.core.model.AudioOutputDevice) {
        _audioOutputDevice.value = device
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

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

