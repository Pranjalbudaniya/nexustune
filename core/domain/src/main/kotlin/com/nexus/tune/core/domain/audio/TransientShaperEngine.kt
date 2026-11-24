package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object TransientShaperEngine {

    class EnvelopeFollowerState(sampleRate: Int = 48000) {
        private val alphaFastAttack: Float = exp(-1.0f / (0.002f * sampleRate))
        private val alphaFastRelease: Float = exp(-1.0f / (0.015f * sampleRate))
        private val alphaSlowAttack: Float = exp(-1.0f / (0.035f * sampleRate))
        private val alphaSlowRelease: Float = exp(-1.0f / (0.120f * sampleRate))

        var envFast: Float = 0.0f
        var envSlow: Float = 0.0f

        // Biquad state for band focus filtering
        var x1: Float = 0.0f
        var x2: Float = 0.0f
        var y1: Float = 0.0f
        var y2: Float = 0.0f

        fun updateEnvelopes(absSample: Float): Float {
            envFast = if (absSample > envFast) {
                alphaFastAttack * envFast + (1.0f - alphaFastAttack) * absSample
            } else {
                alphaFastRelease * envFast + (1.0f - alphaFastRelease) * absSample
            }

            envSlow = if (absSample > envSlow) {
                alphaSlowAttack * envSlow + (1.0f - alphaSlowAttack) * absSample
            } else {
                alphaSlowRelease * envSlow + (1.0f - alphaSlowRelease) * absSample
            }

            return envFast - envSlow
        }

        fun reset() {
            envFast = 0.0f
            envSlow = 0.0f
            x1 = 0.0f
            x2 = 0.0f
            y1 = 0.0f
            y2 = 0.0f
        }
    }

    /**
     * Synthesizes 2nd-order biquad bandpass filter coefficients for frequency-focused transient shaping.
     */
    private fun getFocusCoefficients(focus: TransientBandFocus, sampleRate: Int = 48000): FloatArray {
        val (f0, q) = when (focus) {
            TransientBandFocus.FULL_BAND -> Pair(1000.0f, 0.7071f)
            TransientBandFocus.BASS_PUNCH_LOW -> Pair(160.0f, 1.2f)
            TransientBandFocus.MID_ATTACK_SNAP -> Pair(2500.0f, 1.4f)
            TransientBandFocus.HIGH_PLUCK_SHINE -> Pair(7000.0f, 1.1f)
        }

        val w0 = (2.0 * Math.PI * f0 / sampleRate).toFloat()
        val cosW = cos(w0)
        val sinW = sin(w0)
        val alpha = sinW / (2.0f * q)

        val b0 = alpha
        val b1 = 0.0f
        val b2 = -alpha
        val a0 = 1.0f + alpha
        val a1 = -2.0f * cosW
        val a2 = 1.0f - alpha

        return floatArrayOf(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)
    }

    /**
     * Processes a single audio sample through the transient shaping engine.
     */
    fun processSample(
        sample: Float,
        config: TransientShaperConfig,
        state: EnvelopeFollowerState? = null
    ): Float {
        if (!config.isEnabled || config.preset == TransientPreset.OFF) {
            return sample
        }

        val absSample = abs(sample)
        val deltaEnv = state?.updateEnvelopes(absSample) ?: 0.0f
        val envSlow = state?.envSlow ?: absSample
        val envFast = state?.envFast ?: absSample

        // Calculate dynamic transient envelope gain
        val attackNorm = config.attackBoost.coerceIn(-1.0f, 1.0f)
        val sustainNorm = config.sustainBoost.coerceIn(-1.0f, 1.0f)

        val gainMod = if (deltaEnv > 0.0f) {
            val attackRatio = (deltaEnv / (envSlow + 1e-4f)).coerceIn(0.0f, 2.5f)
            val factor = if (attackNorm >= 0.0f) {
                1.0f + attackNorm * 1.8f * attackRatio
            } else {
                1.0f + attackNorm * 0.7f * attackRatio
            }
            factor.coerceIn(0.25f, 3.5f)
        } else {
            val sustainRatio = ((-deltaEnv) / (envFast + 1e-4f)).coerceIn(0.0f, 2.0f)
            val factor = if (sustainNorm >= 0.0f) {
                1.0f + sustainNorm * 1.5f * sustainRatio
            } else {
                1.0f + sustainNorm * 0.8f * sustainRatio
            }
            factor.coerceIn(0.25f, 3.0f)
        }

        // Apply frequency band focus
        var output = if (config.bandFocus == TransientBandFocus.FULL_BAND || state == null) {
            sample * gainMod
        } else {
            val coeffs = getFocusCoefficients(config.bandFocus)
            val b0 = coeffs[0]; val b1 = coeffs[1]; val b2 = coeffs[2]
            val a1 = coeffs[3]; val a2 = coeffs[4]

            val focused = b0 * sample + b1 * state.x1 + b2 * state.x2 - a1 * state.y1 - a2 * state.y2
            state.x2 = state.x1
            state.x1 = sample
            state.y2 = state.y1
            state.y1 = focused

            val rest = sample - focused
            (focused * gainMod) + rest
        }

        // Auto soft limiter to protect against clipping
        if (config.autoOutputLimit && abs(output) > 0.98f) {
            output = tanh(output)
        }

        return output.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Processes a buffer of PCM audio samples.
     */
    fun processBuffer(samples: FloatArray, config: TransientShaperConfig): FloatArray {
        if (!config.isEnabled || config.preset == TransientPreset.OFF) {
            return samples.copyOf()
        }
        val output = FloatArray(samples.size)
        val state = EnvelopeFollowerState()
        for (i in samples.indices) {
            output[i] = processSample(samples[i], config, state)
        }
        return output
    }

    /**
     * Calculates hardware equalizer transient punch offset in millibels for Band 0/1/4.
     */
    fun calculateHardwareTransientOffset(config: TransientShaperConfig): Int {
        if (!config.isEnabled || config.preset == TransientPreset.OFF) {
            return 0
        }
        val attackNorm = config.attackBoost.coerceIn(-1.0f, 1.0f)
        val baseOffset = when (config.bandFocus) {
            TransientBandFocus.FULL_BAND -> 150
            TransientBandFocus.BASS_PUNCH_LOW -> 220
            TransientBandFocus.MID_ATTACK_SNAP -> 180
            TransientBandFocus.HIGH_PLUCK_SHINE -> 120
        }
        return (baseOffset * attackNorm).roundToInt().coerceIn(-250, 300)
    }

    /**
     * Synthesizes real-time metrics for UI presentation and diagnostics.
     */
    fun calculateMetrics(config: TransientShaperConfig): TransientShaperMetrics {
        if (!config.isEnabled || config.preset == TransientPreset.OFF) {
            return TransientShaperMetrics(
                attackDeltaDb = 0.0f,
                sustainDeltaDb = 0.0f,
                crestFactorDb = 12.0f,
                dynamicPunchScore = 50,
                punchRating = "Natural Dynamics • Unmodified Envelope (Bypassed)"
            )
        }

        val attackNorm = config.attackBoost.coerceIn(-1.0f, 1.0f)
        val sustainNorm = config.sustainBoost.coerceIn(-1.0f, 1.0f)

        val attackDb = if (attackNorm >= 0.0f) {
            (20.0 * log10(1.0 + attackNorm * 1.8)).toFloat()
        } else {
            (20.0 * log10((1.0 + attackNorm * 0.65).coerceAtLeast(0.1))).toFloat()
        }

        val sustainDb = if (sustainNorm >= 0.0f) {
            (20.0 * log10(1.0 + sustainNorm * 1.5)).toFloat()
        } else {
            (20.0 * log10((1.0 + sustainNorm * 0.65).coerceAtLeast(0.1))).toFloat()
        }

        val crestFactor = (13.5f + 4.2f * attackNorm - 2.8f * sustainNorm).coerceIn(6.0f, 24.0f)
        val punchScore = (50.0f + 35.0f * attackNorm - 15.0f * sustainNorm).roundToInt().coerceIn(10, 100)

        val rating = when {
            attackNorm > 0.4f && sustainNorm < -0.1f -> "Surgical Drum Attack • Snappy Tight Punch"
            attackNorm > 0.3f -> "Elevated Dynamic Impact • Crisp Leading Transients"
            attackNorm < -0.2f -> "Smooth Relaxed Envelope • Fatigue-Free Softened Edges"
            sustainNorm > 0.3f -> "Acoustic Sustain Bloom • Deep Resonant Tail"
            sustainNorm < -0.3f -> "Dry Gated Ambience • Articulate Rhythmic Velocity"
            else -> "Balanced Dynamic Articulation"
        }

        return TransientShaperMetrics(
            attackDeltaDb = (round(attackDb * 10.0f) / 10.0f),
            sustainDeltaDb = (round(sustainDb * 10.0f) / 10.0f),
            crestFactorDb = (round(crestFactor * 10.0f) / 10.0f),
            dynamicPunchScore = punchScore,
            punchRating = rating
        )
    }
}

