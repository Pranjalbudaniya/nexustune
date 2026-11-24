package com.nexus.tune.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase,
    private val abLoopManager: com.nexus.tune.core.player.ABLoopManager? = null,
    private val playerController: com.nexus.tune.core.player.PlayerController? = null
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

    private val _bookmarks = MutableStateFlow<List<TrackBookmark>>(emptyList())
    val bookmarks: StateFlow<List<TrackBookmark>> = _bookmarks.asStateFlow()

    private var bookmarkCollectionJob: Job? = null

    val activeQueue: StateFlow<List<Track>> = manageQueueUseCase.getQueue()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeQueueIndex: StateFlow<Int> = manageQueueUseCase.getQueueIndex()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun jumpToQueueIndex(index: Int) {
        val q = activeQueue.value
        if (index in 0 until q.size) {
            viewModelScope.launch {
                manageQueueUseCase.playPlaylist(q, index)
            }
        }
    }

    fun removeFromQueue(index: Int) {
        viewModelScope.launch {
            manageQueueUseCase.remove(index)
        }
    }

    fun clearUpcoming() {
        viewModelScope.launch {
            manageQueueUseCase.clearUpcoming()
        }
    }

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
        playerController?.setPlaybackSpeed(speed)
    }

    private val _playbackPitchSemitones = MutableStateFlow(0)
    val playbackPitchSemitones: StateFlow<Int> = _playbackPitchSemitones.asStateFlow()

    private val _playbackPitchRatio = MutableStateFlow(1.0f)
    val playbackPitchRatio: StateFlow<Float> = _playbackPitchRatio.asStateFlow()

    fun setPlaybackSemitones(semitones: Int) {
        val clamped = semitones.coerceIn(
            com.nexus.tune.core.player.PitchTransposer.MIN_SEMITONES,
            com.nexus.tune.core.player.PitchTransposer.MAX_SEMITONES
        )
        val ratio = com.nexus.tune.core.player.PitchTransposer.semitonesToPitchRatio(clamped)
        _playbackPitchSemitones.value = clamped
        _playbackPitchRatio.value = ratio
        playerController?.setPlaybackSemitones(clamped)
    }

    fun setPlaybackPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        val semitones = com.nexus.tune.core.player.PitchTransposer.pitchRatioToSemitones(clamped)
        _playbackPitchSemitones.value = semitones
        _playbackPitchRatio.value = clamped
        playerController?.setPlaybackPitch(clamped)
    }

    fun transposeUp() {
        setPlaybackSemitones(_playbackPitchSemitones.value + 1)
    }

    fun transposeDown() {
        setPlaybackSemitones(_playbackPitchSemitones.value - 1)
    }

    fun resetPitch() {
        setPlaybackSemitones(0)
    }

    private val _djFxConfig = MutableStateFlow(com.nexus.tune.core.model.DjFxConfig())
    val djFxConfig: StateFlow<com.nexus.tune.core.model.DjFxConfig> = _djFxConfig.asStateFlow()

    fun setDjPreset(preset: com.nexus.tune.core.model.DjPreset) {
        val config = com.nexus.tune.core.domain.audio.DjTransformEngine.getPresetConfig(preset)
        _djFxConfig.value = config
        playerController?.setPlaybackSpeed(config.speed)
        playerController?.setPlaybackSemitones(config.semitones)
        _playbackSpeed.value = config.speed
        _playbackPitchSemitones.value = config.semitones
    }

    fun setDjSpeed(speed: Float) {
        val clamped = com.nexus.tune.core.domain.audio.DjTransformEngine.clampSpeed(speed)
        val current = _djFxConfig.value
        val newConfig = current.copy(
            speed = clamped,
            preset = com.nexus.tune.core.domain.audio.DjTransformEngine.matchClosestPreset(clamped, current.semitones)
        )
        _djFxConfig.value = newConfig
        playerController?.setPlaybackSpeed(clamped)
        _playbackSpeed.value = clamped
    }

    fun setDjSemitones(semitones: Int) {
        val clamped = com.nexus.tune.core.domain.audio.DjTransformEngine.clampSemitones(semitones)
        val current = _djFxConfig.value
        val newConfig = current.copy(
            semitones = clamped,
            preset = com.nexus.tune.core.domain.audio.DjTransformEngine.matchClosestPreset(current.speed, clamped)
        )
        _djFxConfig.value = newConfig
        playerController?.setPlaybackSemitones(clamped)
        _playbackPitchSemitones.value = clamped
    }

    fun setDjSoundstageWidth(width: Float) {
        val clamped = com.nexus.tune.core.domain.audio.DjTransformEngine.clampSoundstageWidth(width)
        _djFxConfig.value = _djFxConfig.value.copy(soundstageWidth = clamped)
    }

    fun toggleDjLoFiFilter() {
        val current = _djFxConfig.value
        _djFxConfig.value = current.copy(loFiFilterEnabled = !current.loFiFilterEnabled)
    }

    fun resetDjFx() {
        setDjPreset(com.nexus.tune.core.model.DjPreset.NORMAL)
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
        loadBookmarks(track.id)
        viewModelScope.launch {
            _lyricsOffsetMs.value = musicRepository.getLyricsOffset(track.id)
            when (val result = musicRepository.getLyrics(track.id, track.title, track.artist, track.durationSeconds)) {
                is MusicResult.Success -> _lyrics.value = result.data
                is MusicResult.Failure -> _lyrics.value = null
            }
        }
    }

    fun loadBookmarks(trackId: String) {
        bookmarkCollectionJob?.cancel()
        bookmarkCollectionJob = viewModelScope.launch {
            musicRepository.getBookmarksForTrack(trackId).collect { list ->
                _bookmarks.value = list
            }
        }
    }

    fun addBookmark(title: String = "", positionMs: Long? = null) {
        val trackId = activeTrackId ?: return
        val pos = positionMs ?: playerController?.playbackState?.value?.positionMs ?: 0L
        val cleanTitle = if (title.isBlank()) {
            val totalSecs = (pos / 1000).coerceAtLeast(0)
            val mins = totalSecs / 60
            val secs = totalSecs % 60
            String.format("Bookmark at %02d:%02d", mins, secs)
        } else {
            title.trim()
        }
        viewModelScope.launch {
            musicRepository.addBookmark(trackId, cleanTitle, pos)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            musicRepository.deleteBookmark(bookmarkId)
        }
    }

    fun seekToBookmark(bookmark: TrackBookmark) {
        playerController?.seekTo(bookmark.positionMs)
    }

    fun toggleLike(track: Track) {
        viewModelScope.launch {
            musicRepository.toggleLike(track)
        }
    }

    val queueDedupStatus = MutableStateFlow<String?>(null)

    fun shuffleQueue(smartDiversity: Boolean = true) {
        viewModelScope.launch {
            manageQueueUseCase.shuffleQueue(smartDiversity)
        }
    }

    fun deduplicateQueue() {
        viewModelScope.launch {
            val count = manageQueueUseCase.deduplicateQueue()
            queueDedupStatus.value = if (count > 0) "Removed $count duplicate tracks from queue" else "Queue has no duplicates"
        }
    }

    fun importCustomLrc(track: Track, lrcContent: String) {
        viewModelScope.launch {
            when (val result = musicRepository.saveCustomLyrics(track.id, track.title, track.artist, lrcContent)) {
                is MusicResult.Success -> _lyrics.value = result.data
                is MusicResult.Failure -> { /* no-op */ }
            }
        }
    }

    suspend fun exportCurrentLyrics(track: Track): String? {
        return musicRepository.exportLyricsToLrc(track.id)
    }

    val currentHarmonicProfile: StateFlow<TrackHarmonicProfile?> = combine(
        activeQueue,
        activeQueueIndex
    ) { queue, index ->
        if (queue.isNotEmpty() && index in 0 until queue.size) {
            com.nexus.tune.core.domain.audio.HarmonicMixingEngine.deriveHarmonicProfile(queue[index])
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val nextTrackHarmonicTransition: StateFlow<HarmonicTransition?> = combine(
        activeQueue,
        activeQueueIndex
    ) { queue, index ->
        if (queue.size > index + 1 && index >= 0) {
            val current = com.nexus.tune.core.domain.audio.HarmonicMixingEngine.deriveHarmonicProfile(queue[index])
            val next = com.nexus.tune.core.domain.audio.HarmonicMixingEngine.deriveHarmonicProfile(queue[index + 1])
            com.nexus.tune.core.domain.audio.HarmonicMixingEngine.evaluateTransition(current, next)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun harmonizeUpcomingQueue() {
        val q = activeQueue.value
        val idx = activeQueueIndex.value
        if (q.size <= idx + 2) return // Need at least 2 upcoming tracks to reorder
        val currentTrack = q[idx]
        val pastTracks = q.take(idx + 1)
        val upcomingTracks = q.drop(idx + 1)
        val harmonizedUpcoming = com.nexus.tune.core.domain.audio.HarmonicMixingEngine.harmonizeQueue(currentTrack, upcomingTracks)
        val reorderedFullQueue = pastTracks + harmonizedUpcoming
        viewModelScope.launch {
            manageQueueUseCase.playPlaylist(reorderedFullQueue, idx)
        }
    }
}

