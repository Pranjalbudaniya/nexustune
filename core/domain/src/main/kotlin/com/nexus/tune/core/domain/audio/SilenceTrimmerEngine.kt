package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object SilenceTrimmerEngine {

    /**
     * Calculates the Root-Mean-Square (RMS) amplitude in dBFS for an audio buffer.
     * Returns values bounded in [-96.0 dBFS, 0.0 dBFS].
     */
    fun calculateRmsDb(samples: FloatArray): Float {
        if (samples.isEmpty()) return -96.0f
        var sumSquares = 0.0
        for (sample in samples) {
            val s = sample.toDouble()
            sumSquares += s * s
        }
        val meanSquare = sumSquares / samples.size.toDouble()
        val rms = sqrt(meanSquare)
        if (rms <= 0.0000158) return -96.0f // < -96 dBFS threshold
        return (20.0 * log10(rms)).toFloat().coerceIn(-96.0f, 0.0f)
    }

    /**
     * Evaluates silence detection for a frame and determines whether to trim/skip the audio segment.
     */
    fun detectSilence(
        rmsDb: Float,
        currentSilenceMs: Long,
        frameDurationMs: Long,
        config: SilenceTrimConfig
    ): SilenceDetectionResult {
        if (!config.isEnabled || config.mode == SilenceTrimMode.OFF) {
            return SilenceDetectionResult(
                isSilent = false,
                rmsDb = rmsDb,
                silenceDurationMs = 0L,
                shouldSkip = false
            )
        }

        val effectiveThreshold = if (config.thresholdDb != -45.0f) config.thresholdDb else config.mode.defaultThresholdDb
        val effectiveMinDuration = if (config.minDurationMs > 0) config.minDurationMs else config.mode.defaultMinDurationMs

        val isSilent = rmsDb < effectiveThreshold
        val newDurationMs = if (isSilent) currentSilenceMs + frameDurationMs else 0L
        val shouldSkip = isSilent && (newDurationMs >= effectiveMinDuration)

        return SilenceDetectionResult(
            isSilent = isSilent,
            rmsDb = rmsDb,
            silenceDurationMs = newDurationMs,
            shouldSkip = shouldSkip
        )
    }

    /**
     * Applies cosine-squared micro-envelope smoothing to eliminate audible click/pop transients
     * at cut boundaries.
     */
    fun applyEnvelopeSmoothing(
        samples: FloatArray,
        isFadeIn: Boolean,
        rampLength: Int = 64
    ): FloatArray {
        val output = samples.clone()
        val n = output.size
        val effectiveRamp = rampLength.coerceIn(1, n)

        for (k in 0 until effectiveRamp) {
            val progress = k.toFloat() / effectiveRamp.toFloat()
            // Cosine-squared window w = sin(pi * progress / 2)^2
            val angle = progress * (Math.PI.toFloat() / 2f)
            val window = sin(angle) * sin(angle)

            if (isFadeIn) {
                output[k] *= window
            } else {
                val idx = n - 1 - k
                output[idx] *= window
            }
        }
        return output
    }

    /**
     * Computes the percentage of audio playback time saved by skipping silences.
     */
    fun estimateTimeSavingsPercent(silentDurationMs: Long, totalDurationMs: Long): Float {
        if (totalDurationMs <= 0L) return 0f
        return ((silentDurationMs.toFloat() / totalDurationMs.toFloat()) * 100f).coerceIn(0f, 90f)
    }

    /**
     * Dynamically adapts the silence threshold based on measured background floor noise.
     */
    fun calculateDynamicThresholdDb(backgroundFloorDb: Float, mode: SilenceTrimMode): Float {
        return when (mode) {
            SilenceTrimMode.SMART_DYNAMIC -> (backgroundFloorDb + 6.0f).coerceIn(-60f, -30f)
            else -> mode.defaultThresholdDb
        }
    }
}

