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
    val fadeVolumeMultiplier: Float = 1.0f,
    val shakeToExtendEnabled: Boolean = false,
    val extensionMinutes: Int = 10,
    val lastExtendedAtMs: Long = 0L,
    val bedtimeHandoffEnabled: Boolean = false,
    val handoffSoundType: com.nexus.tune.core.model.AmbientSoundType = com.nexus.tune.core.model.AmbientSoundType.BROWN_NOISE,
    val handoffVolume: Float = 0.3f,
    val handoffDurationMinutes: Int = 60
)

class SleepTimerManager(
    private val playerController: PlayerController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val sensorManager: android.hardware.SensorManager? = null
) {
    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var fadeSeconds: Int = 30
    private var shakeListenerRegistered = false

    private val accelerometerListener = object : android.hardware.SensorEventListener {
        private var lastShakeTimestamp = 0L
        override fun onSensorChanged(event: android.hardware.SensorEvent?) {
            if (event == null || !_timerState.value.isActive || !_timerState.value.shakeToExtendEnabled) return
            val x = event.values[0] / android.hardware.SensorManager.GRAVITY_EARTH
            val y = event.values[1] / android.hardware.SensorManager.GRAVITY_EARTH
            val z = event.values[2] / android.hardware.SensorManager.GRAVITY_EARTH
            val gForce = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            if (gForce > 2.2f) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTimestamp > 3000L) {
                    lastShakeTimestamp = now
                    extendTimer(_timerState.value.extensionMinutes)
                }
            }
        }
        override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
    }

    fun setFadeDuration(seconds: Int) {
        fadeSeconds = seconds.coerceAtLeast(0)
    }

    fun setShakeToExtend(enabled: Boolean, extensionMinutes: Int = 10) {
        _timerState.value = _timerState.value.copy(
            shakeToExtendEnabled = enabled,
            extensionMinutes = extensionMinutes
        )
        if (enabled && _timerState.value.isActive) {
            registerShakeListener()
        } else if (!enabled) {
            unregisterShakeListener()
        }
    }

    fun setBedtimeHandoff(
        enabled: Boolean,
        soundType: com.nexus.tune.core.model.AmbientSoundType = com.nexus.tune.core.model.AmbientSoundType.BROWN_NOISE,
        volume: Float = 0.3f,
        durationMinutes: Int = 60
    ) {
        _timerState.value = _timerState.value.copy(
            bedtimeHandoffEnabled = enabled,
            handoffSoundType = soundType,
            handoffVolume = volume,
            handoffDurationMinutes = durationMinutes
        )
    }

    fun extendTimer(extraMinutes: Int) {
        if (!_timerState.value.isActive || extraMinutes <= 0) return
        val currentRemaining = _timerState.value.remainingSeconds
        val newRemaining = currentRemaining + (extraMinutes * 60L)
        playerController.setVolume(1.0f)
        _timerState.value = _timerState.value.copy(
            remainingSeconds = newRemaining,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f,
            lastExtendedAtMs = System.currentTimeMillis()
        )
    }

    fun startTimer(minutes: Int, fadeDurationSec: Int? = null) {
        cancelTimer()
        if (minutes <= 0) return

        if (fadeDurationSec != null) {
            fadeSeconds = fadeDurationSec.coerceAtLeast(0)
        }

        val totalSeconds = minutes * 60L
        _timerState.value = _timerState.value.copy(
            isActive = true,
            remainingSeconds = totalSeconds,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )

        if (_timerState.value.shakeToExtendEnabled) {
            registerShakeListener()
        }

        timerJob = coroutineScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _timerState.value.remainingSeconds
                if (current <= 0L) break
                val remaining = current - 1L

                val isFading = fadeSeconds > 0 && remaining <= fadeSeconds
                val multiplier = if (isFading && fadeSeconds > 0) {
                    val frac = (remaining.toFloat() / fadeSeconds.toFloat()).coerceIn(0.0f, 1.0f)
                    frac * frac
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
        _timerState.value = _timerState.value.copy(
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
        unregisterShakeListener()
        timerJob?.cancel()
        timerJob = null
        playerController.setVolume(1.0f)
        _timerState.value = _timerState.value.copy(
            isActive = false,
            remainingSeconds = 0L,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )
    }

    private fun onTimerFinished() {
        unregisterShakeListener()
        playerController.togglePlayPause()
        playerController.setVolume(1.0f)
        val state = _timerState.value
        if (state.bedtimeHandoffEnabled && state.handoffSoundType != com.nexus.tune.core.model.AmbientSoundType.OFF) {
            runCatching {
                com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer.instance.start(
                    state.handoffSoundType,
                    state.handoffVolume
                )
                if (state.handoffDurationMinutes > 0) {
                    coroutineScope.launch {
                        delay(state.handoffDurationMinutes * 60L * 1000L)
                        com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer.instance.stop()
                    }
                }
            }
        } else {
            runCatching {
                com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer.instance.stop()
            }
        }
        _timerState.value = _timerState.value.copy(
            isActive = false,
            remainingSeconds = 0L,
            isEndOfTrack = false,
            isFadingOut = false,
            fadeVolumeMultiplier = 1.0f
        )
    }

    private fun registerShakeListener() {
        if (shakeListenerRegistered || sensorManager == null) return
        val accel = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(accelerometerListener, accel, android.hardware.SensorManager.SENSOR_DELAY_UI)
        shakeListenerRegistered = true
    }

    private fun unregisterShakeListener() {
        if (!shakeListenerRegistered || sensorManager == null) return
        sensorManager.unregisterListener(accelerometerListener)
        shakeListenerRegistered = false
    }
}
