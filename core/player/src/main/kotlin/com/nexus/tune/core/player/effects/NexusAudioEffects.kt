package com.nexus.tune.core.player.effects

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.util.Log

class NexusAudioEffects {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = 0

    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == currentSessionId) return
        release()
        currentSessionId = sessionId
        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
                setStrength(500.toShort()) // 50% default boost
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "AudioEffect initialization not supported on session $sessionId: ${e.message}")
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to toggle equalizer: ${e.message}")
        }
    }

    fun setPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        try {
            val numPresets = eq.numberOfPresets
            if (presetIndex in 0 until numPresets) {
                eq.usePreset(presetIndex.toShort())
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set preset $presetIndex: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
        } catch (e: Exception) {
            // Ignore
        }
        equalizer = null
        bassBoost = null
        currentSessionId = 0
    }
}

