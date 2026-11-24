package com.nexus.tune.core.player

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility for converting and formatting musical semitone transpositions and pitch multipliers.
 *
 * Formula: pitchRatio = 2^(semitones / 12)
 * Standard transposition range: -6 to +6 semitones (octave span centered on original key).
 */
object PitchTransposer {

    const val MIN_SEMITONES = -6
    const val MAX_SEMITONES = 6

    /**
     * Converts a semitone offset (-6..+6) to a frequency/pitch multiplier ratio.
     * e.g., 0 -> 1.0f, +12 -> 2.0f, +1 -> 1.05946f, -1 -> 0.94387f
     */
    fun semitonesToPitchRatio(semitones: Int): Float {
        val clamped = semitones.coerceIn(MIN_SEMITONES, MAX_SEMITONES)
        return (2.0.pow(clamped.toDouble() / 12.0)).toFloat()
    }

    /**
     * Converts a pitch multiplier ratio back to the nearest integer semitone offset.
     */
    fun pitchRatioToSemitones(ratio: Float): Int {
        if (ratio <= 0f) return 0
        val semitones = (12.0 * log2(ratio.toDouble())).roundToInt()
        return semitones.coerceIn(MIN_SEMITONES, MAX_SEMITONES)
    }

    /**
     * Formats semitones into human-readable musical key transposition strings.
     * e.g. "+2 st", "-1 st", "Original (0)"
     */
    fun formatSemitones(semitones: Int): String {
        return when {
            semitones > 0 -> "+$semitones st"
            semitones < 0 -> "$semitones st"
            else -> "Original (0)"
        }
    }

    /**
     * Formats pitch multiplier to 2 decimal places with 'x' suffix.
     * e.g. "1.12x", "1.00x", "0.89x"
     */
    fun formatPitchMultiplier(ratio: Float): String {
        return String.format(java.util.Locale.US, "%.2fx", ratio)
    }
}

