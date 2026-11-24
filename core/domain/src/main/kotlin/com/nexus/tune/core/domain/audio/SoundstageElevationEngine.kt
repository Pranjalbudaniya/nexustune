package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.ElevationAngle
import com.nexus.tune.core.model.SoundstageElevationConfig
import com.nexus.tune.core.model.SoundstageElevationMetrics
import com.nexus.tune.core.model.SoundstageElevationPreset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Audiophile Psychoacoustic Soundstage Elevation & Height Virtualizer Studio Engine.
 *
 * Emulates median-plane Head-Related Transfer Functions (HRTFs) using anatomical pinna
 * spectral notch biquads (7.8 - 9.2 kHz), overhead air shelving (>11 kHz), and micro-delay
 * ceiling reflection lines (1.5 - 8.0 ms) to elevate perceived soundstage from chin level
 * to natural eye/forehead elevation over headphones.
 */
class SoundstageElevationEngine(
    private val sampleRate: Int = 44100
) {
    private class BiquadFilter {
        var b0: Double = 1.0
        var b1: Double = 0.0
        var b2: Double = 0.0
        var a1: Double = 0.0
        var a2: Double = 0.0

        var x1: Double = 0.0
        var x2: Double = 0.0
        var y1: Double = 0.0
        var y2: Double = 0.0

        fun configurePeaking(centerFreq: Double, sampleRate: Double, q: Double, gainDb: Double) {
            val nyquist = sampleRate * 0.495
            val f0 = centerFreq.coerceIn(100.0, nyquist)
            val w0 = 2.0 * PI * f0 / sampleRate
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val alpha = sinW0 / (2.0 * q.coerceIn(1.0, 30.0))
            val a = 10.0.pow(gainDb.coerceIn(-30.0, 30.0) / 40.0)

            val b0Raw = 1.0 + alpha * a
            val b1Raw = -2.0 * cosW0
            val b2Raw = 1.0 - alpha * a
            val a0Raw = 1.0 + alpha / a
            val a1Raw = -2.0 * cosW0
            val a2Raw = 1.0 - alpha / a

            b0 = b0Raw / a0Raw
            b1 = b1Raw / a0Raw
            b2 = b2Raw / a0Raw
            a1 = a1Raw / a0Raw
            a2 = a2Raw / a0Raw
        }

        fun configureHighShelf(cutoffFreq: Double, sampleRate: Double, gainDb: Double) {
            val nyquist = sampleRate * 0.495
            val f0 = cutoffFreq.coerceIn(100.0, nyquist)
            val w0 = 2.0 * PI * f0 / sampleRate
            val cosW0 = cos(w0)
            val sinW0 = sin(w0)
            val a = 10.0.pow(gainDb.coerceIn(-30.0, 30.0) / 40.0)
            val alpha = sinW0 / 2.0 * sqrt((a + 1.0 / a) * (1.0 / 0.707 - 1.0) + 2.0)

            val b0Raw = a * ((a + 1.0) + (a - 1.0) * cosW0 + 2.0 * sqrt(a) * alpha)
            val b1Raw = -2.0 * a * ((a - 1.0) + (a + 1.0) * cosW0)
            val b2Raw = a * ((a + 1.0) + (a - 1.0) * cosW0 - 2.0 * sqrt(a) * alpha)
            val a0Raw = (a + 1.0) - (a - 1.0) * cosW0 + 2.0 * sqrt(a) * alpha
            val a1Raw = 2.0 * ((a - 1.0) - (a + 1.0) * cosW0)
            val a2Raw = (a + 1.0) - (a - 1.0) * cosW0 - 2.0 * sqrt(a) * alpha

            b0 = b0Raw / a0Raw
            b1 = b1Raw / a0Raw
            b2 = b2Raw / a0Raw
            a1 = a1Raw / a0Raw
            a2 = a2Raw / a0Raw
        }

        fun process(x: Float): Float {
            val xIn = x.toDouble()
            val y = b0 * xIn + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1
            x1 = xIn
            y2 = y1
            y1 = y
            return y.toFloat()
        }

        fun reset() {
            x1 = 0.0
            x2 = 0.0
            y1 = 0.0
            y2 = 0.0
        }
    }

    private val leftPinnaNotch = BiquadFilter()
    private val rightPinnaNotch = BiquadFilter()
    private val leftAirShelf = BiquadFilter()
    private val rightAirShelf = BiquadFilter()

    // Ceiling reflection delay line (up to 12 ms buffer)
    private val maxDelaySamples = (sampleRate * 0.012).toInt().coerceAtLeast(128)
    private val leftDelayLine = FloatArray(maxDelaySamples)
    private val rightDelayLine = FloatArray(maxDelaySamples)
    private var delayWriteIndex = 0

    // High frequency damping for ceiling reflections
    private var leftDampX1 = 0.0f
    private var rightDampX1 = 0.0f

    fun reset() {
        leftPinnaNotch.reset()
        rightPinnaNotch.reset()
        leftAirShelf.reset()
        rightAirShelf.reset()
        leftDelayLine.fill(0f)
        rightDelayLine.fill(0f)
        delayWriteIndex = 0
        leftDampX1 = 0f
        rightDampX1 = 0f
    }

    /**
     * Processes interleaved stereo audio buffer in-place.
     */
    fun process(buffer: FloatArray, config: SoundstageElevationConfig): SoundstageElevationMetrics {
        if (!config.isEnabled || config.preset == SoundstageElevationPreset.OFF || config.elevationAngle == ElevationAngle.EYE_LEVEL_0 || buffer.isEmpty()) {
            return SoundstageElevationMetrics(
                virtualElevationDegrees = 0.0f,
                pinnaNotchFrequencyHz = 0.0f,
                ceilingDelaySamples = 0,
                spatialHeightScore = 0,
                elevationRating = "Bypassed (Eye-Level Neutral)"
            )
        }

        val angle = config.elevationAngle
        val intensity = config.elevationIntensity.coerceIn(0.0f, 1.0f)
        val notchFreq = angle.pinnaNotchHz.toDouble()
        val notchDepth = config.pinnaNotchDepthDb.toDouble() * intensity
        val airGain = 2.5 * intensity

        // 1. Configure Pinna Median Plane Notch & Overhead Air Shelving
        leftPinnaNotch.configurePeaking(notchFreq, sampleRate.toDouble(), 7.5, notchDepth)
        rightPinnaNotch.configurePeaking(notchFreq, sampleRate.toDouble(), 7.5, notchDepth)

        leftAirShelf.configureHighShelf(11500.0, sampleRate.toDouble(), airGain)
        rightAirShelf.configureHighShelf(11500.0, sampleRate.toDouble(), airGain)

        // 2. Compute ceiling reflection delay
        val delaySamples = ((config.ceilingReflectionMs * 0.001f * sampleRate).toInt()).coerceIn(16, maxDelaySamples - 1)
        val reflectionGain = (0.18f * intensity).coerceIn(0.0f, 0.35f)
        val diffuse = config.diffuseCeilingSpread

        // 3. Process stream
        var i = 0
        while (i < buffer.size - 1) {
            val inLeft = buffer[i]
            val inRight = buffer[i + 1]

            // Write to delay lines
            leftDelayLine[delayWriteIndex] = inLeft
            rightDelayLine[delayWriteIndex] = inRight

            // Read ceiling reflection
            var readIndex = delayWriteIndex - delaySamples
            if (readIndex < 0) readIndex += maxDelaySamples

            val delayedLeftRaw = leftDelayLine[readIndex]
            val delayedRightRaw = rightDelayLine[readIndex]

            // Ceiling high frequency acoustic absorption (simple 1-pole low-pass ~ 5.5 kHz)
            val dampCoeff = 0.55f
            val reflLeftDamped = delayedLeftRaw * (1f - dampCoeff) + leftDampX1 * dampCoeff
            val reflRightDamped = delayedRightRaw * (1f - dampCoeff) + rightDampX1 * dampCoeff
            leftDampX1 = reflLeftDamped
            rightDampX1 = reflRightDamped

            // Cross-channel reflection mix for overhead proscenium elevation
            val ceilingLeft = if (diffuse) reflLeftDamped * 0.7f + reflRightDamped * 0.3f else reflLeftDamped
            val ceilingRight = if (diffuse) reflRightDamped * 0.7f + reflLeftDamped * 0.3f else reflRightDamped

            // Apply pinna elevation notch to direct sound
            var outLeft = leftPinnaNotch.process(inLeft) + ceilingLeft * reflectionGain
            var outRight = rightPinnaNotch.process(inRight) + ceilingRight * reflectionGain

            // Apply overhead air shelf
            outLeft = leftAirShelf.process(outLeft)
            outRight = rightAirShelf.process(outRight)

            buffer[i] = outLeft.coerceIn(-1.0f, 1.0f)
            buffer[i + 1] = outRight.coerceIn(-1.0f, 1.0f)

            delayWriteIndex = (delayWriteIndex + 1) % maxDelaySamples
            i += 2
        }

        val apparentAngle = angle.degrees * intensity
        val heightScore = ((apparentAngle / 45.0f) * 100.0f).toInt().coerceIn(0, 100)
        val rating = when {
            apparentAngle >= 35.0f -> "Cathedral Dome 3D Envelopment"
            apparentAngle >= 22.0f -> "Elevated Concert Balcony Perspective"
            apparentAngle >= 10.0f -> "Natural Standing Vocalist Height"
            else -> "Subtle Pinna Frontal Lift"
        }

        return SoundstageElevationMetrics(
            virtualElevationDegrees = apparentAngle,
            pinnaNotchFrequencyHz = notchFreq.toFloat(),
            ceilingDelaySamples = delaySamples,
            spatialHeightScore = heightScore,
            elevationRating = rating
        )
    }

    /**
     * Calculates physical equalizer compensation on Band 4 (high treble) to support
     * overhead spatial air extension.
     */
    fun calculateHardwareEqCompensation(config: SoundstageElevationConfig): Short {
        if (!config.isEnabled || config.preset == SoundstageElevationPreset.OFF || config.elevationAngle == ElevationAngle.EYE_LEVEL_0) return 0
        return when (config.elevationAngle) {
            ElevationAngle.CATHEDRAL_DOME_45 -> 70 // +0.7 dB air
            ElevationAngle.BALCONY_30 -> 50 // +0.5 dB
            ElevationAngle.STAGE_LIFT_15 -> 30 // +0.3 dB
            ElevationAngle.EYE_LEVEL_0 -> 0
        }
    }

    companion object {
        fun calculateMetrics(config: SoundstageElevationConfig): SoundstageElevationMetrics {
            if (!config.isEnabled || config.preset == SoundstageElevationPreset.OFF || config.elevationAngle == ElevationAngle.EYE_LEVEL_0) {
                return SoundstageElevationMetrics(
                    virtualElevationDegrees = 0.0f,
                    pinnaNotchFrequencyHz = 0.0f,
                    ceilingDelaySamples = 0,
                    spatialHeightScore = 0,
                    elevationRating = "Bypassed (Eye-Level Neutral)"
                )
            }

            val intensity = config.elevationIntensity.coerceIn(0.0f, 1.0f)
            val apparentAngle = config.elevationAngle.degrees * intensity
            val heightScore = ((apparentAngle / 45.0f) * 100.0f).toInt().coerceIn(0, 100)
            val rating = when {
                apparentAngle >= 35.0f -> "Cathedral Dome 3D Envelopment"
                apparentAngle >= 22.0f -> "Elevated Concert Balcony Perspective"
                apparentAngle >= 10.0f -> "Natural Standing Vocalist Height"
                else -> "Subtle Pinna Frontal Lift"
            }

            val delaySamples = (config.ceilingReflectionMs * 0.001f * 44100).toInt()

            return SoundstageElevationMetrics(
                virtualElevationDegrees = apparentAngle,
                pinnaNotchFrequencyHz = config.elevationAngle.pinnaNotchHz,
                ceilingDelaySamples = delaySamples,
                spatialHeightScore = heightScore,
                elevationRating = rating
            )
        }
    }
}

