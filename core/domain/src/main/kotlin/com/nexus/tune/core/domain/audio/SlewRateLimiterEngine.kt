package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.SlewLimiterMode
import com.nexus.tune.core.model.SlewRateLimiterConfig
import com.nexus.tune.core.model.SlewRateLimiterMetrics
import com.nexus.tune.core.model.SlewRateSpeed
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Audiophile Dynamic Transient Peak Slew-Rate Limiter & Electrostatic Diaphragm Protector Studio Engine.
 *
 * Implements precision dV/dt slew limiting, second-order sample acceleration (TIM distortion index)
 * monitoring, and electrostatic stator arc protection to safeguard high-speed planar magnetic
 * and electrostatic transducers against destructive transient spikes and digital edge clipping.
 */
class SlewRateLimiterEngine(
    private val sampleRate: Int = 44100
) {
    private var prevL = 0.0f
    private var prevR = 0.0f

    private var prevInL = 0.0f
    private var prevIn2L = 0.0f
    private var prevInR = 0.0f
    private var prevIn2R = 0.0f

    private var clampedTransients = 0
    private var peakObservedVUs = 0.0f
    private var runningTimEnergy = 0.0001f

    fun reset() {
        prevL = 0.0f
        prevR = 0.0f
        prevInL = 0.0f
        prevIn2L = 0.0f
        prevInR = 0.0f
        prevIn2R = 0.0f
        clampedTransients = 0
        peakObservedVUs = 0.0f
        runningTimEnergy = 0.0001f
    }

    /**
     * Process an interleaved stereo PCM frame buffer in-place.
     */
    fun processInterleaved(
        buffer: FloatArray,
        offset: Int = 0,
        length: Int = buffer.size,
        config: SlewRateLimiterConfig
    ): SlewRateLimiterMetrics {
        if (!config.isEnabled || length < 2) {
            return calculateMetrics(config, buffer, offset, length, sampleRate)
        }

        val limitStep = calculateMaxStep(config.speed, config.dampingRatio)
        val peakThreshold = config.peakThreshold.coerceIn(0.5f, 1.0f)
        val sampleIntervalUs = 1_000_000.0f / sampleRate

        var localClamped = 0
        var maxSlewVUs = peakObservedVUs * 0.95f

        var i = offset
        while (i < offset + length - 1) {
            val inL = buffer[i]
            val inR = buffer[i + 1]

            // 1. Calculate instantaneous dV/dt (normalized to 50V peak full-scale)
            val deltaInL = inL - prevInL
            val deltaInR = inR - prevInR
            val maxDelta = max(abs(deltaInL), abs(deltaInR))
            val instantaneousVUs = (maxDelta * 50.0f) / sampleIntervalUs
            if (instantaneousVUs > maxSlewVUs) {
                maxSlewVUs = instantaneousVUs
            }

            // 2. TIM Acceleration Calculation (second derivative)
            val accL = inL - 2.0f * prevInL + prevIn2L
            val accR = inR - 2.0f * prevInR + prevIn2R
            val accEnergy = (accL * accL + accR * accR) * 0.5f
            runningTimEnergy = 0.999f * runningTimEnergy + 0.001f * accEnergy

            prevIn2L = prevInL
            prevInL = inL
            prevIn2R = prevInR
            prevInR = inR

            // 3. Slew-rate limiting for Left channel
            val deltaL = inL - prevL
            val absDeltaL = abs(deltaL)
            var outL: Float
            if (absDeltaL > limitStep && limitStep > 0.0f) {
                localClamped++
                val excess = absDeltaL - limitStep
                val clampedDelta = sign(deltaL) * (limitStep + excess * (1.0f - config.dampingRatio))
                outL = prevL + clampedDelta
            } else {
                outL = inL
            }

            // 4. Slew-rate limiting for Right channel
            val deltaR = inR - prevR
            val absDeltaR = abs(deltaR)
            var outR: Float
            if (absDeltaR > limitStep && limitStep > 0.0f) {
                localClamped++
                val excess = absDeltaR - limitStep
                val clampedDelta = sign(deltaR) * (limitStep + excess * (1.0f - config.dampingRatio))
                outR = prevR + clampedDelta
            } else {
                outR = inR
            }

            // 5. Diaphragm / Stator protection (soft-knee ceiling saturation)
            if (config.mode == SlewLimiterMode.STATOR_ARC_PROTECT ||
                config.mode == SlewLimiterMode.ANALOG_WARMTH_ROLLOFF
            ) {
                val absOutL = abs(outL)
                if (absOutL > peakThreshold) {
                    val head = 1.0f - peakThreshold
                    val excess = (absOutL - peakThreshold) / max(0.01f, head)
                    outL = sign(outL) * (peakThreshold + head * tanh(excess))
                }

                val absOutR = abs(outR)
                if (absOutR > peakThreshold) {
                    val head = 1.0f - peakThreshold
                    val excess = (absOutR - peakThreshold) / max(0.01f, head)
                    outR = sign(outR) * (peakThreshold + head * tanh(excess))
                }
            }

            // 6. Solo Clamped Edges Audition Mode
            if (config.mode == SlewLimiterMode.SOLO_CLAMPED_EDGES) {
                buffer[i] = ((inL - outL) * 4.0f).coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = ((inR - outR) * 4.0f).coerceIn(-1.0f, 1.0f)
            } else {
                buffer[i] = outL.coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = outR.coerceIn(-1.0f, 1.0f)
            }

            prevL = outL
            prevR = outR
            i += 2
        }

        clampedTransients += localClamped
        peakObservedVUs = maxSlewVUs

        return calculateMetrics(config, buffer, offset, length, sampleRate)
    }

    private fun calculateMaxStep(speed: SlewRateSpeed, dampingRatio: Float): Float {
        val baseStep = (speed.nominalVUs / 100.0f) * 0.45f
        val dampingScale = 1.0f - 0.70f * dampingRatio.coerceIn(0.0f, 1.0f)
        return (baseStep * dampingScale).coerceIn(0.005f, 1.0f)
    }

    companion object {
        /**
         * Calculates real-time metrics from audio buffer and configuration.
         */
        fun calculateMetrics(
            config: SlewRateLimiterConfig,
            buffer: FloatArray,
            offset: Int = 0,
            length: Int = buffer.size,
            sampleRate: Int = 44100
        ): SlewRateLimiterMetrics {
            if (!config.isEnabled || length < 2) {
                return SlewRateLimiterMetrics(
                    peakSlewRateVUs = 0.0f,
                    timDistortionIndex = 0.0f,
                    clampedTransientsCount = 0,
                    membraneSafetyRating = "Bypassed (Full Bandwidth Passthrough)"
                )
            }

            val sampleIntervalUs = 1_000_000.0f / sampleRate
            var maxDelta = 0.0f
            var timSum = 0.0
            var clampedApprox = 0

            val count = length.coerceAtMost(buffer.size - offset)
            val maxStep = (config.speed.nominalVUs / 100.0f) * 0.45f * (1.0f - 0.70f * config.dampingRatio)

            var j = offset + 2
            while (j < offset + count - 1) {
                val sL = buffer[j]
                val prevL = buffer[j - 2]
                val dL = abs(sL - prevL)
                if (dL > maxDelta) maxDelta = dL
                if (dL > maxStep) clampedApprox++

                if (j >= offset + 4) {
                    val prev2L = buffer[j - 4]
                    val acc = sL - 2.0f * prevL + prev2L
                    timSum += acc * acc
                }
                j += 2
            }

            val peakVUs = (maxDelta * 50.0f) / sampleIntervalUs
            val samplesCount = max(1, count / 2)
            val timRms = sqrt(timSum / samplesCount).toFloat()
            val timIndex = (timRms * 100.0f).coerceIn(0.0f, 100.0f)

            val rating = when {
                peakVUs > 45.0f && config.dampingRatio < 0.3f -> "Transient Slew Warning (> 45 V/µs)"
                clampedApprox > 15 -> "High Clamp Activity (Stator Protected)"
                timIndex > 25.0f -> "TIM Distortion Detected (Smoothing)"
                else -> "Protected: Transducer Membrane Safe"
            }

            return SlewRateLimiterMetrics(
                peakSlewRateVUs = peakVUs,
                timDistortionIndex = timIndex,
                clampedTransientsCount = clampedApprox,
                membraneSafetyRating = rating
            )
        }
    }
}

