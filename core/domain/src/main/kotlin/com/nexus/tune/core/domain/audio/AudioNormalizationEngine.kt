package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.LoudnessConfig
import com.nexus.tune.core.model.LoudnessNormalizationMode
import com.nexus.tune.core.model.Track
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.tanh

object AudioNormalizationEngine {

    const val DEFAULT_TARGET_LUFS = -14.0f
    const val MIN_TARGET_LUFS = -23.0f
    const val MAX_TARGET_LUFS = -9.0f

    const val MIN_PREAMP_DB = -6.0f
    const val MAX_PREAMP_DB = 6.0f

    // Safe gain clamping bounds: between -20dB (0.1x) and +6dB (2.0x)
    const val MIN_GAIN_MULTIPLIER = 0.1f
    const val MAX_GAIN_MULTIPLIER = 2.0f

    /**
     * Calculates the linear playback volume multiplier to achieve target loudness.
     * gainDb = (targetLufs - trackLufs) + preAmpGainDb
     * linearMultiplier = 10^(gainDb / 20.0)
     */
    fun calculateGainMultiplier(
        trackLufs: Float?,
        config: LoudnessConfig
    ): Float {
        if (config.mode == LoudnessNormalizationMode.OFF) {
            return 1.0f
        }

        if (config.mode == LoudnessNormalizationMode.DYNAMIC_PEAK_LIMITER) {
            // When only peak limiter is active, apply pre-amp if any with 1.0 baseline
            val preAmpMultiplier = 10.0.pow((config.preAmpGainDb.coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB) / 20.0)).toFloat()
            return preAmpMultiplier.coerceIn(MIN_GAIN_MULTIPLIER, MAX_GAIN_MULTIPLIER)
        }

        val effectiveTrackLufs = trackLufs ?: -14.0f
        val clampedTarget = config.targetLufs.coerceIn(MIN_TARGET_LUFS, MAX_TARGET_LUFS)
        val clampedPreamp = config.preAmpGainDb.coerceIn(MIN_PREAMP_DB, MAX_PREAMP_DB)

        val deltaDb = (clampedTarget - effectiveTrackLufs) + clampedPreamp
        val linearMultiplier = 10.0.pow((deltaDb / 20.0)).toFloat()

        return linearMultiplier.coerceIn(MIN_GAIN_MULTIPLIER, MAX_GAIN_MULTIPLIER)
    }

    /**
     * Soft-knee peak limiting compression curve.
     * Prevents harsh digital clipping when volume multiplier pushes peaks near or above 1.0.
     * Samples below [threshold] pass untouched (unity gain).
     * Samples above [threshold] smoothly saturate towards 1.0 using tanh().
     */
    fun applySoftLimiter(sample: Float, threshold: Float = 0.95f): Float {
        val sign = if (sample < 0f) -1f else 1f
        val absVal = abs(sample)

        if (absVal <= threshold) {
            return sample
        }

        val kneeRange = (1.0f - threshold).coerceAtLeast(0.001f)
        val excess = (absVal - threshold) / kneeRange
        val saturated = threshold + kneeRange * tanh(excess)
        return sign * saturated.coerceAtMost(1.0f)
    }

    /**
     * Estimates baseline LUFS from track metadata heuristics when loudness tags are missing.
     * Longer studio tracks, podcasts, and acoustic songs typically have wider dynamic ranges.
     */
    fun estimateTrackLufs(track: Track): Float {
        val titleLower = track.title.lowercase()
        val artistLower = track.artist.lowercase()

        return when {
            titleLower.contains("acoustic") || titleLower.contains("unplugged") || titleLower.contains("piano") -> -18.0f
            titleLower.contains("remix") || titleLower.contains("club") || titleLower.contains("bass boosted") || titleLower.contains("dubstep") -> -9.5f
            titleLower.contains("live") || titleLower.contains("concert") -> -16.0f
            titleLower.contains("lofi") || titleLower.contains("chill") || titleLower.contains("ambient") -> -17.0f
            artistLower.contains("orchestra") || artistLower.contains("symphony") -> -20.0f
            track.durationSeconds > 600 -> -19.0f // Long mixes/podcasts/classical
            else -> -14.0f // Standard modern streaming master
        }
    }
}

