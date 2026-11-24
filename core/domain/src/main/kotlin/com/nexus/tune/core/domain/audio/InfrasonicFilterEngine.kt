package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object InfrasonicFilterEngine {

    const val DC_BLOCKER_R = 0.9995f

    /**
     * Calculates the attenuation in decibels at 10 Hz subsonic rumble frequency.
     */
    fun calculateRumbleAttenuationAt10Hz(cutoffHz: Float, slope: InfrasonicSlope): Float {
        if (cutoffHz <= 10.0f) return 0.0f
        val octavesBelowCutoff = log2(cutoffHz.toDouble() / 10.0).toFloat()
        return -1.0f * (slope.dbPerOctave * octavesBelowCutoff)
    }

    /**
     * Calculates dynamic electrical amplifier headroom restored by eliminating subsonic energy.
     */
    fun calculateHeadroomRestoredDb(cutoffHz: Float, slope: InfrasonicSlope): Float {
        val baseHeadroom = 0.6f + (cutoffHz - 15.0f).coerceAtLeast(0.0f) * 0.06f
        val slopeBonus = slope.dbPerOctave * 0.035f
        return (baseHeadroom + slopeBonus).coerceIn(0.5f, 3.5f)
    }

    /**
     * Evaluates a descriptive protection rating for live studio visualization.
     */
    fun evaluateProtectionRating(cutoffHz: Float, slope: InfrasonicSlope, dcBlocked: Boolean): String {
        return when {
            cutoffHz >= 25.0f && slope == InfrasonicSlope.SLOPE_24DB_OCTAVE -> "Turntable Warp Brickwall Suppressed"
            dcBlocked && cutoffHz >= 20.0f -> "Pristine Passband • DC Bias & Rumble Blocked"
            dcBlocked -> "DC Offset Neutralized"
            else -> "Gentle Sub-Bass Rolloff Active"
        }
    }

    /**
     * Synthesizes comprehensive real-time metrics for UI state presentation.
     */
    fun calculateMetrics(config: InfrasonicFilterConfig): InfrasonicMetrics {
        if (!config.isEnabled) {
            return InfrasonicMetrics(
                cutoffHz = 0.0f,
                preservedHeadroomDb = 0.0f,
                rumbleAttenuationAt10HzDb = 0.0f,
                protectionRating = "Bypassed (Unfiltered Subsonics)"
            )
        }

        val atten = calculateRumbleAttenuationAt10Hz(config.cutoffHz, config.slope)
        val headroom = calculateHeadroomRestoredDb(config.cutoffHz, config.slope)
        val rating = evaluateProtectionRating(config.cutoffHz, config.slope, config.dcBlockerEnabled)

        return InfrasonicMetrics(
            cutoffHz = config.cutoffHz,
            preservedHeadroomDb = headroom,
            rumbleAttenuationAt10HzDb = atten,
            protectionRating = rating
        )
    }

    /**
     * Calculates 2nd-order Butterworth/Bessel high-pass biquad coefficients (RBJ Audio EQ Cookbook).
     */
    fun calculateHighPassBiquadCoefficients(
        cutoffHz: Float,
        q: Float = 0.7071f,
        sampleRate: Int = 44100
    ): InfrasonicBiquadCoefficients {
        val safeCutoff = cutoffHz.coerceIn(5.0f, 100.0f).toDouble()
        val safeSr = sampleRate.toDouble()
        val omega0 = 2.0 * Math.PI * (safeCutoff / safeSr)
        val alpha = sin(omega0) / (2.0 * q.toDouble())
        val cosW = cos(omega0)

        val b0 = (1.0 + cosW) / 2.0
        val b1 = -(1.0 + cosW)
        val b2 = (1.0 + cosW) / 2.0
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosW
        val a2 = 1.0 - alpha

        return InfrasonicBiquadCoefficients(
            b0 = (b0 / a0).toFloat(),
            b1 = (b1 / a0).toFloat(),
            b2 = (b2 / a0).toFloat(),
            a1 = (a1 / a0).toFloat(),
            a2 = (a2 / a0).toFloat()
        )
    }

    class InfrasonicFilterState(
        var x1L: Float = 0.0f,
        var x2L: Float = 0.0f,
        var y1L: Float = 0.0f,
        var y2L: Float = 0.0f,
        var dcX1L: Float = 0.0f,
        var dcY1L: Float = 0.0f,
        var x1R: Float = 0.0f,
        var x2R: Float = 0.0f,
        var y1R: Float = 0.0f,
        var y2R: Float = 0.0f,
        var dcX1R: Float = 0.0f,
        var dcY1R: Float = 0.0f
    ) {
        fun reset() {
            x1L = 0.0f; x2L = 0.0f; y1L = 0.0f; y2L = 0.0f; dcX1L = 0.0f; dcY1L = 0.0f
            x1R = 0.0f; x2R = 0.0f; y1R = 0.0f; y2R = 0.0f; dcX1R = 0.0f; dcY1R = 0.0f
        }
    }

    /**
     * Processes a single stereo frame through the infrasonic high-pass and DC blocker pipeline.
     */
    fun processFrame(
        left: Float,
        right: Float,
        config: InfrasonicFilterConfig,
        state: InfrasonicFilterState,
        sampleRate: Int = 44100
    ): Pair<Float, Float> {
        if (!config.isEnabled) {
            return Pair(left, right)
        }

        var inL = left
        var inR = right

        // 1. Single-pole recursive DC offset eliminator: y[n] = x[n] - x[n-1] + R * y[n-1]
        if (config.dcBlockerEnabled) {
            val dcL = inL - state.dcX1L + DC_BLOCKER_R * state.dcY1L
            state.dcX1L = inL
            state.dcY1L = dcL
            inL = dcL

            val dcR = inR - state.dcX1R + DC_BLOCKER_R * state.dcY1R
            state.dcX1R = inR
            state.dcY1R = dcR
            inR = dcR
        }

        // 2. High-pass filter stage
        val q = if (config.slope == InfrasonicSlope.SLOPE_12DB_OCTAVE) 0.5f else 0.7071f
        val coeffs = calculateHighPassBiquadCoefficients(config.cutoffHz, q, sampleRate)

        val outL = coeffs.b0 * inL + coeffs.b1 * state.x1L + coeffs.b2 * state.x2L - coeffs.a1 * state.y1L - coeffs.a2 * state.y2L
        state.x2L = state.x1L
        state.x1L = inL
        state.y2L = state.y1L
        state.y1L = outL

        val outR = coeffs.b0 * inR + coeffs.b1 * state.x1R + coeffs.b2 * state.x2R - coeffs.a1 * state.y1R - coeffs.a2 * state.y2R
        state.x2R = state.x1R
        state.x1R = inR
        state.y2R = state.y1R
        state.y1R = outR

        return Pair(outL.coerceIn(-1.0f, 1.0f), outR.coerceIn(-1.0f, 1.0f))
    }
}

