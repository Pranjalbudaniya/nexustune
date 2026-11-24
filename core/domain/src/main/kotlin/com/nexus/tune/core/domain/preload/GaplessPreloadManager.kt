package com.nexus.tune.core.domain.preload

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class GaplessPreloadManager(
    private val audioPreloader: AudioPreloader,
    private val queueRepository: QueueRepository,
    private val resolveStreamUseCase: ResolveStreamUseCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _preloadedTrackId = MutableStateFlow<String?>(null)
    val preloadedTrackId: StateFlow<String?> = _preloadedTrackId.asStateFlow()

    private var isPreloading = false

    fun checkAndPreload(
        currentTrack: Track?,
        positionMs: Long,
        durationMs: Long,
        enabled: Boolean,
        thresholdSeconds: Int
    ) {
        if (!enabled || currentTrack == null || durationMs <= 0 || positionMs <= 0) return

        val remainingMs = durationMs - positionMs
        val thresholdMs = thresholdSeconds * 1000L

        if (remainingMs in 1..thresholdMs) {
            if (_preloadedTrackId.value != null || isPreloading) return
            isPreloading = true

            coroutineScope.launch {
                try {
                    val queue = queueRepository.getQueue().first()
                    val currentIndex = queueRepository.getQueueIndex().first()
                    val nextIndex = currentIndex + 1
                    if (nextIndex < queue.size) {
                        val nextTrack = queue[nextIndex]
                        val streamUrl = when (val res = resolveStreamUseCase(nextTrack.id)) {
                            is MusicResult.Success -> res.data.url
                            is MusicResult.Failure -> nextTrack.streamUrl
                        }
                        if (streamUrl != null) {
                            audioPreloader.preloadTrack(nextTrack, streamUrl)
                            _preloadedTrackId.value = nextTrack.id
                        }
                    }
                } catch (e: Exception) {
                    // Resilient fallback
                } finally {
                    isPreloading = false
                }
            }
        } else if (remainingMs > thresholdMs) {
            if (_preloadedTrackId.value != null && positionMs < (durationMs / 2)) {
                _preloadedTrackId.value = null
            }
        }
    }

    fun onTrackChanged(newTrackId: String?) {
        if (_preloadedTrackId.value == newTrackId) {
            _preloadedTrackId.value = null
        }
    }

    fun clear() {
        _preloadedTrackId.value = null
        audioPreloader.clearPreload()
    }
}

