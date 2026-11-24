package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.HeadphoneCorrectionConfig
import com.nexus.tune.core.model.HeadphoneTargetCurve

object HeadphoneCorrectionEngine {

    /**
     * Calculates 5-band millibel (mB) offsets [60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz]
     * mapped to each acoustic target curve.
     */
    fun calculateTargetEqOffsets(curve: HeadphoneTargetCurve): List<Int> {
        return when (curve) {
            HeadphoneTargetCurve.HARMAN_OVER_EAR -> listOf(450, 150, 0, 300, 0)
            HeadphoneTargetCurve.HARMAN_IN_EAR -> listOf(600, 200, 0, 400, 100)
            HeadphoneTargetCurve.DIFFUSE_FIELD -> listOf(-100, -100, 100, 600, 200)
            HeadphoneTargetCurve.STUDIO_NEUTRAL -> listOf(0, 0, 0, 0, 0)
            HeadphoneTargetCurve.WARM_PUNCH -> listOf(300, 450, 150, -100, -100)
            HeadphoneTargetCurve.V_SHAPED_BASSHEAD -> listOf(700, 400, -200, 300, 500)
            HeadphoneTargetCurve.TREBLE_AIR -> listOf(0, 0, 100, 400, 750)
        }
    }

    /**
     * Returns the recommended BassBoost strength (0..1000) for hardware acoustic driver compensation.
     */
    fun calculateBassBoostStrength(curve: HeadphoneTargetCurve): Int {
        return when (curve) {
            HeadphoneTargetCurve.V_SHAPED_BASSHEAD -> 700
            HeadphoneTargetCurve.HARMAN_IN_EAR -> 450
            HeadphoneTargetCurve.WARM_PUNCH -> 400
            HeadphoneTargetCurve.HARMAN_OVER_EAR -> 350
            else -> 0
        }
    }

    /**
     * Calculates simulated interaural time difference (ITD) in milliseconds for cross-feed naturalization.
     */
    fun calculateCrossfeedDelayMs(levelPercent: Int): Float {
        val clamped = levelPercent.coerceIn(0, 100)
        // Standard human head ITD is ~0.3ms to 0.4ms
        return 0.25f + (clamped / 100f) * 0.15f
    }

    /**
     * Calculates crossfeed interaural level attenuation in dB.
     */
    fun calculateCrossfeedAttenuationDb(levelPercent: Int): Float {
        val clamped = levelPercent.coerceIn(0, 100)
        // Higher crossfeed level = less attenuation on the opposite channel (more blend)
        return -14.0f + (clamped / 100f) * 8.0f // -14dB to -6dB
    }

    fun isConfigActive(config: HeadphoneCorrectionConfig): Boolean {
        return config.isEnabled
    }

    fun formatTargetDescription(curve: HeadphoneTargetCurve): String {
        return "${curve.displayName} • ${curve.tonalCharacter}"
    }
}

