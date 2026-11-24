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
    private val manageQueueUseCase: ManageQueueUseCase,
    private val abLoopManager: com.nexus.tune.core.player.ABLoopManager? = null
) : ViewModel() {

    val abLoopState: StateFlow<com.nexus.tune.core.model.ABLoopState> =
        abLoopManager?.loopState ?: MutableStateFlow(com.nexus.tune.core.model.ABLoopState()).asStateFlow()

    fun setABLoopPointA(posMs: Long? = null) {
        abLoopManager?.setPointA(posMs)
    }

    fun setABLoopPointB(posMs: Long? = null) {
        abLoopManager?.setPointB(posMs)
    }

    fun toggleABLoop(enabled: Boolean? = null) {
        abLoopManager?.toggleLoop(enabled)
    }

    fun clearABLoop() {
        abLoopManager?.clearLoop()
    }

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

    private var activeTrackId: String? = null

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
        val newOffset = _lyricsOffsetMs.value + deltaMs
        _lyricsOffsetMs.value = newOffset
        activeTrackId?.let { trackId ->
            viewModelScope.launch {
                musicRepository.saveLyricsOffset(trackId, newOffset)
            }
        }
    }

    fun resetLyricsOffset() {
        _lyricsOffsetMs.value = 0L
        activeTrackId?.let { trackId ->
            viewModelScope.launch {
                musicRepository.saveLyricsOffset(trackId, 0L)
            }
        }
    }

    fun loadLyrics(track: Track) {
        activeTrackId = track.id
        viewModelScope.launch {
            _lyricsOffsetMs.value = musicRepository.getLyricsOffset(track.id)
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

    val queueDedupStatus = MutableStateFlow<String?>(null)

    fun deduplicateQueue() {
        viewModelScope.launch {
            val count = manageQueueUseCase.deduplicateQueue()
            queueDedupStatus.value = if (count > 0) "Removed $count duplicate tracks from queue" else "Queue has no duplicates"
        }
    }
}

