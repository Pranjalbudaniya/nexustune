package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object MidBassClarityEngine {

    /**
     * Calculates the localized spectral energy congestion ratio between mid-bass and overall spectrum.
     */
    fun calculateCongestionRatio(midBassEnergy: Float, totalEnergy: Float): Float {
        val safeTotal = max(1e-6f, totalEnergy)
        return (midBassEnergy / safeTotal).coerceIn(0.0f, 1.0f)
    }

    /**
     * Calculates the dynamic parametric attenuation in decibels (negative value) based on
     * congestion ratio exceeding the mode threshold.
     */
    fun calculateDynamicAttenuationDb(
        congestionRatio: Float,
        threshold: Float,
        maxDipDb: Float
    ): Float {
        if (congestionRatio <= threshold || abs(maxDipDb) < 1e-3f) return 0.0f
        val excessRatio = ((congestionRatio - threshold) / max(1e-4f, 1.0f - threshold)).coerceIn(0.0f, 1.0f)
        return (excessRatio * maxDipDb).coerceIn(maxDipDb, 0.0f)
    }

    /**
     * Translates dynamic attenuation in dB to hardware equalizer millibel offset for the 230 Hz band.
     */
    fun calculateHardwareBandOffset(attenuationDb: Float): Int {
        if (attenuationDb >= -0.05f) return 0
        return (attenuationDb * 100.0f).roundToInt().coerceIn(-1200, 0)
    }

    /**
     * Evaluates a human-readable clarity rating for live studio visualization.
     */
    fun evaluateClarityRating(congestionRatio: Float, attenuationDb: Float): String {
        return when {
            attenuationDb < -3.5f -> "Heavy De-Mudding Active (${String.format(java.util.Locale.US, "%.1f", attenuationDb)} dB)"
            attenuationDb < -0.5f -> "Gentle Decongesting (${String.format(java.util.Locale.US, "%.1f", attenuationDb)} dB)"
            congestionRatio > 0.40f -> "Congestion Managed (Warm & Open)"
            else -> "Pristine Balance (Uncongested)"
        }
    }

    /**
     * Synthesizes comprehensive real-time metrics for UI state presentation.
     */
    fun calculateMetrics(
        midBassEnergy: Float,
        totalEnergy: Float,
        config: MidBassClarityConfig
    ): MidBassClarityMetrics {
        if (!config.isEnabled || config.mode == MidBassClarityMode.OFF) {
            return MidBassClarityMetrics(
                congestionRatio = 0.0f,
                dynamicAttenuationDb = 0.0f,
                isDecongesting = false,
                clarityRating = "Bypassed"
            )
        }

        val ratio = calculateCongestionRatio(midBassEnergy, totalEnergy)
        val threshold = (config.dynamicThresholdPercent / 100.0f).coerceIn(0.1f, 0.9f)
        val attenuation = calculateDynamicAttenuationDb(ratio, threshold, config.mode.maxDipDb)
        val isDecongesting = attenuation < -0.2f
        val rating = evaluateClarityRating(ratio, attenuation)

        return MidBassClarityMetrics(
            congestionRatio = ratio,
            dynamicAttenuationDb = attenuation,
            isDecongesting = isDecongesting,
            clarityRating = rating
        )
    }

    /**
     * Computes unconditionally stable peaking/dip biquad filter coefficients (RBJ Audio EQ).
     */
    fun calculateBiquadDipCoefficients(
        centerHz: Float,
        attenuationDb: Float,
        q: Float = 1.4f,
        sampleRate: Int = 44100
    ): MidBassBiquadCoefficients {
        if (abs(attenuationDb) < 1e-4f) {
            return MidBassBiquadCoefficients(b0 = 1.0f, b1 = 0.0f, b2 = 0.0f, a1 = 0.0f, a2 = 0.0f)
        }

        val safeCenter = centerHz.coerceIn(100.0f, 1000.0f).toDouble()
        val safeSr = sampleRate.toDouble()
        val aLinear = 10.0.pow(attenuationDb.toDouble() / 40.0)
        val omega0 = 2.0 * Math.PI * (safeCenter / safeSr)
        val alpha = sin(omega0) / (2.0 * q.toDouble())
        val cosW = cos(omega0)

        val b0 = 1.0 + alpha * aLinear
        val b1 = -2.0 * cosW
        val b2 = 1.0 - alpha * aLinear
        val a0 = 1.0 + alpha / aLinear
        val a1 = -2.0 * cosW
        val a2 = 1.0 - alpha / aLinear

        return MidBassBiquadCoefficients(
            b0 = (b0 / a0).toFloat(),
            b1 = (b1 / a0).toFloat(),
            b2 = (b2 / a0).toFloat(),
            a1 = (a1 / a0).toFloat(),
            a2 = (a2 / a0).toFloat()
        )
    }

    class MidBassFilterState(
        var x1L: Float = 0.0f,
        var x2L: Float = 0.0f,
        var y1L: Float = 0.0f,
        var y2L: Float = 0.0f,
        var x1R: Float = 0.0f,
        var x2R: Float = 0.0f,
        var y1R: Float = 0.0f,
        var y2R: Float = 0.0f
    ) {
        fun reset() {
            x1L = 0.0f
            x2L = 0.0f
            y1L = 0.0f
            y2L = 0.0f
            x1R = 0.0f
            x2R = 0.0f
            y1R = 0.0f
            y2R = 0.0f
        }
    }

    /**
     * Processes a single stereo frame through the dynamic mid-bass clarity dip filter.
     */
    fun processFrame(
        left: Float,
        right: Float,
        attenuationDb: Float,
        config: MidBassClarityConfig,
        state: MidBassFilterState,
        sampleRate: Int = 44100
    ): Pair<Float, Float> {
        if (!config.isEnabled || config.mode == MidBassClarityMode.OFF || abs(attenuationDb) < 1e-4f) {
            return Pair(left, right)
        }

        val coeffs = calculateBiquadDipCoefficients(config.centerFrequencyHz, attenuationDb, 1.4f, sampleRate)

        val outL = coeffs.b0 * left + coeffs.b1 * state.x1L + coeffs.b2 * state.x2L - coeffs.a1 * state.y1L - coeffs.a2 * state.y2L
        state.x2L = state.x1L
        state.x1L = left
        state.y2L = state.y1L
        state.y1L = outL

        val outR = coeffs.b0 * right + coeffs.b1 * state.x1R + coeffs.b2 * state.x2R - coeffs.a1 * state.y1R - coeffs.a2 * state.y2R
        state.x2R = state.x1R
        state.x1R = right
        state.y2R = state.y1R
        state.y1R = outR

        return Pair(outL.coerceIn(-1.0f, 1.0f), outR.coerceIn(-1.0f, 1.0f))
    }
}

