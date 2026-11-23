package com.nexus.tune.core.player

import com.nexus.tune.core.model.SkipSegment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SkipSegmentManager(
    private val playerController: PlayerController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _currentSegments = MutableStateFlow<List<SkipSegment>>(emptyList())
    val currentSegments: StateFlow<List<SkipSegment>> = _currentSegments.asStateFlow()

    private var monitorJob: Job? = null

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
    }

    fun setSegments(segments: List<SkipSegment>) {
        _currentSegments.value = segments
        startMonitoring()
    }

    fun clearSegments() {
        _currentSegments.value = emptyList()
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = coroutineScope.launch {
            playerController.playbackState.collect { state ->
                if (!_enabled.value || _currentSegments.value.isEmpty()) return@collect
                val pos = state.positionMs
                val matching = _currentSegments.value.firstOrNull { pos in it.startMs until it.endMs }
                if (matching != null) {
                    playerController.seekTo(matching.endMs)
                }
            }
        }
    }
}

