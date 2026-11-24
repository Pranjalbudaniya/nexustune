package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object PhaseMeterEngine {

    /**
     * Calculates the normalized Pearson cross-correlation coefficient r in [-1.0, +1.0]
     * between Left and Right stereo channels.
     */
    fun calculatePearsonCorrelation(left: FloatArray, right: FloatArray): Float {
        val size = min(left.size, right.size)
        if (size <= 0) return 1.0f

        var sumL = 0.0
        var sumR = 0.0
        for (i in 0 until size) {
            sumL += left[i].toDouble()
            sumR += right[i].toDouble()
        }
        val meanL = sumL / size.toDouble()
        val meanR = sumR / size.toDouble()

        var cov = 0.0
        var varL = 0.0
        var varR = 0.0

        for (i in 0 until size) {
            val diffL = left[i].toDouble() - meanL
            val diffR = right[i].toDouble() - meanR
            cov += diffL * diffR
            varL += diffL * diffL
            varR += diffR * diffR
        }

        val denom = sqrt(varL * varR)
        if (denom < 1e-9) {
            // Both channels silent or DC
            return 1.0f
        }

        return (cov / denom).toFloat().coerceIn(-1.0f, 1.0f)
    }

    /**
     * Evaluates the categorical phase correlation rating and mono compatibility.
     */
    fun evaluatePhaseRating(correlation: Float): PhaseCorrelationRating {
        return when {
            correlation > 0.95f -> PhaseCorrelationRating.PERFECT_MONO
            correlation >= 0.55f -> PhaseCorrelationRating.EXCELLENT_STEREO
            correlation >= 0.20f -> PhaseCorrelationRating.ACCEPTABLE_WIDE
            correlation >= -0.20f -> PhaseCorrelationRating.PHASE_WARNING
            else -> PhaseCorrelationRating.CRITICAL_CANCELLATION
        }
    }

    /**
     * Calculates required side-channel attenuation in decibels to restore mono compatibility
     * under the selected MonoGuard mode.
     */
    fun calculateMonoGuardSideAttenuationDb(correlation: Float, guardMode: MonoGuardMode): Float {
        if (guardMode == MonoGuardMode.OFF) return 0.0f
        val threshold = guardMode.threshold
        if (correlation >= threshold) return 0.0f

        val deficit = threshold - correlation
        val maxDeficit = threshold + 1.0f
        val ratio = (deficit / maxDeficit).coerceIn(0f, 1f)

        // Attenuate side channel up to -18 dB
        return -18.0f * ratio
    }

    /**
     * Synthesizes comprehensive phase metrics including correlation coefficient, rating, and mono safety.
     */
    fun calculatePhaseMetrics(
        left: FloatArray,
        right: FloatArray,
        guardMode: MonoGuardMode = MonoGuardMode.INTELLIGENT_SAFEGUARD
    ): PhaseCorrelationMetrics {
        val corr = calculatePearsonCorrelation(left, right)
        val rating = evaluatePhaseRating(corr)
        val attenuationDb = calculateMonoGuardSideAttenuationDb(corr, guardMode)
        return PhaseCorrelationMetrics(
            correlationCoeff = corr,
            rating = rating,
            sideAttenuationDb = attenuationDb,
            isMonoSafe = rating.isMonoSafe
        )
    }

    /**
     * Processes a stereo frame through the Mono-Guard DSP, compressing side-channel energy
     * when phase cancellation risks acoustic degradation on mono speakers.
     */
    fun processPhaseSafeFrame(
        left: FloatArray,
        right: FloatArray,
        guardMode: MonoGuardMode = MonoGuardMode.INTELLIGENT_SAFEGUARD
    ): Pair<FloatArray, FloatArray> {
        val size = min(left.size, right.size)
        if (size <= 0 || guardMode == MonoGuardMode.OFF) {
            return Pair(left, right)
        }

        val corr = calculatePearsonCorrelation(left, right)
        val attenuationDb = calculateMonoGuardSideAttenuationDb(corr, guardMode)
        if (attenuationDb >= 0.0f) {
            return Pair(left, right)
        }

        val sideMultiplier = 10.0.pow(attenuationDb / 20.0).toFloat()
        val outL = FloatArray(size)
        val outR = FloatArray(size)

        for (i in 0 until size) {
            val mid = (left[i] + right[i]) * 0.5f
            val side = (left[i] - right[i]) * 0.5f
            val guardedSide = side * sideMultiplier
            outL[i] = (mid + guardedSide).coerceIn(-1.0f, 1.0f)
            outR[i] = (mid - guardedSide).coerceIn(-1.0f, 1.0f)
        }

        return Pair(outL, outR)
    }
}

