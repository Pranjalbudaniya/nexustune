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
    val isEndOfTrack: Boolean = false,
    val isFadingOut: Boolean = false,
    val fadeVolumeMultiplier: Float = 1.0f
)

class SleepTimerManager(
    private val playerController: PlayerController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var fadeSeconds: Int = 30

    fun setFadeDuration(seconds: Int) {
        fadeSeconds = seconds.coerceAtLeast(0)
    }

    fun startTimer(minutes: Int, fadeDurationSec: Int? = null) {
        cancelTimer()
        if (minutes <= 0) return

        if (fadeDurationSec != null) {
            fadeSeconds = fadeDurationSec.coerceAtLeast(0)
        }

        val totalSeconds = minutes * 60L
        _timerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = totalSeconds,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )

        timerJob = coroutineScope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--

                val isFading = fadeSeconds > 0 && remaining <= fadeSeconds
                val multiplier = if (isFading && fadeSeconds > 0) {
                    val frac = (remaining.toFloat() / fadeSeconds.toFloat()).coerceIn(0.0f, 1.0f)
                    frac * frac // perceptual quadratic decay
                } else {
                    1.0f
                }

                if (isFading) {
                    playerController.setVolume(multiplier)
                }

                _timerState.value = _timerState.value.copy(
                    remainingSeconds = remaining,
                    isFadingOut = isFading,
                    fadeVolumeMultiplier = multiplier
                )
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
            isEndOfTrack = true,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )
        timerJob = coroutineScope.launch {
            playerController.playbackState.first { it.status == PlaybackStatus.ENDED }
            onTimerFinished()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        playerController.setVolume(1.0f)
        _timerState.value = SleepTimerState(
            isActive = false,
            remainingSeconds = 0L,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )
    }

    private fun onTimerFinished() {
        playerController.togglePlayPause()
        playerController.setVolume(1.0f)
        _timerState.value = SleepTimerState(
            isActive = false,
            remainingSeconds = 0L,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )
    }
}
