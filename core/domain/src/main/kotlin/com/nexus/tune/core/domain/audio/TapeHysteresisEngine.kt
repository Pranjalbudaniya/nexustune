package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object TapeHysteresisEngine {

    class TapeState(val sampleRate: Int = 48000) {
        // Head bump peaking biquad state
        var hbX1: Float = 0.0f
        var hbX2: Float = 0.0f
        var hbY1: Float = 0.0f
        var hbY2: Float = 0.0f

        // High frequency tape speed rolloff biquad state
        var hfX1: Float = 0.0f
        var hfX2: Float = 0.0f
        var hfY1: Float = 0.0f
        var hfY2: Float = 0.0f

        // Wow & Flutter ring buffer & LFO phases
        private val ringBufferSize = 512
        private val ringBuffer = FloatArray(ringBufferSize)
        private var writeIndex: Int = 0

        private var lfoWowPhase: Double = 0.0
        private var lfoFlutterPhase: Double = 0.0

        fun filterHeadBump(sample: Float, speed: TapeSpeed, gainDb: Float): Float {
            if (gainDb <= 0.01f) return sample

            val f0 = when (speed) {
                TapeSpeed.IPS_30 -> 75.0f
                TapeSpeed.IPS_15 -> 55.0f
                TapeSpeed.IPS_7_5 -> 45.0f
            }
            val q = when (speed) {
                TapeSpeed.IPS_30 -> 1.4f
                TapeSpeed.IPS_15 -> 1.2f
                TapeSpeed.IPS_7_5 -> 1.0f
            }

            val a = 10.0.pow(gainDb.toDouble() / 40.0)
            val w0 = 2.0 * Math.PI * f0 / sampleRate
            val cosW = cos(w0)
            val sinW = sin(w0)
            val alpha = sinW / (2.0 * q)

            val b0 = 1.0 + alpha * a
            val b1 = -2.0 * cosW
            val b2 = 1.0 - alpha * a
            val a0 = 1.0 + alpha / a
            val a1 = -2.0 * cosW
            val a2 = 1.0 - alpha / a

            val y = ((b0 / a0) * sample + (b1 / a0) * hbX1 + (b2 / a0) * hbX2 - (a1 / a0) * hbY1 - (a2 / a0) * hbY2).toFloat()
            hbX2 = hbX1
            hbX1 = sample
            hbY2 = hbY1
            hbY1 = y
            return y
        }

        fun filterTapeSpeedRolloff(sample: Float, speed: TapeSpeed): Float {
            if (speed == TapeSpeed.IPS_30) return sample

            val cutoff = if (speed == TapeSpeed.IPS_7_5) 14000.0f else 18500.0f
            val w0 = 2.0 * Math.PI * cutoff / sampleRate
            val cosW = cos(w0)
            val sinW = sin(w0)
            val alpha = sinW / (2.0 * 0.7071)

            val b0 = (1.0 - cosW) / 2.0
            val b1 = 1.0 - cosW
            val b2 = (1.0 - cosW) / 2.0
            val a0 = 1.0 + alpha
            val a1 = -2.0 * cosW
            val a2 = 1.0 - alpha

            val y = ((b0 / a0) * sample + (b1 / a0) * hfX1 + (b2 / a0) * hfX2 - (a1 / a0) * hfY1 - (a2 / a0) * hfY2).toFloat()
            hfX2 = hfX1
            hfX1 = sample
            hfY2 = hfY1
            hfY1 = y
            return y
        }

        fun processWowFlutter(sample: Float, depthNorm: Float): Float {
            if (depthNorm <= 0.01f) return sample

            ringBuffer[writeIndex] = sample

            val wowRate = 0.8
            val flutterRate = 8.5

            lfoWowPhase += 2.0 * Math.PI * wowRate / sampleRate
            if (lfoWowPhase >= 2.0 * Math.PI) lfoWowPhase -= 2.0 * Math.PI

            lfoFlutterPhase += 2.0 * Math.PI * flutterRate / sampleRate
            if (lfoFlutterPhase >= 2.0 * Math.PI) lfoFlutterPhase -= 2.0 * Math.PI

            val wowMod = sin(lfoWowPhase) * (depthNorm * 1.6)
            val flutterMod = sin(lfoFlutterPhase) * (depthNorm * 0.5)
            val totalDelay = (16.0 + wowMod + flutterMod).coerceIn(4.0, 32.0)

            val readPos = writeIndex - totalDelay
            val readIndexFloor = floor(readPos).toInt()
            val frac = (readPos - readIndexFloor).toFloat()

            val idx0 = (readIndexFloor % ringBufferSize + ringBufferSize) % ringBufferSize
            val idx1 = ((readIndexFloor + 1) % ringBufferSize + ringBufferSize) % ringBufferSize

            val interpolated = ringBuffer[idx0] * (1.0f - frac) + ringBuffer[idx1] * frac

            writeIndex = (writeIndex + 1) % ringBufferSize
            return interpolated
        }

        fun reset() {
            hbX1 = 0.0f; hbX2 = 0.0f; hbY1 = 0.0f; hbY2 = 0.0f
            hfX1 = 0.0f; hfX2 = 0.0f; hfY1 = 0.0f; hfY2 = 0.0f
            ringBuffer.fill(0.0f)
            writeIndex = 0
            lfoWowPhase = 0.0
            lfoFlutterPhase = 0.0
        }
    }

    /**
     * Langevin-inspired magnetic hysteresis soft-clipping function.
     */
    private fun magneticHysteresis(x: Float, drive: Float): Float {
        val driven = x * (1.0f + 2.5f * drive)
        val norm = (2.0f / Math.PI.toFloat()) * atan(driven * (Math.PI.toFloat() / 2.0f))
        val compression = 1.0f - 0.06f * driven * driven * sign(driven)
        return norm * compression
    }

    /**
     * Processes a single audio sample through the analog tape warmer modeling engine.
     */
    fun processSample(
        sample: Float,
        config: TapeHysteresisConfig,
        state: TapeState? = null
    ): Float {
        if (!config.isEnabled || config.preset == TapePreset.OFF) {
            return sample
        }

        val driveNorm = config.tapeDrive.coerceIn(0.0f, 1.0f)
        val wowNorm = config.wowFlutterDepth.coerceIn(0.0f, 1.0f)
        val bumpGain = config.headBumpGainDb.coerceIn(0.0f, 4.5f)

        // Step 1: Head bump low-frequency electromagnetic resonance
        var y = if (state != null) {
            state.filterHeadBump(sample, config.speed, bumpGain)
        } else {
            sample
        }

        // Step 2: Tape speed high-frequency shelf / rolloff
        y = if (state != null) {
            state.filterTapeSpeedRolloff(y, config.speed)
        } else {
            y
        }

        // Step 3: Magnetic hysteresis saturation transfer curve
        val saturated = magneticHysteresis(y, driveNorm)
        val autoMakeup = 1.0f / sqrt(1.0f + 1.1f * driveNorm * driveNorm)
        y = saturated * autoMakeup

        // Step 4: Micro-mechanical Wow & Flutter modulation
        y = if (state != null) {
            state.processWowFlutter(y, wowNorm)
        } else {
            y
        }

        // Analog soft saturation ceiling
        if (abs(y) > 0.98f) {
            y = tanh(y)
        }

        return y.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Processes a buffer of PCM audio samples.
     */
    fun processBuffer(samples: FloatArray, config: TapeHysteresisConfig): FloatArray {
        if (!config.isEnabled || config.preset == TapePreset.OFF) {
            return samples.copyOf()
        }
        val output = FloatArray(samples.size)
        val state = TapeState()
        for (i in samples.indices) {
            output[i] = processSample(samples[i], config, state)
        }
        return output
    }

    /**
     * Calculates hardware equalizer sub-bass warmth offset in millibels for Band 0 & Band 1.
     */
    fun calculateHardwareTapeOffset(config: TapeHysteresisConfig): Int {
        if (!config.isEnabled || config.preset == TapePreset.OFF) {
            return 0
        }
        val bumpFactor = (config.headBumpGainDb / 4.5f).coerceIn(0.0f, 1.0f)
        val driveFactor = config.tapeDrive.coerceIn(0.0f, 1.0f)
        val baseOffset = when (config.speed) {
            TapeSpeed.IPS_30 -> 120
            TapeSpeed.IPS_15 -> 220
            TapeSpeed.IPS_7_5 -> 180
        }
        return (baseOffset * (0.5f * bumpFactor + 0.5f * driveFactor)).roundToInt().coerceIn(40, 300)
    }

    /**
     * Synthesizes real-time metrics for UI presentation and diagnostics.
     */
    fun calculateMetrics(config: TapeHysteresisConfig): TapeHysteresisMetrics {
        if (!config.isEnabled || config.preset == TapePreset.OFF) {
            return TapeHysteresisMetrics(
                saturationCompressionDb = 0.0f,
                headBumpFrequencyHz = 0.0f,
                wowFlutterPpm = 0,
                analogWarmthScore = 0,
                tapeCharacterRating = "Pristine Digital Direct • Tape Bypassed"
            )
        }

        val driveNorm = config.tapeDrive.coerceIn(0.0f, 1.0f)
        val wowNorm = config.wowFlutterDepth.coerceIn(0.0f, 1.0f)
        val bumpGain = config.headBumpGainDb.coerceIn(0.0f, 4.5f)

        val compDb = (20.0 * log10(1.0 / sqrt(1.0 + 1.1 * driveNorm * driveNorm))).toFloat()

        val headBumpFreq = when (config.speed) {
            TapeSpeed.IPS_30 -> 75.0f
            TapeSpeed.IPS_15 -> 55.0f
            TapeSpeed.IPS_7_5 -> 45.0f
        }

        val wowPpm = (wowNorm * 85.0f).roundToInt()
        val warmth = ((driveNorm * 50.0f) + ((bumpGain / 4.5f) * 35.0f) + (wowNorm * 15.0f)).roundToInt().coerceIn(10, 100)

        val rating = when (config.speed) {
            TapeSpeed.IPS_30 -> "Studer A800 Mastering Reel • Transparent Analog Glue"
            TapeSpeed.IPS_15 -> "Ampex ATR-102 Studio Warmth • Deep 55 Hz Head Bump"
            TapeSpeed.IPS_7_5 -> "Vintage Ferric Cassette • Nostalgic Capstan Warble"
        }

        return TapeHysteresisMetrics(
            saturationCompressionDb = (round(compDb * 10.0f) / 10.0f),
            headBumpFrequencyHz = headBumpFreq,
            wowFlutterPpm = wowPpm,
            analogWarmthScore = warmth,
            tapeCharacterRating = rating
        )
    }
}

