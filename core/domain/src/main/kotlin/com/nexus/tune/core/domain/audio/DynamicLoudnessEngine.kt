package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DynamicLoudnessMode
import com.nexus.tune.core.model.LoudnessCurveOffsets
import kotlin.math.pow
import kotlin.math.roundToInt

object DynamicLoudnessEngine {

    /**
     * Calculates the psychoacoustic attenuation factor (0.0 .. 1.0) based on volume fraction.
     * At 1.0 (reference SPL), attenuation is 0.0 (no compensation required).
     * As volume decreases, the factor scales according to human auditory thresholds (ISO 226).
     */
    fun calculatePerceivedAttenuationFactor(volumeFraction: Float): Float {
        val clamped = volumeFraction.coerceIn(0f, 1f)
        // Non-linear perceptual scaling modeling human loudness perception exponent
        return (1.0f - clamped.toDouble().pow(0.6)).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Returns 5-band millibel EQ offsets [60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz]
     * calibrated to restore bass and treble fullness dynamically at the current volume level.
     */
    fun calculateContourOffsets(
        mode: DynamicLoudnessMode,
        currentVolume: Float,
        intensityPercent: Int = 100
    ): List<Int> {
        if (mode == DynamicLoudnessMode.OFF || intensityPercent <= 0) {
            return listOf(0, 0, 0, 0, 0)
        }

        val factor = calculatePerceivedAttenuationFactor(currentVolume)
        val intensity = (intensityPercent.coerceIn(0, 100) / 100f) * factor

        val bassBoostMb = (mode.maxBassBoostDb * 100f * intensity).roundToInt()
        val midBassBoostMb = (mode.maxBassBoostDb * 55f * intensity).roundToInt()
        val trebleBoostMb = (mode.maxTrebleBoostDb * 100f * intensity).roundToInt()
        // Subtle dip in ear canal resonance zone (3-4kHz) to prevent auditory fatigue
        val presenceCutMb = (-mode.maxTrebleBoostDb * 10f * intensity).roundToInt()

        return listOf(
            bassBoostMb.coerceIn(-1500, 1500),
            midBassBoostMb.coerceIn(-1500, 1500),
            0,
            presenceCutMb.coerceIn(-1500, 1500),
            trebleBoostMb.coerceIn(-1500, 1500)
        )
    }

    /**
     * Calculates full loudness curve offsets including effective dB gain metrics.
     */
    fun calculateLoudnessCurve(
        mode: DynamicLoudnessMode,
        currentVolume: Float,
        intensityPercent: Int = 100
    ): LoudnessCurveOffsets {
        val bands = calculateContourOffsets(mode, currentVolume, intensityPercent)
        val bassGainDb = bands[0] / 100f
        val trebleGainDb = bands[4] / 100f
        return LoudnessCurveOffsets(
            bandOffsetsMb = bands,
            effectiveBassGainDb = bassGainDb,
            effectiveTrebleGainDb = trebleGainDb
        )
    }

    /**
     * Estimates perceived bass loss in decibels according to ISO 226 equal-loudness contours.
     */
    fun estimatePerceivedBassLossDb(currentVolume: Float): Float {
        val factor = calculatePerceivedAttenuationFactor(currentVolume)
        return (factor * 12.0f).coerceIn(0f, 15f)
    }

    /**
     * Returns the recommended safe digital pre-amp headroom reduction to eliminate digital clipping.
     */
    fun calculateSafeHeadroomPreAmpDb(maxBoostDb: Float, intensityPercent: Int = 100): Float {
        val scaled = maxBoostDb * (intensityPercent.coerceIn(0, 100) / 100f)
        return -(scaled * 0.4f)
    }
}

