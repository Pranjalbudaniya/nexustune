package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object DeEsserEngine {

    /**
     * Synthesizes 2nd-order band-pass detection filter coefficients for monitoring vocal sibilance energy.
     * Uses standard Audio EQ Cookbook band-pass equations (constant skirt gain, peak gain = Q).
     */
    fun calculateBandpassDetectionCoefficients(
        centerFrequencyHz: Float,
        q: Float = 2.0f,
        sampleRate: Int = 48000
    ): DeEsserBiquadCoefficients {
        val nyquist = sampleRate / 2.0f
        val clampedFreq = centerFrequencyHz.coerceIn(3000.0f, nyquist - 500.0f)
        val omega0 = 2.0 * Math.PI * (clampedFreq.toDouble() / sampleRate.toDouble())
        val cosW = cos(omega0)
        val sinW = sin(omega0)
        val alpha = sinW / (2.0 * q.toDouble().coerceAtLeast(0.5))

        val b0Raw = alpha * q.toDouble()
        val b1Raw = 0.0
        val b2Raw = -alpha * q.toDouble()
        val a0Raw = 1.0 + alpha
        val a1Raw = -2.0 * cosW
        val a2Raw = 1.0 - alpha

        return DeEsserBiquadCoefficients(
            b0 = (b0Raw / a0Raw).toFloat(),
            b1 = (b1Raw / a0Raw).toFloat(),
            b2 = (b2Raw / a0Raw).toFloat(),
            a1 = (a1Raw / a0Raw).toFloat(),
            a2 = (a2Raw / a0Raw).toFloat()
        )
    }

    /**
     * Calculates dynamic gain reduction in decibels (negative value) based on localized sibilance ratio.
     */
    fun calculateDynamicReductionDb(
        sibilanceRatio: Float,
        thresholdPercent: Int,
        maxReductionDb: Float
    ): Float {
        val thresholdNorm = thresholdPercent.coerceIn(10, 80) / 100.0f
        if (sibilanceRatio <= thresholdNorm) return 0.0f

        val excessRatio = ((sibilanceRatio - thresholdNorm) / (1.0f - thresholdNorm)).coerceIn(0.0f, 1.0f)
        val reduction = -1.0f * (excessRatio * maxReductionDb.coerceIn(0.0f, 12.0f))
        return reduction.coerceIn(-maxReductionDb, 0.0f)
    }

    /**
     * Translates dynamic decibel reduction into a millibel (mB) attenuation offset for hardware equalizer Band 3 (~4-6 kHz).
     */
    fun calculateHardwareOffsetMb(reductionDb: Float, isEnabled: Boolean): Int {
        if (!isEnabled) return 0
        return (reductionDb * 100.0f).roundToInt().coerceIn(-1000, 0)
    }

    /**
     * Evaluates a descriptive perceptual rating for UI studio display.
     */
    fun evaluateRating(maxReductionDb: Float, centerFreqHz: Float): String {
        return when {
            maxReductionDb >= 7.0f -> "Split-Band Brickwall Harshness Clamp (${centerFreqHz.toInt()} Hz)"
            maxReductionDb >= 5.0f -> "Bright Headphone Resonance Tamed (${centerFreqHz.toInt()} Hz)"
            maxReductionDb >= 3.0f -> "Studio Intimate Vocal Sibilance Control"
            maxReductionDb > 0.5f -> "Subtle Transparent Consonant Softener"
            else -> "Passband Neutral Sibilants"
        }
    }

    /**
     * Synthesizes real-time metrics for UI state presentation and diagnostics.
     */
    fun calculateMetrics(
        config: DeEsserConfig,
        observedSibilanceActivityPercent: Int = 40
    ): DeEsserMetrics {
        if (!config.isEnabled) {
            return DeEsserMetrics(
                dynamicReductionDb = 0.0f,
                sibilanceActivityPercent = 0,
                vocalSmoothnessRating = "Bypassed (Natural Sibilants)",
                fatigueReductionPercent = 0
            )
        }

        val nominalRatio = (observedSibilanceActivityPercent / 100.0f).coerceIn(0.0f, 1.0f)
        val dynamicReduction = calculateDynamicReductionDb(nominalRatio, config.thresholdPercent, config.maxReductionDb)
        val rating = evaluateRating(config.maxReductionDb, config.centerFrequencyHz)
        val fatigueReduction = ((config.maxReductionDb / 8.0f) * 65.0f + (config.thresholdPercent * 0.35f))
            .roundToInt().coerceIn(10, 95)

        return DeEsserMetrics(
            dynamicReductionDb = dynamicReduction,
            sibilanceActivityPercent = observedSibilanceActivityPercent,
            vocalSmoothnessRating = rating,
            fatigueReductionPercent = fatigueReduction
        )
    }
}

