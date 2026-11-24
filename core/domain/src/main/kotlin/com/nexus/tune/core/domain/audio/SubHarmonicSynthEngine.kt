package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object SubHarmonicSynthEngine {

    /**
     * Calculates perceived psychoacoustic bass gain in decibels.
     * Maps subjective loudness enhancement perceived by the auditory cortex.
     */
    fun calculatePerceivedBassGainDb(intensityPercent: Int, isEnabled: Boolean): Float {
        if (!isEnabled) return 0.0f
        return (intensityPercent.coerceIn(0, 100) / 100.0f) * 6.5f
    }

    /**
     * Translates the synthesis profile into a millibel (mB) offset for hardware equalizer Band 0 (~60 Hz).
     */
    fun calculateHardwareOffsetMb(
        intensityPercent: Int,
        isEnabled: Boolean,
        target: SubHarmonicTarget
    ): Int {
        if (!isEnabled) return 0
        val clampedIntensity = intensityPercent.coerceIn(0, 100)
        val multiplier = when (target) {
            SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS -> 3.5f // Gentle to prevent speaker distortion
            SubHarmonicTarget.HYBRID_BALANCED -> 4.8f
            SubHarmonicTarget.FULL_RANGE_SUBWOOFER -> 6.0f
        }
        return (clampedIntensity * multiplier).roundToInt().coerceIn(0, 600)
    }

    /**
     * Synthesizes 2nd and 3rd order missing fundamental harmonics using Chebyshev polynomials.
     * T_2(x) = 2x² - 1, T_3(x) = 4x³ - 3x.
     * Transmits the upper harmonic envelope allowing small speaker drivers to trigger deep bass perception.
     */
    fun synthesizeMissingFundamentalHarmonics(sample: Float, intensityPercent: Int): Float {
        if (intensityPercent <= 0) return sample
        val intensityNorm = intensityPercent.coerceIn(0, 100) / 100.0f
        val w2 = 0.22f * intensityNorm
        val w3 = 0.12f * intensityNorm

        val x = sample.coerceIn(-1.0f, 1.0f)
        val h2 = (2.0f * x * x - 1.0f) * sign(x)
        val h3 = 4.0f * x * x * x - 3.0f * x

        return (x + w2 * h2 + w3 * h3).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Synthesizes an octave-lower fundamental (f0 / 2) via zero-crossing state inversion.
     * Used for full-range subwoofers and planar magnetic headphones.
     */
    fun synthesizeSubOctaveSample(
        sample: Float,
        prevSample: Float,
        currentInversionState: Boolean,
        blendPercent: Int
    ): Pair<Float, Boolean> {
        val blendNorm = blendPercent.coerceIn(0, 100) / 100.0f
        if (blendNorm <= 0.0f) return Pair(sample, currentInversionState)

        // Toggle state on positive-going zero crossing
        val nextInversion = if (prevSample <= 0.0f && sample > 0.0f) {
            !currentInversionState
        } else {
            currentInversionState
        }

        val subOctaveComponent = if (nextInversion) sample else -sample
        val output = (sample * (1.0f - blendNorm * 0.4f) + subOctaveComponent * blendNorm * 0.45f)
            .coerceIn(-1.0f, 1.0f)

        return Pair(output, nextInversion)
    }

    /**
     * Evaluates a descriptive perceptual rating for live studio display.
     */
    fun evaluateRating(
        intensityPercent: Int,
        target: SubHarmonicTarget,
        subOctaveBlendPercent: Int
    ): String {
        return when {
            target == SubHarmonicTarget.FULL_RANGE_SUBWOOFER && subOctaveBlendPercent >= 70 ->
                "Tectonic Sub-Octave Rumble Active (30-45 Hz)"
            target == SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS && intensityPercent >= 60 ->
                "MaxxBass Phantom Fundamental Reconstructed"
            target == SubHarmonicTarget.HYBRID_BALANCED ->
                "Audiophile Balanced Fundamental & Sub-Harmonic Blend"
            intensityPercent > 0 ->
                "Subtle Sub-Bass Weight & Foundation"
            else ->
                "Passband Neutral Bass"
        }
    }

    /**
     * Synthesizes real-time metrics for UI state presentation and diagnostics.
     */
    fun calculateMetrics(config: SubHarmonicSynthConfig): SubHarmonicMetrics {
        if (!config.isEnabled) {
            return SubHarmonicMetrics(
                perceivedBassGainDb = 0.0f,
                syntheticBandwidthHz = "Bypassed (Flat 20-20,000 Hz)",
                phantomFundamentalRating = "Bypassed (Natural Passband)",
                excursionSafeguardActive = false
            )
        }

        val gainDb = calculatePerceivedBassGainDb(config.intensityPercent, config.isEnabled)
        val bandwidth = when (config.target) {
            SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS -> "Phantom: 40 Hz via 80-160 Hz Harmonics"
            SubHarmonicTarget.FULL_RANGE_SUBWOOFER -> "Sub-Octave: 25-50 Hz Direct Synthesis"
            SubHarmonicTarget.HYBRID_BALANCED -> "Hybrid: 35-120 Hz Extended Continuum"
        }
        val rating = evaluateRating(config.intensityPercent, config.target, config.subOctaveBlendPercent)
        val isSafeguard = config.target == SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS

        return SubHarmonicMetrics(
            perceivedBassGainDb = gainDb,
            syntheticBandwidthHz = bandwidth,
            phantomFundamentalRating = rating,
            excursionSafeguardActive = isSafeguard
        )
    }
}

