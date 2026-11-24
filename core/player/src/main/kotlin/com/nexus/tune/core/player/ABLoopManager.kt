package com.nexus.tune.core.player

import com.nexus.tune.core.model.ABLoopState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages A-B repeat looping of arbitrary track segments for music practice, dancing, and transcription.
 */
class ABLoopManager(
    private val playerController: PlayerController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _loopState = MutableStateFlow(ABLoopState())
    val loopState: StateFlow<ABLoopState> = _loopState.asStateFlow()

    private var monitorJob: Job? = null

    init {
        startMonitoring()
    }

    fun setPointA(posMs: Long? = null) {
        val position = posMs ?: playerController.playbackState.value.positionMs
        val current = _loopState.value
        val end = current.endMs
        val validEnd = if (end != null && position >= end) null else end
        _loopState.value = current.copy(
            startMs = position,
            endMs = validEnd,
            isEnabled = validEnd != null,
            loopCount = 0
        )
    }

    fun setPointB(posMs: Long? = null) {
        val position = posMs ?: playerController.playbackState.value.positionMs
        val current = _loopState.value
        val start = current.startMs ?: 0L
        if (position > start) {
            _loopState.value = current.copy(
                startMs = start,
                endMs = position,
                isEnabled = true,
                loopCount = 0
            )
        }
    }

    fun toggleLoop(enabled: Boolean? = null) {
        val current = _loopState.value
        if (current.startMs == null || current.endMs == null) return
        val target = enabled ?: !current.isEnabled
        _loopState.value = current.copy(isEnabled = target)
    }

    fun clearLoop() {
        _loopState.value = ABLoopState()
    }

    fun release() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = coroutineScope.launch {
            playerController.playbackState.collect { state ->
                val loop = _loopState.value
                val start = loop.startMs
                val end = loop.endMs
                if (!loop.isEnabled || start == null || end == null) return@collect
                val pos = state.positionMs
                if (pos >= end) {
                    playerController.seekTo(start)
                    _loopState.value = loop.copy(loopCount = loop.loopCount + 1)
                }
            }
        }
    }
}
