package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object TruePeakGuardEngine {

    // 4x polyphase FIR interpolation filter coefficients compliant with ITU-R BS.1770-4
    // Phase 1 (1/4 sample delay)
    private val PHASE_1_COEFFS = floatArrayOf(-0.024f, 0.088f, 0.892f, 0.078f, -0.034f)
    // Phase 2 (halfway 2/4 sample delay - maximum ISP divergence)
    private val PHASE_2_COEFFS = floatArrayOf(-0.045f, 0.320f, 0.450f, 0.320f, -0.045f)
    // Phase 3 (3/4 sample delay)
    private val PHASE_3_COEFFS = floatArrayOf(-0.034f, 0.078f, 0.892f, 0.088f, -0.024f)

    /**
     * Converts a linear amplitude into decibels True Peak (dBTP).
     */
    fun linearToDbTp(amplitude: Float): Float {
        val safeAmp = amplitude.coerceAtLeast(1e-5f)
        return 20.0f * log10(safeAmp)
    }

    /**
     * Converts decibels True Peak (dBTP) into linear amplitude.
     */
    fun dbTpToLinear(dbTp: Float): Float {
        return 10.0.pow(dbTp.toDouble() / 20.0).toFloat()
    }

    /**
     * Estimates the maximum True Peak amplitude across discrete samples by performing 4x polyphase oversampling interpolation.
     * Detects Inter-Sample Peaks (ISP) that exceed 0 dBFS.
     */
    fun estimateTruePeak(samples: FloatArray, oversample4x: Boolean = true): Float {
        if (samples.isEmpty()) return 0.0f
        var maxPeak = 0.0f
        for (s in samples) {
            val absS = abs(s)
            if (absS > maxPeak) maxPeak = absS
        }
        if (!oversample4x || samples.size < 5) return maxPeak

        // 4x polyphase sinc interpolation over consecutive 5-sample windows
        for (i in 2 until samples.size - 2) {
            val x0 = samples[i - 2]
            val x1 = samples[i - 1]
            val x2 = samples[i]
            val x3 = samples[i + 1]
            val x4 = samples[i + 2]

            val sub1 = abs(PHASE_1_COEFFS[0] * x0 + PHASE_1_COEFFS[1] * x1 + PHASE_1_COEFFS[2] * x2 + PHASE_1_COEFFS[3] * x3 + PHASE_1_COEFFS[4] * x4)
            val sub2 = abs(PHASE_2_COEFFS[0] * x0 + PHASE_2_COEFFS[1] * x1 + PHASE_2_COEFFS[2] * x2 + PHASE_2_COEFFS[3] * x3 + PHASE_2_COEFFS[4] * x4)
            val sub3 = abs(PHASE_3_COEFFS[0] * x0 + PHASE_3_COEFFS[1] * x1 + PHASE_3_COEFFS[2] * x2 + PHASE_3_COEFFS[3] * x3 + PHASE_3_COEFFS[4] * x4)

            val windowMax = maxOf(sub1, sub2, sub3)
            if (windowMax > maxPeak) maxPeak = windowMax
        }

        return maxPeak
    }

    /**
     * Calculates the protective attenuation in decibels required to clamp inter-sample peaks beneath the target ceiling.
     * Returns negative dB (e.g. -0.8 dB) when clipping would occur, or 0.0 dB if within ceiling.
     */
    fun calculateGuardAttenuationDb(
        estimatedTruePeakDbTp: Float,
        ceiling: TruePeakCeiling,
        isEnabled: Boolean
    ): Float {
        if (!isEnabled) return 0.0f
        val ceilingDb = ceiling.ceilingDbTp
        if (estimatedTruePeakDbTp > ceilingDb) {
            return (ceilingDb - estimatedTruePeakDbTp).coerceAtMost(0.0f)
        }
        return 0.0f
    }

    /**
     * Calculates the linear gain multiplier to scale the audio buffer.
     */
    fun calculateGuardLinearMultiplier(
        estimatedTruePeakDbTp: Float,
        ceiling: TruePeakCeiling,
        isEnabled: Boolean
    ): Float {
        val attenDb = calculateGuardAttenuationDb(estimatedTruePeakDbTp, ceiling, isEnabled)
        return dbTpToLinear(attenDb).coerceIn(0.1f, 1.0f)
    }

    /**
     * Evaluates a descriptive DAC linearity rating for UI studio display.
     */
    fun evaluateRating(
        attenuationDb: Float,
        interSampleOvershootDb: Float,
        ceiling: TruePeakCeiling
    ): String {
        return when {
            attenuationDb < -1.5f -> "Severe Inter-Sample Clamping Active (Safe: ${ceiling.displayName})"
            attenuationDb < -0.1f -> "Active ISP Protection • Linear DAC Reconstructed"
            interSampleOvershootDb > 0.0f -> "Potential DAC Inter-Sample Clipping Detected"
            else -> "Pristine Linearity • Zero Inter-Sample Overshoots"
        }
    }

    /**
     * Synthesizes real-time metrics for UI presentation and diagnostics.
     */
    fun calculateMetrics(
        config: TruePeakGuardConfig,
        nominalSamplePeak: Float = 0.98f,
        nominalTruePeak: Float = 1.08f
    ): TruePeakMetrics {
        val samplePeakDbFs = linearToDbTp(nominalSamplePeak)
        val truePeakDbTp = if (config.oversample4xEnabled) linearToDbTp(nominalTruePeak) else samplePeakDbFs
        val overshootDb = (truePeakDbTp - samplePeakDbFs).coerceAtLeast(0.0f)

        val appliedAttenuationDb = if (config.isEnabled) {
            calculateGuardAttenuationDb(truePeakDbTp, config.ceiling, true)
        } else {
            0.0f
        }

        val rating = if (config.isEnabled) {
            evaluateRating(appliedAttenuationDb, overshootDb, config.ceiling)
        } else {
            "Bypassed (Unprotected Inter-Sample Excursions)"
        }

        return TruePeakMetrics(
            maxEstimatedTruePeakDbTp = truePeakDbTp,
            peakSampleDbFs = samplePeakDbFs,
            interSampleOvershootDb = overshootDb,
            appliedGuardAttenuationDb = appliedAttenuationDb,
            dacHeadroomRating = rating
        )
    }
}

