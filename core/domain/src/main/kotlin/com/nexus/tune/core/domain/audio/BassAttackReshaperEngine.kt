package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.BassAttackPreset
import com.nexus.tune.core.model.BassAttackReshaperConfig
import com.nexus.tune.core.model.BassAttackReshaperMetrics
import com.nexus.tune.core.model.BassAttackTarget
import com.nexus.tune.core.model.BassReshaperMode
import kotlin.math.*

/**
 * Audiophile Dynamic Multiband Sub-Bass Transient Punch & Attack Reshaper Studio.
 *
 * Implements sub-bass band-split transient attack velocity amplification, sustain
 * resonance tail clamping, and acoustic kick/bass envelope re-contouring.
 */
class BassAttackReshaperEngine {

    /**
     * Process audio buffer with sub-bass transient punch reshaping.
     */
    fun process(
        samples: FloatArray,
        config: BassAttackReshaperConfig,
        sampleRate: Int = 44100
    ): FloatArray {
        if (!config.isEnabled || config.preset == BassAttackPreset.OFF || samples.isEmpty()) {
            return samples.copyOf()
        }

        val output = FloatArray(samples.size)

        // 1. Calculate 2nd-order Linkwitz-Riley low-pass filter coefficients for sub-bass band
        val cutoff = config.crossoverFrequencyHz.coerceIn(30.0f, 250.0f)
        val omega = 2.0 * PI * cutoff / sampleRate
        val sinOmega = sin(omega)
        val cosOmega = cos(omega)
        val q = 0.70710678 // Butterworth Q
        val alpha = sinOmega / (2.0 * q)

        val b0 = (1.0 - cosOmega) / 2.0
        val b1 = 1.0 - cosOmega
        val b2 = (1.0 - cosOmega) / 2.0
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosOmega
        val a2 = 1.0 - alpha

        val normB0 = (b0 / a0).toFloat()
        val normB1 = (b1 / a0).toFloat()
        val normB2 = (b2 / a0).toFloat()
        val normA1 = (a1 / a0).toFloat()
        val normA2 = (a2 / a0).toFloat()

        // Time constants for envelope tracking
        // Fast attack (~6ms) and slow sustain decay (~180ms)
        val attackCoeff = exp(-1.0 / (sampleRate * 0.006f)).toFloat()
        val sustainCoeff = exp(-1.0 / (sampleRate * 0.180f)).toFloat()

        var x1 = 0.0f
        var x2 = 0.0f
        var y1 = 0.0f
        var y2 = 0.0f

        var envAttack = 0.0f
        var envSustain = 0.0f

        val attackMultiplier = config.attackPunchMultiplier.coerceIn(1.0f, 2.5f)
        val sustainDamp = (1.0f - (config.sustainDampingPercent.coerceIn(0, 100) / 100.0f * 0.75f))

        for (i in samples.indices) {
            val inputSample = samples[i]

            // Linkwitz-Riley lowpass for sub-bass component
            val subSample = normB0 * inputSample + normB1 * x1 + normB2 * x2 - normA1 * y1 - normA2 * y2
            x2 = x1
            x1 = inputSample
            y2 = y1
            y1 = subSample

            // Mid/High remainder component
            val midHighSample = inputSample - subSample

            // Dual envelope tracking
            val absSub = abs(subSample)
            envAttack = if (absSub > envAttack) absSub else envAttack * attackCoeff + absSub * (1.0f - attackCoeff)
            envSustain = if (absSub > envSustain) absSub else envSustain * sustainCoeff + absSub * (1.0f - sustainCoeff)

            // Transient detection ratio
            val transientDelta = max(0.0f, envAttack - envSustain)
            val isTransientPeak = transientDelta > 0.015f

            val reshapedSub: Float = when (config.mode) {
                BassReshaperMode.DYNAMIC_ATTACK_BOOST -> {
                    if (isTransientPeak) {
                        subSample * (1.0f + (attackMultiplier - 1.0f) * (transientDelta / (envAttack + 0.001f)).coerceIn(0.0f, 1.0f))
                    } else {
                        subSample
                    }
                }
                BassReshaperMode.SUSTAIN_TAIL_DAMP -> {
                    if (!isTransientPeak) {
                        subSample * sustainDamp
                    } else {
                        subSample
                    }
                }
                BassReshaperMode.BALANCED_TRANSIENT_PUNCH -> {
                    if (isTransientPeak) {
                        val boost = 1.0f + (attackMultiplier - 1.0f) * (transientDelta / (envAttack + 0.001f)).coerceIn(0.0f, 1.0f)
                        subSample * boost
                    } else {
                        subSample * sustainDamp
                    }
                }
                BassReshaperMode.SOLO_ATTACK_TRANSIENT -> {
                    if (isTransientPeak) {
                        subSample * attackMultiplier
                    } else {
                        0.0f
                    }
                }
            }

            // Combine reshaped sub with mid/high (or solo transient if audition mode)
            val finalSample = if (config.soloAttackTransient || config.mode == BassReshaperMode.SOLO_ATTACK_TRANSIENT) {
                reshapedSub
            } else {
                midHighSample + reshapedSub
            }

            output[i] = finalSample.coerceIn(-1.0f, 1.0f)
        }

        return output
    }

