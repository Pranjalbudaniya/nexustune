package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DecorrelatorMode
import com.nexus.tune.core.model.DecorrelatorPreset
import com.nexus.tune.core.model.DecorrelatorStage
import com.nexus.tune.core.model.SpatialDecorrelatorConfig
import com.nexus.tune.core.model.SpatialDecorrelatorMetrics
import kotlin.math.*

/**
 * Audiophile Dynamic Mid-Side Spatial Phase Decorrelator & Ambience Diffuser Studio.
 *
 * Implements mid/side decomposition, frequency-dependent all-pass phase decorrelation,
 * and mono-compatibility safeguarding for an expansive 3D holographic soundstage.
 */
class SpatialDecorrelatorEngine {

    /**
     * Process stereo interleaved audio buffer [L0, R0, L1, R1, ...] with spatial decorrelation.
     */
    fun processStereo(
        samples: FloatArray,
        config: SpatialDecorrelatorConfig,
        sampleRate: Int = 44100
    ): FloatArray {
        if (!config.isEnabled || config.preset == DecorrelatorPreset.OFF || samples.isEmpty()) {
            return samples.copyOf()
        }

        val frameCount = samples.size / 2
        val output = FloatArray(samples.size)

        val delaySamples = max(1, (config.stage.delayMs * sampleRate / 1000.0f).roundToInt())
        val delaySamples2 = max(1, (delaySamples * 1.4142f).roundToInt())

        val buffer1 = FloatArray(delaySamples + 1)
        val buffer2 = FloatArray(delaySamples2 + 1)
        var ptr1 = 0
        var ptr2 = 0

        val diffusionGain = (config.diffusionIntensityPercent.coerceIn(0, 100) / 100.0f) * 0.65f
        val width = config.stereoWidthMultiplier.coerceIn(0.5f, 2.5f)

        // Mid/Side processing
        val invSqrt2 = 0.70710678f

        for (i in 0 until frameCount) {
            val left = samples[i * 2]
            val right = samples[i * 2 + 1]

            // Mid & Side extraction
            val mid = (left + right) * invSqrt2
            var side = (left - right) * invSqrt2

            // Decorrelate Side channel
            val decorrelatedSide: Float = when (config.mode) {
                DecorrelatorMode.MID_SIDE_PHASE_SPLIT -> {
                    side * width
                }
                DecorrelatorMode.ALLPASS_CASCADE_DIFFUSE -> {
                    // Schroeder all-pass stage 1: y[n] = -g*x[n] + x[n-D] + g*y[n-D]
                    val delayed1 = buffer1[ptr1]
                    val ap1 = -diffusionGain * side + delayed1
                    buffer1[ptr1] = side + diffusionGain * ap1
                    ptr1 = (ptr1 + 1) % delaySamples

                    // Schroeder all-pass stage 2
                    val delayed2 = buffer2[ptr2]
                    val ap2 = -diffusionGain * ap1 + delayed2
                    buffer2[ptr2] = ap1 + diffusionGain * ap2
                    ptr2 = (ptr2 + 1) % delaySamples2

                    (side * (1.0f - diffusionGain * 0.5f) + ap2 * diffusionGain) * width
                }
                DecorrelatorMode.DYNAMIC_AMBIENCE_SPREAD -> {
                    val delayed1 = buffer1[ptr1]
                    val ap1 = -diffusionGain * side + delayed1
                    buffer1[ptr1] = side + diffusionGain * ap1
                    ptr1 = (ptr1 + 1) % delaySamples

                    val diffuseSide = ap1 * width * 1.25f
                    diffuseSide
                }
                DecorrelatorMode.SOLO_DECORRELATED_SIDE -> {
                    val delayed1 = buffer1[ptr1]
                    val ap1 = -diffusionGain * side + delayed1
                    buffer1[ptr1] = side + diffusionGain * ap1
                    ptr1 = (ptr1 + 1) % delaySamples
                    ap1 * width
                }
            }

            if (config.soloDecorrelatedSide || config.mode == DecorrelatorMode.SOLO_DECORRELATED_SIDE) {
                output[i * 2] = decorrelatedSide.coerceIn(-1.0f, 1.0f)
                output[i * 2 + 1] = (-decorrelatedSide).coerceIn(-1.0f, 1.0f)
            } else {
                // Reconstruct Left / Right
                var outL = (mid + decorrelatedSide) * invSqrt2
                var outR = (mid - decorrelatedSide) * invSqrt2

                if (config.monoCompatibilityGuard) {
                    // Subtle central crossfeed to guarantee mono summing never cancels
                    val guardCrossfeed = 0.08f
                    val guardedL = outL * (1.0f - guardCrossfeed) + outR * guardCrossfeed
                    val guardedR = outR * (1.0f - guardCrossfeed) + outL * guardCrossfeed
                    outL = guardedL
                    outR = guardedR
                }

                output[i * 2] = outL.coerceIn(-1.0f, 1.0f)
                output[i * 2 + 1] = outR.coerceIn(-1.0f, 1.0f)
            }
        }

        return output
    }

