package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object CrosstalkCancellationEngine {

    /**
     * Calculates the interaural time delay in microseconds from discrete sample count.
     */
    fun calculateDelayMicroseconds(delaySamples: Int, sampleRate: Int = 44100): Float {
        if (delaySamples <= 0 || sampleRate <= 0) return 0.0f
        return (delaySamples.toFloat() / sampleRate.toFloat()) * 1_000_000.0f
    }

    /**
     * Calculates the apparent acoustic soundstage width span in degrees (30° physical chassis up to 175° panoramic).
     */
    fun calculateVirtualStageSpan(mode: CrosstalkCancellationMode, intensityPercent: Int): Int {
        if (mode == CrosstalkCancellationMode.OFF) return 30
        val baseSpan = mode.targetSpanDegrees
        val intensityFactor = (intensityPercent / 100.0f).coerceIn(0.1f, 1.0f)
        return (30 + ((baseSpan - 30) * intensityFactor)).roundToInt().coerceIn(30, 180)
    }

    /**
     * Evaluates a descriptive acoustic rating based on the virtual soundstage angle.
     */
    fun evaluateBinauralRating(spanDegrees: Int): String {
        return when {
            spanDegrees >= 165 -> "Full 3D Holographic ($spanDegrees° Arc)"
            spanDegrees >= 135 -> "Panoramic Stereo ($spanDegrees° Arc)"
            spanDegrees >= 80 -> "Expanded Soundfield ($spanDegrees° Arc)"
            else -> "Narrow Chassis Focus ($spanDegrees° Arc)"
        }
    }

    /**
     * Synthesizes comprehensive real-time metrics for UI state presentation.
     */
    fun calculateMetrics(
        config: CrosstalkCancellationConfig,
        sampleRate: Int = 44100
    ): CrosstalkCancellationMetrics {
        if (!config.isEnabled || config.mode == CrosstalkCancellationMode.OFF) {
            return CrosstalkCancellationMetrics(
                delayMicroseconds = 0.0f,
                effectiveCancellationDb = 0.0f,
                virtualStageSpanDegrees = 30,
                binauralRating = "Bypassed (Narrow Chassis)"
            )
        }

        val delayUs = calculateDelayMicroseconds(config.delaySamples, sampleRate)
        val intensity = (config.cancellationIntensityPercent / 100.0f).coerceIn(0.0f, 1.0f) * config.mode.defaultIntensityFactor
        val cancelDb = if (intensity > 1e-4f) {
            (20.0 * log10(intensity.toDouble())).toFloat().coerceIn(-40.0f, 0.0f)
        } else {
            0.0f
        }
        val span = calculateVirtualStageSpan(config.mode, config.cancellationIntensityPercent)
        val rating = evaluateBinauralRating(span)

        return CrosstalkCancellationMetrics(
            delayMicroseconds = delayUs,
            effectiveCancellationDb = cancelDb,
            virtualStageSpanDegrees = span,
            binauralRating = rating
        )
    }

    class XtcState(val bufferSize: Int = 64) {
        val ringL = FloatArray(bufferSize)
        val ringR = FloatArray(bufferSize)
        var writePos: Int = 0
        var shadowL: Float = 0.0f
        var shadowR: Float = 0.0f

        fun reset() {
            ringL.fill(0.0f)
            ringR.fill(0.0f)
            writePos = 0
            shadowL = 0.0f
            shadowR = 0.0f
        }
    }

    /**
     * Processes a single stereo frame through the acoustic crosstalk cancellation matrix.
     */
    fun processFrame(
        left: Float,
        right: Float,
        config: CrosstalkCancellationConfig,
        state: XtcState
    ): Pair<Float, Float> {
        if (!config.isEnabled || config.mode == CrosstalkCancellationMode.OFF) {
            return Pair(left, right)
        }

        // Write input to circular delay line
        state.ringL[state.writePos] = left
        state.ringR[state.writePos] = right

        // Read contralateral delayed samples
        val delay = config.delaySamples.coerceIn(1, state.bufferSize - 1)
        val readPos = (state.writePos - delay + state.bufferSize) % state.bufferSize
        val delayedR = state.ringR[readPos]
        val delayedL = state.ringL[readPos]

        // Advance circular pointer
        state.writePos = (state.writePos + 1) % state.bufferSize

        // Head shadow acoustic diffraction single-pole lowpass filter
        val beta = config.headShadowDamping.coerceIn(0.1f, 0.9f)
        state.shadowR = (1.0f - beta) * delayedR + beta * state.shadowR
        state.shadowL = (1.0f - beta) * delayedL + beta * state.shadowL

        // Invert and cross-inject into opposite channels
        val alpha = (config.cancellationIntensityPercent / 100.0f).coerceIn(0.0f, 1.0f) * config.mode.defaultIntensityFactor
        val outL = left - alpha * state.shadowR
        val outR = right - alpha * state.shadowL

        // Equal-energy normalization to preserve apparent center loudness
        val norm = (1.0 / sqrt(1.0 + alpha * alpha.toDouble())).toFloat()

        return Pair((outL * norm).coerceIn(-1.0f, 1.0f), (outR * norm).coerceIn(-1.0f, 1.0f))
    }
}

