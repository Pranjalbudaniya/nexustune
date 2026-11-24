package com.nexus.tune.core.player.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nexus.tune.core.model.WakeAlarmConfig
import com.nexus.tune.core.player.PlayerController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Manages morning wake-up alarms with gentle audio volume fade-in over 30s to 10 minutes.
 */
class WakeAlarmManager(
    private val context: Context,
    private val playerController: PlayerController,
    private val ambientSynthesizer: com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    companion object {
        const val ACTION_WAKE_ALARM = "com.nexus.tune.ACTION_WAKE_ALARM"
        const val WAKE_ALARM_REQUEST_CODE = 4040
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive.asStateFlow()

    private val _currentFadedVolume = MutableStateFlow(0.0f)
    val currentFadedVolume: StateFlow<Float> = _currentFadedVolume.asStateFlow()

    private var fadeJob: Job? = null

    /**
     * Calculates epoch milliseconds for the next occurrence of [hour]:[minute].
     * If the time today has already passed, it schedules for tomorrow.
     */
    fun calculateNextTriggerMillis(hour: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    fun scheduleAlarm(config: WakeAlarmConfig) {
        if (!config.isEnabled) {
            cancelAlarm()
            return
        }
        val triggerAtMillis = calculateNextTriggerMillis(config.hour, config.minute)
        val intent = Intent(context, WakeAlarmReceiver::class.java).apply {
            action = ACTION_WAKE_ALARM
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, WAKE_ALARM_REQUEST_CODE, intent, flags)

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    fun cancelAlarm() {
        val intent = Intent(context, WakeAlarmReceiver::class.java).apply {
            action = ACTION_WAKE_ALARM
        }
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, WAKE_ALARM_REQUEST_CODE, intent, flags)
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        stopAlarm()
    }

    fun triggerWakeAlarm(config: WakeAlarmConfig) {
        _isAlarmActive.value = true
        fadeJob?.cancel()

        playerController.setVolume(0.0f)
        if (config.source == com.nexus.tune.core.model.WakeAlarmSource.AMBIENT_SOUND && ambientSynthesizer != null) {
            ambientSynthesizer.start(com.nexus.tune.core.model.AmbientSoundType.BINAURAL_ALPHA)
            ambientSynthesizer.setVolume(0.0f)
        } else {
            val state = playerController.playbackState.value
            if (state.status != com.nexus.tune.core.model.PlaybackStatus.PLAYING) {
                playerController.togglePlayPause()
            }
        }

        startFadeIn(config.fadeInSeconds, config.targetVolume)
    }

    fun startFadeIn(durationSeconds: Int, targetVolume: Float) {
        fadeJob?.cancel()
        fadeJob = coroutineScope.launch {
            val totalSteps = (durationSeconds * 2).coerceAtLeast(1)
            val stepDelayMs = 500L
            val stepIncrement = targetVolume / totalSteps

            var currentVol = 0.0f
            for (step in 1..totalSteps) {
                delay(stepDelayMs)
                currentVol = (currentVol + stepIncrement).coerceAtMost(targetVolume)
                _currentFadedVolume.value = currentVol
                playerController.setVolume(currentVol)
                ambientSynthesizer?.setVolume(currentVol)
            }
            _currentFadedVolume.value = targetVolume
            playerController.setVolume(targetVolume)
            ambientSynthesizer?.setVolume(targetVolume)
        }
    }

    fun stopAlarm() {
        _isAlarmActive.value = false
        fadeJob?.cancel()
        fadeJob = null
        playerController.setVolume(1.0f)
        ambientSynthesizer?.setVolume(0.5f)
    }

    fun snooze(minutes: Int = 10) {
        stopAlarm()
        val triggerAtMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, WakeAlarmReceiver::class.java).apply {
            action = ACTION_WAKE_ALARM
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, WAKE_ALARM_REQUEST_CODE, intent, flags)
        alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
