package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object AirBandExciterEngine {

    /**
     * Synthesizes digital biquad coefficients for a high-shelf filter centered at the requested air frequency.
     * Implements standard bilinear z-transform Audio EQ Cookbook equations with slope S = 1.0.
     */
    fun calculateBiquadCoefficients(
        frequencyHz: Int,
        boostDb: Float,
        sampleRate: Int = 48000
    ): AirBandBiquadCoefficients {
        val clampedBoost = boostDb.coerceIn(0.0f, 12.0f)
        val A = 10.0.pow(clampedBoost.toDouble() / 40.0)
        val nyquist = sampleRate / 2.0
        val clampedFreq = frequencyHz.toDouble().coerceIn(1000.0, nyquist - 200.0)
        val omega0 = 2.0 * Math.PI * clampedFreq / sampleRate.toDouble()
        val cosW = cos(omega0)
        val sinW = sin(omega0)
        val slope = 1.0
        val alpha = sinW / 2.0 * sqrt((A + 1.0 / A) * (1.0 / slope - 1.0) + 2.0)
        val sqrtA2Alpha = 2.0 * sqrt(A) * alpha

        val a0 = (A + 1.0) - (A - 1.0) * cosW + sqrtA2Alpha
        val a1Raw = 2.0 * ((A - 1.0) - (A + 1.0) * cosW)
        val a2Raw = (A + 1.0) - (A - 1.0) * cosW - sqrtA2Alpha

        val b0Raw = A * ((A + 1.0) + (A - 1.0) * cosW + sqrtA2Alpha)
        val b1Raw = -2.0 * A * ((A - 1.0) + (A + 1.0) * cosW)
        val b2Raw = A * ((A + 1.0) + (A - 1.0) * cosW - sqrtA2Alpha)

        return AirBandBiquadCoefficients(
            b0 = (b0Raw / a0).toFloat(),
            b1 = (b1Raw / a0).toFloat(),
            b2 = (b2Raw / a0).toFloat(),
            a1 = (a1Raw / a0).toFloat(),
            a2 = (a2Raw / a0).toFloat()
        )
    }

    /**
     * Translates the decibel boost into hardware equalizer millibels (mB) targeting Band 4 (high treble).
     * 1.0 dB = 100 mB.
     */
    fun calculateHardwareOffsetMb(boostDb: Float, isEnabled: Boolean): Int {
        if (!isEnabled) return 0
        return (boostDb * 100.0f).roundToInt().coerceIn(0, 1000)
    }

    /**
     * Generates musical even-order harmonics (x²) over high-frequency audio content.
     * Restores silky sheen and breath lost to lossy MP3/AAC 16 kHz brickwall encoding.
     */
    fun generateHarmonics(sample: Float, harmonicExcitationPercent: Int): Float {
        if (harmonicExcitationPercent <= 0) return sample
        val gamma = (harmonicExcitationPercent.coerceIn(0, 100) / 100.0f) * 0.20f
        // Non-linear even harmonic generator preserving sign symmetry: y = x + gamma * (x^2 * sgn(x))
        val harmonic = sample * sample * sign(sample)
        return (sample + gamma * harmonic).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Evaluates a descriptive perceptual sheen rating for UI studio display.
     */
    fun evaluateSheenDescriptor(
        boostDb: Float,
        harmonicPercent: Int,
        frequency: AirBandFrequency
    ): String {
        return when {
            boostDb >= 5.0f && harmonicPercent >= 50 -> "Euphoric Lossless Sparkle & Top-End Radiance"
            boostDb >= 4.0f -> "Silky Analog Pultec Sheen"
            boostDb >= 2.5f && frequency == AirBandFrequency.HZ_12000 -> "Crisp Vocal Intelligibility & Breath"
            boostDb >= 2.5f && frequency == AirBandFrequency.HZ_20000 -> "Maag 20 kHz Holographic 3D Space"
            boostDb > 0.5f -> "Subtle Velvet Atmosphere"
            else -> "Pristine Flat Treble Passband"
        }
    }

    /**
     * Synthesizes real-time metrics for UI state presentation and diagnostics.
     */
    fun calculateMetrics(config: AirBandExciterConfig): AirBandMetrics {
        if (!config.isEnabled) {
            return AirBandMetrics(
                frequencyHz = config.frequency.frequencyHz,
                boostDb = 0.0f,
                harmonicDistortionPercent = 0.0f,
                airEnergyIndexPercent = 35,
                sheenDescriptor = "Bypassed (Natural Highs)"
            )
        }

        val effectiveBoost = config.boostDb.coerceIn(0.0f, 8.0f)
        val thd = (config.harmonicExcitationPercent.coerceIn(0, 100) * 0.035f)
        val energyIndex = (40 + (effectiveBoost * 7.5f) + (config.harmonicExcitationPercent * 0.20f))
            .roundToInt().coerceIn(0, 100)
        val descriptor = evaluateSheenDescriptor(effectiveBoost, config.harmonicExcitationPercent, config.frequency)

        return AirBandMetrics(
            frequencyHz = config.frequency.frequencyHz,
            boostDb = effectiveBoost,
            harmonicDistortionPercent = thd,
            airEnergyIndexPercent = energyIndex,
            sheenDescriptor = descriptor
        )
    }
}

