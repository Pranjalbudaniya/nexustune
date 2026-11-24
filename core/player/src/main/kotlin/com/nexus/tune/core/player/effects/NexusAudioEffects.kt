package com.nexus.tune.core.player.effects

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import com.nexus.tune.core.model.EqualizerPreset

class NexusAudioEffects {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
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
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = false
                setTargetGain(0)
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

    fun setLoudnessEnhancerEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to toggle loudness enhancer: ${e.message}")
        }
    }

    fun setTargetGain(gainmB: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainmB)
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set target gain: ${e.message}")
        }
    }

    fun applyTrackLoudnessGain(gainDb: Float, baseTargetGainmB: Int = 0) {
        val additionalGainmB = (gainDb * 100).toInt()
        val combinedGainmB = (baseTargetGainmB + additionalGainmB).coerceIn(0, 1000)
        setTargetGain(combinedGainmB)
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

    fun applyPreset(preset: EqualizerPreset) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            when (preset) {
                EqualizerPreset.FLAT -> {
                    for (i in 0 until numBands) eq.setBandLevel(i.toShort(), 0)
                    bassBoost?.setStrength(0.toShort())
                }
                EqualizerPreset.BASS_BOOST -> {
                    if (numBands >= 2) {
                        eq.setBandLevel(0.toShort(), 600.toShort())
                        eq.setBandLevel(1.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(800.toShort())
                }
                EqualizerPreset.TREBLE_BOOST -> {
                    if (numBands >= 5) {
                        eq.setBandLevel((numBands - 1).toShort(), 600.toShort())
                        eq.setBandLevel((numBands - 2).toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(200.toShort())
                }
                EqualizerPreset.VOCAL -> {
                    if (numBands >= 3) {
                        eq.setBandLevel(1.toShort(), 500.toShort())
                        eq.setBandLevel(2.toShort(), 500.toShort())
                    }
                    bassBoost?.setStrength(200.toShort())
                }
                EqualizerPreset.ELECTRONIC -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 500.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(700.toShort())
                }
                EqualizerPreset.ROCK -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 400.toShort())
                        eq.setBandLevel(1.toShort(), 200.toShort())
                        eq.setBandLevel(2.toShort(), (-100).toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(500.toShort())
                }
                EqualizerPreset.CLASSICAL -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 300.toShort())
                        eq.setBandLevel(1.toShort(), 200.toShort())
                        eq.setBandLevel(2.toShort(), (-200).toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(100.toShort())
                }
                EqualizerPreset.JAZZ -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 200.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(2.toShort(), 300.toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 300.toShort())
                    }
                    bassBoost?.setStrength(300.toShort())
                }
                EqualizerPreset.HIP_HOP -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 700.toShort())
                        eq.setBandLevel(1.toShort(), 500.toShort())
                        eq.setBandLevel(2.toShort(), 0.toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(800.toShort())
                }
                EqualizerPreset.ACOUSTIC -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 200.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(2.toShort(), 400.toShort())
                        eq.setBandLevel(3.toShort(), 400.toShort())
                        eq.setBandLevel(4.toShort(), 300.toShort())
                    }
                    bassBoost?.setStrength(150.toShort())
                }
                EqualizerPreset.CUSTOM -> {
                    // Custom band gains preserved
                }
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to apply preset $preset: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        equalizer = null
        bassBoost = null
        loudnessEnhancer = null
        currentSessionId = 0
    }

    companion object {
        val instance: NexusAudioEffects by lazy { NexusAudioEffects() }
    }
}
