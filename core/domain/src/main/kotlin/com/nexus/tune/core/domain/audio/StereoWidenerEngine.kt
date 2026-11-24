package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.StereoPhaseRating
import com.nexus.tune.core.model.StereoWidthConfig
import com.nexus.tune.core.model.StereoWidthMode
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object StereoWidenerEngine {

    /**
     * Calculates Mid and Side linear coefficients based on stereo width percentage and manual trims.
     */
    fun calculateGains(
        widthPercent: Int,
        midGainDb: Float = 0f,
        sideGainDb: Float = 0f
    ): Pair<Float, Float> {
        val clampedWidth = widthPercent.coerceIn(0, 200)
        val widthFactor = clampedWidth / 100f

        val midLinear = 10f.pow(midGainDb.coerceIn(-12f, 6f) / 20f)
        val sideLinear = widthFactor * 10f.pow(sideGainDb.coerceIn(-12f, 6f) / 20f)

        return Pair(midLinear, sideLinear)
    }

    /**
     * Performs Mid/Side deconstruction and stereo reconstruction on a single sample pair.
     */
    fun reconstructStereo(
        left: Float,
        right: Float,
        midGain: Float,
        sideGain: Float
    ): Pair<Float, Float> {
        val mid = (left + right) * 0.5f * midGain
        val side = (left - right) * 0.5f * sideGain

        val newLeft = (mid + side).coerceIn(-1f, 1f)
        val newRight = (mid - side).coerceIn(-1f, 1f)
        return Pair(newLeft, newRight)
    }

    /**
     * Calculates the Pearson stereo phase correlation coefficient between Left and Right channels.
     * Correlation ranges from +1.0 (pure mono) through 0.0 (wide stereo) to -1.0 (out of phase).
     */
    fun calculatePhaseCorrelation(left: FloatArray, right: FloatArray): Float {
        if (left.isEmpty() || right.isEmpty() || left.size != right.size) {
            return 1.0f
        }

        var sumProduct = 0.0
        var sumLeftSq = 0.0
        var sumRightSq = 0.0

        for (i in left.indices) {
            val l = left[i].toDouble()
            val r = right[i].toDouble()
            sumProduct += l * r
            sumLeftSq += l * l
            sumRightSq += r * r
        }

        val denominator = sqrt(sumLeftSq * sumRightSq)
        if (denominator <= 1e-9) {
            return 1.0f // Silent or near-zero signal
        }

        val r = (sumProduct / denominator).toFloat()
        return r.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Evaluates acoustic phase safety and provides human-readable monitoring status.
     */
    fun evaluatePhaseRating(correlation: Float): StereoPhaseRating {
        val clamped = correlation.coerceIn(-1.0f, 1.0f)
        return when {
            clamped >= 0.85f -> StereoPhaseRating(
                correlation = clamped,
                rating = "Monophonic / Highly Coherent",
                isPhaseSafe = true
            )
            clamped >= 0.35f -> StereoPhaseRating(
                correlation = clamped,
                rating = "Natural Balanced Stereo",
                isPhaseSafe = true
            )
            clamped >= 0.0f -> StereoPhaseRating(
                correlation = clamped,
                rating = "Expanded Panoramic Stereo",
                isPhaseSafe = true
            )
            clamped >= -0.25f -> StereoPhaseRating(
                correlation = clamped,
                rating = "Extreme Width (Minor Out-of-Phase)",
                isPhaseSafe = false
            )
            else -> StereoPhaseRating(
                correlation = clamped,
                rating = "Phase Cancellation Alert! (Mono Comb-Filtering)",
                isPhaseSafe = false
            )
        }
    }

    /**
     * Calculates the number of delay samples required for a psychoacoustic Haas micro-delay.
     */
    fun calculateHaasDelaySamples(delayMs: Float, sampleRate: Int = 44100): Int {
        val clampedMs = delayMs.coerceIn(0f, 25f)
        return ((clampedMs / 1000f) * sampleRate).roundToInt()
    }

    /**
     * Maps stereo width percentage (0..200%) into hardware virtualizer strength (0..1000).
     */
    fun mapWidthToVirtualizerStrength(widthPercent: Int): Short {
        val clamped = widthPercent.coerceIn(0, 200)
        return ((clamped / 200f) * 1000).roundToInt().coerceIn(0, 1000).toShort()
    }

    /**
     * Returns the pre-configured StereoWidthConfig for a chosen StereoWidthMode preset.
     */
    fun getPresetConfig(mode: StereoWidthMode): StereoWidthConfig {
        return when (mode) {
            StereoWidthMode.MONO -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 0,
                haasDelayMs = 0f,
                midLevelDb = 0f,
                sideLevelDb = -12f
            )
            StereoWidthMode.NARROW -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 50,
                haasDelayMs = 0f,
                midLevelDb = 1f,
                sideLevelDb = -3f
            )
            StereoWidthMode.NATURAL -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 100,
                haasDelayMs = 0f,
                midLevelDb = 0f,
                sideLevelDb = 0f
            )
            StereoWidthMode.EXPANDED -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 135,
                haasDelayMs = 4.5f,
                midLevelDb = 0f,
                sideLevelDb = 1.5f
            )
            StereoWidthMode.SUPER_WIDE -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 175,
                haasDelayMs = 12.0f,
                midLevelDb = -0.5f,
                sideLevelDb = 3.5f
            )
            StereoWidthMode.MID_VOCAL -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 10,
                haasDelayMs = 0f,
                midLevelDb = 3.0f,
                sideLevelDb = -12f
            )
            StereoWidthMode.SIDE_AMBIENT -> StereoWidthConfig(
                isEnabled = true,
                mode = mode,
                customWidthPercent = 200,
                haasDelayMs = 18.0f,
                midLevelDb = -9.0f,
                sideLevelDb = 5.0f
            )
        }
    }
}

