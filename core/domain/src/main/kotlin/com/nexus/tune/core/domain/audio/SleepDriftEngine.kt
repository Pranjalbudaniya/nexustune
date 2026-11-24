package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.SleepDriftProfile
import kotlin.math.roundToInt

object SleepDriftEngine {

    /**
     * Calculates the normalized progress (0.0 .. 1.0) of a sleep timer session.
     */
    fun calculateDriftProgress(elapsedSeconds: Long, totalSeconds: Long): Float {
        if (totalSeconds <= 0L) return 0f
        return (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Returns 5-band millibel EQ offsets [60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz]
     * progressively rolling off high frequencies as drift progress advances.
     */
    fun calculateEqRollOffBands(profile: SleepDriftProfile, driftProgress: Float): List<Int> {
        val progress = driftProgress.coerceIn(0f, 1f)
        if (profile == SleepDriftProfile.OFF || progress <= 0f) {
            return listOf(0, 0, 0, 0, 0)
        }

        return when (profile) {
            SleepDriftProfile.OFF -> listOf(0, 0, 0, 0, 0)
            SleepDriftProfile.WARM_MELLOW -> listOf(
                0,
                0,
                0,
                (-400 * progress).roundToInt(),
                (-800 * progress).roundToInt()
            )
            SleepDriftProfile.DEEP_DRIFT -> listOf(
                0,
                0,
                (-200 * progress).roundToInt(),
                (-600 * progress).roundToInt(),
                (-1200 * progress).roundToInt()
            )
            SleepDriftProfile.BINAURAL_THETA -> listOf(
                (50 * progress).roundToInt(),
                0,
                0,
                (-450 * progress).roundToInt(),
                (-700 * progress).roundToInt()
            )
            SleepDriftProfile.BINAURAL_DELTA -> listOf(
                (100 * progress).roundToInt(),
                (50 * progress).roundToInt(),
                0,
                (-550 * progress).roundToInt(),
                (-900 * progress).roundToInt()
            )
        }
    }

    /**
     * Calculates gradual tempo deceleration multiplier (e.g. 1.0 down to 0.90) for calming respiration.
     */
    fun calculateDriftSpeed(profile: SleepDriftProfile, driftProgress: Float): Float {
        val progress = driftProgress.coerceIn(0f, 1f)
        return when (profile) {
            SleepDriftProfile.DEEP_DRIFT, SleepDriftProfile.BINAURAL_DELTA -> {
                if (progress > 0.5f) {
                    // Decelerate from 1.0f at 50% to 0.90f at 100%
                    1.0f - ((progress - 0.5f) * 0.20f)
                } else {
                    1.0f
                }
            }
            else -> 1.0f
        }
    }

    /**
     * Returns Left and Right carrier frequencies in Hz for binaural entrainment.
     */
    fun calculateBinauralFrequencies(profile: SleepDriftProfile): Pair<Float, Float>? {
        return when (profile) {
            SleepDriftProfile.BINAURAL_THETA -> Pair(144.0f, 150.0f) // 6Hz Theta (Relaxation & REM)
            SleepDriftProfile.BINAURAL_DELTA -> Pair(147.0f, 150.0f) // 3Hz Delta (Deep restorative sleep)
            else -> null
        }
    }
}

