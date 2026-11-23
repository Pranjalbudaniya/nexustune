package com.nexus.tune.core.player

import com.nexus.tune.core.model.PlaybackStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val isEndOfTrack: Boolean = false
)

class SleepTimerManager(
    private val playerController: PlayerController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(minutes: Int) {
        cancelTimer()
        if (minutes <= 0) return

        val totalSeconds = minutes * 60L
        _timerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = totalSeconds,
            isEndOfTrack = false
        )

        timerJob = coroutineScope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _timerState.value = _timerState.value.copy(remainingSeconds = remaining)
            }
            if (isActive) {
                onTimerFinished()
            }
        }
    }

    fun startEndOfTrackTimer() {
        cancelTimer()
        _timerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = 0L,
            isEndOfTrack = true
        )
        timerJob = coroutineScope.launch {
            playerController.playbackState.first { it.status == PlaybackStatus.ENDED }
            onTimerFinished()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = SleepTimerState(isActive = false, remainingSeconds = 0L, isEndOfTrack = false)
    }

    private fun onTimerFinished() {
        playerController.togglePlayPause()
        _timerState.value = SleepTimerState(isActive = false, remainingSeconds = 0L, isEndOfTrack = false)
    }
}
