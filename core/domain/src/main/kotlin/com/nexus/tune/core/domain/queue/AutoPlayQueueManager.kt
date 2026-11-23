package com.nexus.tune.core.domain.queue

import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.GetAutoPlayRecommendationsUseCase
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoPlayQueueManager(
    private val getAutoPlayRecommendationsUseCase: GetAutoPlayRecommendationsUseCase,
    private val queueRepository: QueueRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private var isEnabled: Boolean = true
    private var isFetching: Boolean = false

    private val _isReplenishing = MutableStateFlow(false)
    val isReplenishing: StateFlow<Boolean> = _isReplenishing.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun isAutoPlayEnabled(): Boolean = isEnabled

    fun checkAndReplenish(
        currentTrack: Track?,
        queue: List<Track>,
        currentIndex: Int
    ) {
        if (!isEnabled || isFetching || currentTrack == null) return
        val remainingTracks = queue.size - (currentIndex + 1)
        if (remainingTracks <= 2) {
            replenishQueue(currentTrack, queue)
        }
    }

    private fun replenishQueue(currentTrack: Track, queue: List<Track>) {
        isFetching = true
        _isReplenishing.value = true
        coroutineScope.launch {
            try {
                val recommendations = getAutoPlayRecommendationsUseCase(currentTrack, queue)
                if (recommendations.isNotEmpty()) {
                    queueRepository.addTracksNext(recommendations.take(5))
                }
            } catch (e: Exception) {
                // Ignore failure gracefully
            } finally {
                isFetching = false
                _isReplenishing.value = false
            }
        }
    }
}

