package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object TubeSaturationEngine {

    class DcFilterState(private val r: Float = 0.9995f) {
        private var x1: Float = 0.0f
        private var y1: Float = 0.0f

        fun filter(x: Float): Float {
            val y = x - x1 + r * y1
            x1 = x
            y1 = y
            return y
        }

        fun reset() {
            x1 = 0.0f
            y1 = 0.0f
        }
    }

    /**
     * Non-linear transfer functions modeling vacuum tube and analog saturation physics.
     */
    private fun triodeTransfer(v: Float, drive: Float): Float {
        return if (v >= 0.0f) {
            (v - 0.25f * drive * v * v) / (1.0f + 0.5f * abs(v))
        } else {
            (v + 0.12f * drive * v * v) / (1.0f + 0.7f * abs(v))
        }
    }

    private fun pentodeTransfer(v: Float, drive: Float, bias: Float): Float {
        val t = tanh(v * (1.0f + 0.3f * drive))
        val cubic = t - (1.0f / 6.0f) * t * t * t
        val gridTilt = 0.08f * bias * (1.0f - exp(-abs(v).coerceAtMost(5.0f)))
        return cubic + gridTilt
    }

    private fun tapeTransfer(v: Float): Float {
        val norm = (2.0f / Math.PI.toFloat()) * atan(v * (Math.PI.toFloat() / 2.0f))
        val compression = 1.0f - 0.08f * v * v * sign(v)
        return norm * compression
    }

    private fun consoleTransfer(v: Float, drive: Float, bias: Float): Float {
        return v - 0.06f * drive * v * v * v + 0.04f * bias * v * v
    }

    /**
     * Computes automatic makeup gain factor to compensate for drive-induced volume inflation.
     */
    fun calculateAutoMakeupGain(drive: Float): Float {
        val clampedDrive = drive.coerceIn(0.0f, 1.0f)
        return 1.0f / sqrt(1.0f + 1.2f * clampedDrive * clampedDrive)
    }

    /**
     * Processes a single audio sample through the analog saturation modeling engine.
     */
    fun processSample(
        sample: Float,
        config: TubeSaturationConfig,
        dcFilter: DcFilterState? = null
    ): Float {
        if (!config.isEnabled || config.profile == TubeWarmthProfile.OFF) {
            return sample
        }

        val clampedDrive = config.drive.coerceIn(0.0f, 1.0f)
        val clampedBias = config.bias.coerceIn(-0.5f, 0.5f)

        // Drive gain staging (1.0x to 4.0x)
        val driveFactor = 1.0f + 3.0f * clampedDrive
        val drivenSample = sample * driveFactor
        val v = drivenSample + 0.3f * clampedBias
        val v0 = 0.3f * clampedBias

        // Evaluate non-linear transfer curve relative to quiescent DC point
        val yRaw = when (config.topology) {
            TubeTopology.TRIODE_12AX7 -> {
                triodeTransfer(v, clampedDrive) - triodeTransfer(v0, clampedDrive)
            }
            TubeTopology.PENTODE_EL34 -> {
                pentodeTransfer(v, clampedDrive, clampedBias) - pentodeTransfer(v0, clampedDrive, clampedBias)
            }
            TubeTopology.WARM_TAPE_SATURATION -> {
                tapeTransfer(v) - tapeTransfer(v0)
            }
            TubeTopology.CLEAN_ANALOG_CONSOLE -> {
                consoleTransfer(v, clampedDrive, clampedBias) - consoleTransfer(v0, clampedDrive, clampedBias)
            }
        }

        // Apply auto makeup gain
        val makeupGain = if (config.autoMakeupGain) {
            calculateAutoMakeupGain(clampedDrive)
        } else {
            1.0f
        }
        var y = yRaw * makeupGain

        // Apply recursive DC blocker if filter state provided
        if (dcFilter != null) {
            y = dcFilter.filter(y)
        }

        // Analog soft ceiling limiter
        return if (abs(y) > 0.98f) {
            tanh(y)
        } else {
            y.coerceIn(-1.0f, 1.0f)
        }
    }

    /**
     * Processes a buffer of PCM audio samples.
     */
    fun processBuffer(samples: FloatArray, config: TubeSaturationConfig): FloatArray {
        if (!config.isEnabled || config.profile == TubeWarmthProfile.OFF) {
            return samples.copyOf()
        }
        val output = FloatArray(samples.size)
        val dcFilter = DcFilterState()
        for (i in samples.indices) {
            output[i] = processSample(samples[i], config, dcFilter)
        }
        return output
    }

    /**
     * Calculates hardware equalizer warmth offset in millibels for Band 1 & Band 2.
     */
    fun calculateHardwareWarmthOffset(config: TubeSaturationConfig): Int {
        if (!config.isEnabled || config.profile == TubeWarmthProfile.OFF) {
            return 0
        }
        val driveNorm = config.drive.coerceIn(0.0f, 1.0f)
        // Scaled to +50 mB to +250 mB depending on drive and topology
        val baseOffset = when (config.topology) {
            TubeTopology.TRIODE_12AX7 -> 180
            TubeTopology.PENTODE_EL34 -> 120
            TubeTopology.WARM_TAPE_SATURATION -> 150
            TubeTopology.CLEAN_ANALOG_CONSOLE -> 80
        }
        return (baseOffset * (0.4f + 0.6f * driveNorm)).roundToInt().coerceIn(40, 300)
    }

    /**
     * Synthesizes real-time audiophile metrics through analytical Fourier tone probing.
     */
    fun calculateMetrics(config: TubeSaturationConfig): TubeSaturationMetrics {
        if (!config.isEnabled || config.profile == TubeWarmthProfile.OFF) {
            return TubeSaturationMetrics(
                secondHarmonicDb = -96.0f,
                thirdHarmonicDb = -96.0f,
                totalHarmonicDistortionPercent = 0.0f,
                warmthScore = 0,
                headroomCompensationDb = 0.0f,
                characterRating = "Transparent Direct Digital Stream (Bypassed)"
            )
        }

        // Analyze harmonic spectra using N=512 reference probe tone at -3 dBFS
        val n = 512
        val probeAmplitude = 0.7071f
        var real1 = 0.0
        var imag1 = 0.0
        var real2 = 0.0
        var imag2 = 0.0
        var real3 = 0.0
        var imag3 = 0.0

        for (i in 0 until n) {
            val angle = 2.0 * Math.PI * i / n
            val sample = (probeAmplitude * sin(angle)).toFloat()
            val y = processSample(sample, config, null).toDouble()

            real1 += y * cos(angle)
            imag1 += y * sin(angle)
            real2 += y * cos(2.0 * angle)
            imag2 += y * sin(2.0 * angle)
            real3 += y * cos(3.0 * angle)
            imag3 += y * sin(3.0 * angle)
        }

        val a1 = ((2.0 / n) * sqrt(real1 * real1 + imag1 * imag1)).coerceAtLeast(1e-4)
        val a2 = (2.0 / n) * sqrt(real2 * real2 + imag2 * imag2)
        val a3 = (2.0 / n) * sqrt(real3 * real3 + imag3 * imag3)

        val h2Ratio = (a2 / a1).coerceAtLeast(1e-5)
        val h3Ratio = (a3 / a1).coerceAtLeast(1e-5)

        val h2Db = (20.0 * log10(h2Ratio)).toFloat().coerceIn(-96.0f, 0.0f)
        val h3Db = (20.0 * log10(h3Ratio)).toFloat().coerceIn(-96.0f, 0.0f)
        val thdPercent = ((sqrt(a2 * a2 + a3 * a3) / a1) * 100.0).toFloat().coerceIn(0.0f, 50.0f)

        val driveNorm = config.drive.coerceIn(0.0f, 1.0f)
        val evenDominance = if (a2 + a3 > 1e-6) (a2 / (a2 + a3)).toFloat() else 0.5f
        val warmth = ((driveNorm * 65.0f) + (evenDominance * 35.0f)).roundToInt().coerceIn(10, 100)

        val makeupFactor = calculateAutoMakeupGain(driveNorm)
        val makeupDb = (20.0 * log10(makeupFactor.toDouble())).toFloat()

        val rating = when (config.topology) {
            TubeTopology.TRIODE_12AX7 -> "Lush 2nd-Harmonic Euphonic Warmth • Silky Vocal Air"
            TubeTopology.PENTODE_EL34 -> "Punchy Odd-Harmonic Presence • Dynamic Transient Bite"
            TubeTopology.WARM_TAPE_SATURATION -> "Analog Tape Hysteresis • Rounded Peak Dynamic Glue"
            TubeTopology.CLEAN_ANALOG_CONSOLE -> "Class A Console Headroom • Ultra-Clean Discrete Op-Amp Tone"
        }

        return TubeSaturationMetrics(
            secondHarmonicDb = (round(h2Db * 10.0f) / 10.0f),
            thirdHarmonicDb = (round(h3Db * 10.0f) / 10.0f),
            totalHarmonicDistortionPercent = (round(thdPercent * 100.0f) / 100.0f),
            warmthScore = warmth,
            headroomCompensationDb = (round(makeupDb * 10.0f) / 10.0f),
            characterRating = rating
        )
    }
}

