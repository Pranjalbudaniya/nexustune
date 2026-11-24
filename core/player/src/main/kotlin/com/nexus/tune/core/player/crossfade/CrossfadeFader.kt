package com.nexus.tune.core.player.crossfade

import com.nexus.tune.core.model.CrossfadeCurve
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class CrossfadeVolumes(
    val outgoingVolume: Float,
    val incomingVolume: Float
)

class CrossfadeFader {

    /**
     * Calculates instant outgoing and incoming volume levels based on elapsed time,
     * total crossfade duration, and the specified acoustic curve.
     *
     * @param elapsedMs Elapsed milliseconds into the crossfade window [0 .. durationMs].
     * @param durationMs Total crossfade duration in milliseconds (> 0).
     * @param curve The acoustic attenuation curve.
     * @return [CrossfadeVolumes] containing normalized volume levels in [0.0f .. 1.0f].
     */
    fun calculateAttenuation(
        elapsedMs: Long,
        durationMs: Long,
        curve: CrossfadeCurve = CrossfadeCurve.EQUAL_POWER
    ): CrossfadeVolumes {
        if (durationMs <= 0L) {
            return CrossfadeVolumes(outgoingVolume = 0.0f, incomingVolume = 1.0f)
        }

        val progress = (elapsedMs.toFloat() / durationMs.toFloat()).coerceIn(0.0f, 1.0f)

        return when (curve) {
            CrossfadeCurve.EQUAL_POWER -> {
                // Quarter-sine and quarter-cosine curve:
                // cos^2(theta) + sin^2(theta) = 1.0, preserving acoustic power across transition
                val theta = progress * (PI.toFloat() / 2.0f)
                val outVol = cos(theta).coerceIn(0.0f, 1.0f)
                val inVol = sin(theta).coerceIn(0.0f, 1.0f)
                CrossfadeVolumes(outVol, inVol)
            }
            CrossfadeCurve.LINEAR -> {
                val outVol = (1.0f - progress).coerceIn(0.0f, 1.0f)
                val inVol = progress.coerceIn(0.0f, 1.0f)
                CrossfadeVolumes(outVol, inVol)
            }
            CrossfadeCurve.EXPONENTIAL -> {
                val outProgress = 1.0f - progress
                val outVol = (outProgress * outProgress).coerceIn(0.0f, 1.0f)
                val inVol = (progress * progress).coerceIn(0.0f, 1.0f)
                CrossfadeVolumes(outVol, inVol)
            }
        }
    }
}
