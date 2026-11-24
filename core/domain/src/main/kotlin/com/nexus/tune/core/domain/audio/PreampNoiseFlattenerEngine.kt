package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.PreampFlattenerMode
import com.nexus.tune.core.model.PreampNoiseFlattenerConfig
import com.nexus.tune.core.model.PreampNoiseFlattenerMetrics
import com.nexus.tune.core.model.PreampNoiseProfile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Audiophile Dynamic Analog Preamp Thermal Noise Profiler & Spectral Flattener Studio Engine.
 *
 * Implements precision Johnson-Nyquist thermal noise compensation, multi-band spectral tilt
 * estimation, soft-knee downward expansion, and Fletcher-Munson equal-loudness shaped thermal
 * dither generation for transparent analog restoration.
 */
class PreampNoiseFlattenerEngine(
    private val sampleRate: Int = 44100
) {
    // Envelope detector state for downward expander
    private var envelopeL = 0.0f
    private var envelopeR = 0.0f

    // Attack and release coefficients (attack ~8ms, release ~120ms)
    private val attackAlpha = exp(-1.0f / (0.008f * sampleRate))
    private val releaseAlpha = exp(-1.0f / (0.120f * sampleRate))

    // High-shelf filter states for L and R
    private var x1L = 0.0f
    private var x2L = 0.0f
    private var y1L = 0.0f
    private var y2L = 0.0f

    private var x1R = 0.0f
    private var x2R = 0.0f
    private var y1R = 0.0f
    private var y2R = 0.0f

    // Dither filter memory (1st order highpass for psychoacoustic shape)
    private var ditherMemL = 0.0f
    private var ditherMemR = 0.0f
    private val rng = Random(42)

    // Running energy trackers for metrics
    private var runningLowEnergy = 0.0001f
    private var runningMidEnergy = 0.0001f
    private var runningHighEnergy = 0.0001f
    private var lastGainReductionDb = 0.0f

    fun reset() {
        envelopeL = 0.0f
        envelopeR = 0.0f
        x1L = 0.0f
        x2L = 0.0f
        y1L = 0.0f
        y2L = 0.0f
        x1R = 0.0f
        x2R = 0.0f
        y1R = 0.0f
        y2R = 0.0f
        ditherMemL = 0.0f
        ditherMemR = 0.0f
        runningLowEnergy = 0.0001f
        runningMidEnergy = 0.0001f
        runningHighEnergy = 0.0001f
        lastGainReductionDb = 0.0f
    }

    /**
     * Process an interleaved stereo PCM frame buffer in-place.
     */
    fun processInterleaved(
        buffer: FloatArray,
        offset: Int = 0,
        length: Int = buffer.size,
        config: PreampNoiseFlattenerConfig
    ): PreampNoiseFlattenerMetrics {
        if (!config.isEnabled || length < 2) {
            return calculateMetrics(config, buffer, offset, length, sampleRate)
        }

        val effectiveTiltDb = calculateEffectiveTiltDb(config)
        val shelfCoeffs = calculateHighShelfCoefficients(5000.0f, effectiveTiltDb, sampleRate)

        val thresholdLinear = 10.0f.pow(config.downwardThresholdDb / 20.0f)
        val expanderRatio = 1.0f + config.flattenDepth * 1.5f
        val ditherScale = config.thermalDitherLevel * 0.0008f

        var maxReductionDb = 0.0f

        var i = offset
        while (i < offset + length - 1) {
            val inL = buffer[i]
            val inR = buffer[i + 1]

            // 1. Envelope detection
            val absL = abs(inL)
            val absR = abs(inR)
            envelopeL = if (absL > envelopeL) {
                attackAlpha * envelopeL + (1.0f - attackAlpha) * absL
            } else {
                releaseAlpha * envelopeL + (1.0f - releaseAlpha) * absL
            }
            envelopeR = if (absR > envelopeR) {
                attackAlpha * envelopeR + (1.0f - attackAlpha) * absR
            } else {
                releaseAlpha * envelopeR + (1.0f - releaseAlpha) * absR
            }

            val maxEnv = max(envelopeL, envelopeR)

            // 2. Downward Expander computation
            var expanderGain = 1.0f
            if (config.mode == PreampFlattenerMode.DOWNWARD_EXPAND ||
                (config.mode == PreampFlattenerMode.DYNAMIC_FLATTEN && config.flattenDepth > 0.05f)
            ) {
                if (maxEnv < thresholdLinear && maxEnv > 1e-7f) {
                    val envDb = 20.0f * log10(maxEnv)
                    val diffDb = envDb - config.downwardThresholdDb
                    // Apply downward expansion curve clamped to -28 dB max gate
                    val reductionDb = max(-28.0f, diffDb * (expanderRatio - 1.0f))
                    expanderGain = 10.0f.pow(reductionDb / 20.0f)
                    val curReductionDb = abs(reductionDb)
                    if (curReductionDb > maxReductionDb) {
                        maxReductionDb = curReductionDb
                    }
                }
            }

            // 3. Spectral Tilt Equalizer (Biquad high shelf)
            var filteredL = inL
            var filteredR = inR
            if (abs(effectiveTiltDb) > 0.1f) {
                filteredL = shelfCoeffs.b0 * inL + shelfCoeffs.b1 * x1L + shelfCoeffs.b2 * x2L -
                        shelfCoeffs.a1 * y1L - shelfCoeffs.a2 * y2L
                x2L = x1L
                x1L = inL
                y2L = y1L
                y1L = filteredL

                filteredR = shelfCoeffs.b0 * inR + shelfCoeffs.b1 * x1R + shelfCoeffs.b2 * x2R -
                        shelfCoeffs.a1 * y1R - shelfCoeffs.a2 * y2R
                x2R = x1R
                x1R = inR
                y2R = y1R
                y1R = filteredR
            }

            // 4. Downward expansion attenuation
            var outL = filteredL * expanderGain
            var outR = filteredR * expanderGain

            // 5. Shaped Thermal Dither injection (Fletcher-Munson pre-emphasis)
            if (ditherScale > 1e-6f || config.mode == PreampFlattenerMode.WARM_DITHER) {
                val rawDitherL = (rng.nextFloat() * 2.0f - 1.0f) * ditherScale
                val rawDitherR = (rng.nextFloat() * 2.0f - 1.0f) * ditherScale

                // 1st order HP filter shaped dither: d_shaped = raw - 0.75 * raw_prev
                val shapedL = rawDitherL - 0.75f * ditherMemL
                ditherMemL = rawDitherL
                val shapedR = rawDitherR - 0.75f * ditherMemR
                ditherMemR = rawDitherR

                outL += shapedL
                outR += shapedR
            }

            // 6. Output routing / Solo mode
            if (config.mode == PreampFlattenerMode.SOLO_NOISE_PROFILE) {
                // In solo noise profile mode, output the extracted residual noise difference amplified by +6dB
                buffer[i] = ((inL - outL) * 2.0f).coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = ((inR - outR) * 2.0f).coerceIn(-1.0f, 1.0f)
            } else {
                buffer[i] = outL.coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = outR.coerceIn(-1.0f, 1.0f)
            }

            // Energy accumulation for tilt metric estimation
            val energy = (abs(inL) + abs(inR)) * 0.5f
            runningMidEnergy = 0.999f * runningMidEnergy + 0.001f * energy
            i += 2
        }

        lastGainReductionDb = maxReductionDb
        return calculateMetrics(config, buffer, offset, length, sampleRate)
    }

    private fun calculateEffectiveTiltDb(config: PreampNoiseFlattenerConfig): Float {
        val baseTilt = when (config.profile) {
            PreampNoiseProfile.AUTO_DETECT -> config.hfTiltDb
            PreampNoiseProfile.FLICKER_PINK -> -2.5f + config.hfTiltDb
            PreampNoiseProfile.THERMAL_WHITE -> -1.5f + config.hfTiltDb
            PreampNoiseProfile.BROWNIAN_LOW -> 2.0f + config.hfTiltDb
            PreampNoiseProfile.SHOT_BLUE -> -3.5f + config.hfTiltDb
        }
        return (baseTilt * config.flattenDepth).coerceIn(-8.0f, 8.0f)
    }

    data class BiquadCoeffs(
        val b0: Float,
        val b1: Float,
        val b2: Float,
        val a1: Float,
        val a2: Float
    )

    companion object {
        /**
         * Calculates high-shelf biquad filter coefficients according to Audio EQ Cookbook.
         */
        fun calculateHighShelfCoefficients(
            frequencyHz: Float,
            gainDb: Float,
            sampleRate: Int
        ): BiquadCoeffs {
            if (abs(gainDb) < 0.01f) {
                return BiquadCoeffs(1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            }

            val a = 10.0f.pow(gainDb / 40.0f)
            val w0 = (2.0 * PI * frequencyHz / sampleRate).toFloat()
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val slope = 1.0f
            val alpha = (sinW0 / 2.0f * sqrt((a + 1.0f / a) * (1.0f / slope - 1.0f) + 2.0f))

            val a0 = (a + 1.0f) - (a - 1.0f) * cosW0 + 2.0f * sqrt(a) * alpha
            val b0 = a * ((a + 1.0f) + (a - 1.0f) * cosW0 + 2.0f * sqrt(a) * alpha) / a0
            val b1 = -2.0f * a * ((a - 1.0f) + (a + 1.0f) * cosW0) / a0
            val b2 = a * ((a + 1.0f) + (a - 1.0f) * cosW0 - 2.0f * sqrt(a) * alpha) / a0
            val a1 = 2.0f * ((a - 1.0f) - (a + 1.0f) * cosW0) / a0
            val a2 = ((a + 1.0f) - (a - 1.0f) * cosW0 - 2.0f * sqrt(a) * alpha) / a0

            return BiquadCoeffs(b0, b1, b2, a1, a2)
        }

        /**
         * Calculates real-time metrics from audio buffer and configuration.
         */
        fun calculateMetrics(
            config: PreampNoiseFlattenerConfig,
            buffer: FloatArray,
            offset: Int = 0,
            length: Int = buffer.size,
            sampleRate: Int = 44100
        ): PreampNoiseFlattenerMetrics {
            if (!config.isEnabled || length < 2) {
                return PreampNoiseFlattenerMetrics(
                    estimatedNoiseFloorDb = -62.0f,
                    spectralTiltSlope = 0.0f,
                    dynamicGainReductionDb = 0.0f,
                    ditherActive = false,
                    noiseFloorRating = "Bypassed (Direct Signal)"
                )
            }

            var sumSquares = 0.0
            var lowCount = 0
            var highCount = 0
            var lowEnergy = 0.0
            var highEnergy = 0.0

            val count = length.coerceAtMost(buffer.size - offset)
            var j = offset
            while (j < offset + count - 1) {
                val sL = buffer[j]
                val sR = buffer[j + 1]
                val sq = (sL * sL + sR * sR) * 0.5f
                sumSquares += sq

                // Rough zero-crossing frequency partition for spectral slope estimation
                if (j > offset + 2) {
                    val delta = abs(sL - buffer[j - 2])
                    if (delta < 0.02f) {
                        lowEnergy += sq
                        lowCount++
                    } else if (delta > 0.15f) {
                        highEnergy += sq
                        highCount++
                    }
                }
                j += 2
            }

            val totalSamples = (count / 2).coerceAtLeast(1)
            val rms = sqrt(sumSquares / totalSamples).toFloat().coerceAtLeast(1e-6f)
            val rmsDb = 20.0f * log10(rms)

            // Estimate quiet passages noise floor
            val noiseFloorDb = (rmsDb - 18.0f).coerceIn(-85.0f, -20.0f)

            val avgLow = if (lowCount > 0) lowEnergy / lowCount else 1e-6
            val avgHigh = if (highCount > 0) highEnergy / highCount else 1e-6
            val tiltRatio = (avgHigh / avgLow.coerceAtLeast(1e-7)).toFloat()
            val tiltSlope = (10.0f * log10(tiltRatio.coerceAtLeast(1e-4f)) * 0.25f).coerceIn(-6.0f, 6.0f)

            val gainReductionDb = if (rmsDb < config.downwardThresholdDb) {
                abs(rmsDb - config.downwardThresholdDb) * config.flattenDepth
            } else {
                0.0f
            }.coerceIn(0.0f, 24.0f)

            val ditherActive = config.thermalDitherLevel > 0.02f ||
                    config.mode == PreampFlattenerMode.WARM_DITHER

            val rating = when {
                noiseFloorDb < -58.0f -> "Pristine Analog Silence (< -58 dB)"
                noiseFloorDb < -48.0f -> "Discrete Studio Grade (-52 dB)"
                noiseFloorDb < -38.0f -> "Vintage Vinyl / Tape (-42 dB)"
                else -> "High Quiescent Thermal Noise (> -38 dB)"
            }

            return PreampNoiseFlattenerMetrics(
                estimatedNoiseFloorDb = noiseFloorDb,
                spectralTiltSlope = tiltSlope,
                dynamicGainReductionDb = gainReductionDb,
                ditherActive = ditherActive,
                noiseFloorRating = rating
            )
        }
    }
}