    /**
     * Estimate real-time spatial correlation coefficient and stereo phase spread angle.
     */
    fun estimateMetrics(
        samples: FloatArray,
        config: SpatialDecorrelatorConfig
    ): SpatialDecorrelatorMetrics {
        if (!config.isEnabled || config.preset == DecorrelatorPreset.OFF || samples.size < 4) {
            return SpatialDecorrelatorMetrics(
                correlationCoefficient = 1.0f,
                phaseSpreadAngleDegrees = 0.0f,
                soundstageWidthRating = "BYPASS",
                isMonoSafe = true
            )
        }

        val frameCount = samples.size / 2
        var sumLR = 0.0
        var sumL2 = 0.0
        var sumR2 = 0.0

        val step = max(1, frameCount / 256)
        var i = 0
        while (i < frameCount) {
            val l = samples[i * 2].toDouble()
            val r = samples[i * 2 + 1].toDouble()
            sumLR += l * r
            sumL2 += l * l
            sumR2 += r * r
            i += step
        }

        val denom = sqrt(sumL2 * sumR2)
        val correlation = if (denom > 1e-6) {
            (sumLR / denom).coerceIn(-1.0, 1.0).toFloat()
        } else {
            1.0f
        }

        val phaseAngleRad = acos(correlation.coerceIn(-1.0f, 1.0f))
        val phaseAngleDeg = (phaseAngleRad * 180.0f / PI.toFloat())

        val rating = when {
            correlation < 0.2f -> "HOLOGRAPHIC 3D"
            correlation < 0.6f -> "WIDE IMMERSIVE"
            correlation < 0.85f -> "FOCUSED STEREO"
            else -> "CENTERED MONO"
        }

        val isMonoSafe = correlation >= 0.10f || config.monoCompatibilityGuard

        return SpatialDecorrelatorMetrics(
            correlationCoefficient = (correlation * 100).roundToInt() / 100.0f,
            phaseSpreadAngleDegrees = (phaseAngleDeg * 10).roundToInt() / 10.0f,
            soundstageWidthRating = rating,
            isMonoSafe = isMonoSafe
        )
    }

    /**
     * Map presets to full studio configuration.
     */
    fun presetToConfig(preset: DecorrelatorPreset): SpatialDecorrelatorConfig {
        return when (preset) {
            DecorrelatorPreset.OFF -> SpatialDecorrelatorConfig(
                isEnabled = false,
                preset = DecorrelatorPreset.OFF
            )
            DecorrelatorPreset.HEADPHONE_SOUNDSTAGE_EXPAND -> SpatialDecorrelatorConfig(
                isEnabled = true,
                preset = DecorrelatorPreset.HEADPHONE_SOUNDSTAGE_EXPAND,
                stage = DecorrelatorStage.BINAURAL_HEADPHONE_EXPANDER,
                mode = DecorrelatorMode.ALLPASS_CASCADE_DIFFUSE,
                diffusionIntensityPercent = 65,
                stereoWidthMultiplier = 1.40f,
                monoCompatibilityGuard = true
            )
            DecorrelatorPreset.STUDIO_NEARFIELD_UNCLUTTER -> SpatialDecorrelatorConfig(
                isEnabled = true,
                preset = DecorrelatorPreset.STUDIO_NEARFIELD_UNCLUTTER,
                stage = DecorrelatorStage.SUBTLE_HAAS_MICRO,
                mode = DecorrelatorMode.MID_SIDE_PHASE_SPLIT,
                diffusionIntensityPercent = 35,
                stereoWidthMultiplier = 1.15f,
                monoCompatibilityGuard = true
            )
            DecorrelatorPreset.ORCHESTRAL_HALL_AIR -> SpatialDecorrelatorConfig(
                isEnabled = true,
                preset = DecorrelatorPreset.ORCHESTRAL_HALL_AIR,
                stage = DecorrelatorStage.DIFFUSE_CONCERT_DEPTH,
                mode = DecorrelatorMode.DYNAMIC_AMBIENCE_SPREAD,
                diffusionIntensityPercent = 75,
                stereoWidthMultiplier = 1.50f,
                monoCompatibilityGuard = true
            )
            DecorrelatorPreset.VINYL_STEREO_RECENTER -> SpatialDecorrelatorConfig(
                isEnabled = true,
                preset = DecorrelatorPreset.VINYL_STEREO_RECENTER,
                stage = DecorrelatorStage.SUBTLE_HAAS_MICRO,
                mode = DecorrelatorMode.MID_SIDE_PHASE_SPLIT,
                diffusionIntensityPercent = 20,
                stereoWidthMultiplier = 0.95f,
                monoCompatibilityGuard = true
            )
        }
    }
}