    /**
     * Real-time metrics estimation for UI visualization.
     */
    fun estimateMetrics(
        samples: FloatArray,
        config: BassAttackReshaperConfig,
        sampleRate: Int = 44100
    ): BassAttackReshaperMetrics {
        if (!config.isEnabled || config.preset == BassAttackPreset.OFF || samples.isEmpty()) {
            return BassAttackReshaperMetrics(
                attackVelocityRatio = 1.0f,
                sustainDampingDb = 0.0f,
                transientPunchRating = "BYPASS",
                isTransientActive = false
            )
        }

        var peakAttack = 0.0f
        var avgSustain = 0.0f
        var transientCount = 0

        val step = max(1, samples.size / 256)
        var i = 0
        while (i < samples.size) {
            val magnitude = abs(samples[i])
            if (magnitude > peakAttack) {
                peakAttack = magnitude
            }
            avgSustain += magnitude
            if (magnitude > 0.25f) {
                transientCount++
            }
            i += step
        }
        avgSustain /= (samples.size / step).coerceAtLeast(1)

        val velocityRatio = if (avgSustain > 0.001f) {
            (peakAttack / avgSustain * (config.attackPunchMultiplier / 1.35f)).coerceIn(1.0f, 4.0f)
        } else {
            config.attackPunchMultiplier
        }

        val dampingDb = -20.0f * log10((1.0f - (config.sustainDampingPercent / 100.0f * 0.75f)).coerceAtLeast(0.05f))

        val rating = when {
            velocityRatio >= 2.5f -> "SURGICAL SNAP"
            velocityRatio >= 1.8f -> "PUNCHY IMPACT"
            velocityRatio >= 1.3f -> "TIGHT THUMP"
            else -> "NATURAL BASS"
        }

        val isTransientActive = transientCount > 2

        return BassAttackReshaperMetrics(
            attackVelocityRatio = (velocityRatio * 100).roundToInt() / 100.0f,
            sustainDampingDb = (dampingDb * 10).roundToInt() / 10.0f,
            transientPunchRating = rating,
            isTransientActive = isTransientActive
        )
    }

    /**
     * Map presets to full studio configuration.
     */
    fun presetToConfig(preset: BassAttackPreset): BassAttackReshaperConfig {
        return when (preset) {
            BassAttackPreset.OFF -> BassAttackReshaperConfig(
                isEnabled = false,
                preset = BassAttackPreset.OFF
            )
            BassAttackPreset.TIGHT_KICK_IMPACT -> BassAttackReshaperConfig(
                isEnabled = true,
                preset = BassAttackPreset.TIGHT_KICK_IMPACT,
                target = BassAttackTarget.SUB_KICK_60HZ,
                mode = BassReshaperMode.BALANCED_TRANSIENT_PUNCH,
                attackPunchMultiplier = 1.55f,
                sustainDampingPercent = 55,
                crossoverFrequencyHz = 75.0f
            )
            BassAttackPreset.DE_BOOM_SUSTAIN -> BassAttackReshaperConfig(
                isEnabled = true,
                preset = BassAttackPreset.DE_BOOM_SUSTAIN,
                target = BassAttackTarget.ACOUSTIC_UPRIGHT_100HZ,
                mode = BassReshaperMode.SUSTAIN_TAIL_DAMP,
                attackPunchMultiplier = 1.15f,
                sustainDampingPercent = 70,
                crossoverFrequencyHz = 110.0f
            )
            BassAttackPreset.ACOUSTIC_JAZZ_BASS -> BassAttackReshaperConfig(
                isEnabled = true,
                preset = BassAttackPreset.ACOUSTIC_JAZZ_BASS,
                target = BassAttackTarget.ACOUSTIC_UPRIGHT_100HZ,
                mode = BassReshaperMode.DYNAMIC_ATTACK_BOOST,
                attackPunchMultiplier = 1.30f,
                sustainDampingPercent = 35,
                crossoverFrequencyHz = 100.0f
            )
            BassAttackPreset.HARDSTYLE_SUB_SLAP -> BassAttackReshaperConfig(
                isEnabled = true,
                preset = BassAttackPreset.HARDSTYLE_SUB_SLAP,
                target = BassAttackTarget.PUNCHY_BASS_80HZ,
                mode = BassReshaperMode.DYNAMIC_ATTACK_BOOST,
                attackPunchMultiplier = 1.85f,
                sustainDampingPercent = 50,
                crossoverFrequencyHz = 90.0f
            )
        }
    }
}

