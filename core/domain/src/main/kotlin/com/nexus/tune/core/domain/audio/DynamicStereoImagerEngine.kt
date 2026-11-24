package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object DynamicStereoImagerEngine {

    class StereoImagerState(val sampleRate: Int = 48000) {
        // Highpass biquad state for mono-bass collapse on Side channel
        var hpX1: Float = 0.0f
        var hpX2: Float = 0.0f
        var hpY1: Float = 0.0f
        var hpY2: Float = 0.0f

        // Allpass biquad state for high-frequency spatial air decorrelation
        var apX1: Float = 0.0f
        var apX2: Float = 0.0f
        var apY1: Float = 0.0f
        var apY2: Float = 0.0f

        // Rolling correlation accumulators
        private var sumLR: Double = 0.0
        private var sumLL: Double = 0.0
        private var sumRR: Double = 0.0
        private var sampleCount: Int = 0

        var currentCorrelation: Float = 0.85f
        var monoGuardGain: Float = 1.0f

        fun updateCorrelation(l: Float, r: Float) {
            sumLR += l * r
            sumLL += l * l
            sumRR += r * r
            sampleCount++

            if (sampleCount >= 256) {
                val denom = sqrt(sumLL * sumRR)
                currentCorrelation = if (denom > 1e-6) {
                    (sumLR / denom).toFloat().coerceIn(-1.0f, 1.0f)
                } else {
                    1.0f
                }

                // Smoothly calculate mono guard damping factor
                monoGuardGain = if (currentCorrelation < 0.25f) {
                    ((currentCorrelation + 0.5f) / 0.75f).coerceIn(0.15f, 1.0f)
                } else {
                    1.0f
                }

                sumLR *= 0.5
                sumLL *= 0.5
                sumRR *= 0.5
                sampleCount = 128
            }
        }

        fun filterMonoBassHighpass(s: Float, cutoffHz: Float): Float {
            val clampedCutoff = cutoffHz.coerceIn(40.0f, 400.0f)
            val w0 = (2.0 * Math.PI * clampedCutoff / sampleRate).toFloat()
            val cosW = cos(w0)
            val sinW = sin(w0)
            val alpha = sinW / (2.0f * 0.7071f)

            val b0 = (1.0f + cosW) / 2.0f
            val b1 = -(1.0f + cosW)
            val b2 = (1.0f + cosW) / 2.0f
            val a0 = 1.0f + alpha
            val a1 = -2.0f * cosW
            val a2 = 1.0f - alpha

            val y = (b0 / a0) * s + (b1 / a0) * hpX1 + (b2 / a0) * hpX2 - (a1 / a0) * hpY1 - (a2 / a0) * hpY2
            hpX2 = hpX1
            hpX1 = s
            hpY2 = hpY1
            hpY1 = y
            return y
        }

        fun filterHighAirAllPass(s: Float): Float {
            // All-pass filter centered at 5.5 kHz providing micro-spatial phase decorrelation
            val w0 = (2.0 * Math.PI * 5500.0 / sampleRate).toFloat()
            val cosW = cos(w0)
            val sinW = sin(w0)
            val alpha = sinW / (2.0f * 0.65f)

            val a0 = 1.0f + alpha
            val a1 = -2.0f * cosW
            val a2 = 1.0f - alpha
            val b0 = a2
            val b1 = a1
            val b2 = a0

            val y = (b0 / a0) * s + (b1 / a0) * apX1 + (b2 / a0) * apX2 - (a1 / a0) * apY1 - (a2 / a0) * apY2
            apX2 = apX1
            apX1 = s
            apY2 = apY1
            apY1 = y
            return y
        }

        fun reset() {
            hpX1 = 0.0f; hpX2 = 0.0f; hpY1 = 0.0f; hpY2 = 0.0f
            apX1 = 0.0f; apX2 = 0.0f; apY1 = 0.0f; apY2 = 0.0f
            sumLR = 0.0; sumLL = 0.0; sumRR = 0.0; sampleCount = 0
            currentCorrelation = 0.85f
            monoGuardGain = 1.0f
        }
    }

    /**
     * Processes a discrete stereo sample pair (Left, Right) through the dynamic stereo imaging engine.
     */
    fun processStereoSample(
        left: Float,
        right: Float,
        config: DynamicStereoImagerConfig,
        state: StereoImagerState? = null
    ): Pair<Float, Float> {
        if (!config.isEnabled || config.preset == StereoImagerPreset.OFF) {
            return Pair(left, right)
        }

        state?.updateCorrelation(left, right)

        // Mid/Side Matrix Decomposition
        val invSqrt2 = 0.70710678f
        val mid = (left + right) * invSqrt2
        val sideRaw = (left - right) * invSqrt2

        // Apply Mono-Bass Highpass filter to Side channel (collapses low frequencies to pure mono)
        val sideBassFiltered = if (state != null && config.monoBassCutoffHz > 20.0f) {
            state.filterMonoBassHighpass(sideRaw, config.monoBassCutoffHz)
        } else {
            sideRaw
        }

        // Apply High-Band Air Decorrelation to Side channel
        val airSpreadNorm = config.highBandAirSpread.coerceIn(0.0f, 1.0f)
        val sideDecorrelated = if (state != null && airSpreadNorm > 0.05f) {
            val airComponent = state.filterHighAirAllPass(sideBassFiltered)
            sideBassFiltered + airComponent * (airSpreadNorm * 0.35f)
        } else {
            sideBassFiltered
        }

        // Apply Stereo Width & Intelligent Mono Guard Damping
        val widthNorm = config.stereoWidth.coerceIn(0.0f, 2.0f)
        val guardGain = if (config.monoGuardActive && state != null) state.monoGuardGain else 1.0f
        val sideProcessed = sideDecorrelated * widthNorm * guardGain

        // Mid/Side Matrix Reconstruction
        var outLeft = (mid + sideProcessed) * invSqrt2
        var outRight = (mid - sideProcessed) * invSqrt2

        // Analog soft saturation ceiling limiter to prevent inter-channel clipping
        if (abs(outLeft) > 0.98f) outLeft = tanh(outLeft)
        if (abs(outRight) > 0.98f) outRight = tanh(outRight)

        return Pair(outLeft.coerceIn(-1.0f, 1.0f), outRight.coerceIn(-1.0f, 1.0f))
    }

    /**
     * Processes an interleaved stereo buffer [L0, R0, L1, R1, ...]
     */
    fun processInterleavedBuffer(
        buffer: FloatArray,
        config: DynamicStereoImagerConfig
    ): FloatArray {
        if (!config.isEnabled || config.preset == StereoImagerPreset.OFF) {
            return buffer.copyOf()
        }
        val output = FloatArray(buffer.size)
        val state = StereoImagerState()
        var i = 0
        while (i < buffer.size - 1) {
            val (l, r) = processStereoSample(buffer[i], buffer[i + 1], config, state)
            output[i] = l
            output[i + 1] = r
            i += 2
        }
        return output
    }

    /**
     * Calculates hardware virtualizer strength (0 to 1000) corresponding to stereo width.
     */
    fun calculateHardwareSpatialOffset(config: DynamicStereoImagerConfig): Short {
        if (!config.isEnabled || config.preset == StereoImagerPreset.OFF) {
            return 0
        }
        val widthNorm = (config.stereoWidth - 1.0f).coerceAtLeast(0.0f) // 0.0 to 1.0
        val baseStrength = (widthNorm * 750.0f).roundToInt()
        return baseStrength.coerceIn(0, 1000).toShort()
    }

    /**
     * Synthesizes real-time metrics for UI presentation and diagnostics.
     */
    fun calculateMetrics(
        config: DynamicStereoImagerConfig,
        observedCorrelation: Float? = null
    ): StereoImagerMetrics {
        if (!config.isEnabled || config.preset == StereoImagerPreset.OFF) {
            return StereoImagerMetrics(
                correlationCoefficient = 0.95f,
                perceivedWidthPercent = 100,
                monoBassCutoffHz = 0.0f,
                monoCompatibilityScore = 100,
                spatialDepthRating = "Original Stereo Image • Unmodified Phase (Bypassed)"
            )
        }

        val width = config.stereoWidth.coerceIn(0.0f, 2.0f)
        val widthPercent = (width * 100.0f).roundToInt()
        val estimatedCorrelation = observedCorrelation ?: when {
            width <= 0.2f -> 0.99f
            width <= 1.0f -> 0.90f - (width * 0.10f)
            width <= 1.5f -> 0.80f - ((width - 1.0f) * 0.35f)
            else -> 0.62f - ((width - 1.5f) * 0.40f)
        }.coerceIn(-1.0f, 1.0f)

        val compatibilityScore = when {
            estimatedCorrelation >= 0.80f -> 100
            estimatedCorrelation >= 0.50f -> 85
            estimatedCorrelation >= 0.25f -> 70
            estimatedCorrelation >= 0.0f -> 45
            else -> 20
        }

        val rating = when {
            width == 0.0f -> "Summed Pure Mono • 100% Phase Coherence"
            width > 1.5f -> "Ultra-Wide Spatial Sphere • Binaural Out-of-Head Air"
            width > 1.2f -> "Expanded Concert Stage • Deep Side Ambience Bloom"
            width < 0.8f -> "Focused Center Stage • Compact Intimate Field"
            else -> "Natural Stereo Dimension • Solid Mono-Bass Foundation"
        }

        return StereoImagerMetrics(
            correlationCoefficient = (round(estimatedCorrelation * 100.0f) / 100.0f),
            perceivedWidthPercent = widthPercent,
            monoBassCutoffHz = config.monoBassCutoffHz,
            monoCompatibilityScore = compatibilityScore,
            spatialDepthRating = rating
        )
    }
}

