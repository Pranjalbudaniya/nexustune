package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DitherMode
import com.nexus.tune.core.model.QuantizationNoiseMetrics
import kotlin.math.round
import kotlin.random.Random

object DitherShapingEngine {

    /**
     * Generates a Triangular Probability Density Function (TPDF) random sample in [-1.0f, +1.0f]
     * using two independent uniform distributions (R1 - R2).
     */
    fun generateTpdfSample(randomSource: () -> Float = { Random.nextFloat() }): Float {
        val r1 = randomSource()
        val r2 = randomSource()
        return r1 - r2
    }

    /**
     * Generates a Rectangular Probability Density Function (RPDF) random sample in [-0.5f, +0.5f]
     * using a single uniform distribution.
     */
    fun generateRpdfSample(randomSource: () -> Float = { Random.nextFloat() }): Float {
        return randomSource() - 0.5f
    }

    /**
     * Returns FIR filter error feedback coefficients for noise shaping.
     * PSYCHOACOUSTIC_LIPSHITZ: 2nd order Lipshitz/Wannamaker F-weighting [1.62f, -0.93f].
     * HIGH_PASS_CONTOUR: 1st order high-pass contour [0.95f].
     */
    fun getNoiseShapingCoefficients(mode: DitherMode): FloatArray {
        return when (mode) {
            DitherMode.PSYCHOACOUSTIC_LIPSHITZ -> floatArrayOf(1.62f, -0.93f)
            DitherMode.HIGH_PASS_CONTOUR -> floatArrayOf(0.95f)
            DitherMode.OFF,
            DitherMode.RECTANGULAR_RPDF,
            DitherMode.TRIANGULAR_TPDF -> FloatArray(0)
        }
    }

    /**
     * Quantizes an input sample [-1.0f, 1.0f] into the specified bit depth,
     * applying selected dither noise and psychoacoustic noise-shaping error feedback.
     */
    fun quantizeSample(
        input: Float,
        targetBits: Int = 16,
        mode: DitherMode = DitherMode.TRIANGULAR_TPDF,
        errorHistory: FloatArray = FloatArray(4),
        randomSource: () -> Float = { Random.nextFloat() }
    ): Float {
        val clampedBits = targetBits.coerceIn(4, 32)
        val maxLevel = ((1L shl (clampedBits - 1)) - 1L).toFloat()
        if (maxLevel <= 0f) return input

        val ditherLsb = when (mode) {
            DitherMode.OFF -> 0f
            DitherMode.RECTANGULAR_RPDF -> generateRpdfSample(randomSource)
            DitherMode.TRIANGULAR_TPDF,
            DitherMode.PSYCHOACOUSTIC_LIPSHITZ,
            DitherMode.HIGH_PASS_CONTOUR -> generateTpdfSample(randomSource)
        }

        val coeffs = getNoiseShapingCoefficients(mode)
        var shapedErrorLsb = 0f
        for (i in coeffs.indices) {
            if (i < errorHistory.size) {
                shapedErrorLsb += coeffs[i] * errorHistory[i]
            }
        }

        val scaledInput = (input.coerceIn(-1.0f, 1.0f)) * maxLevel
        val targetWithDitherAndShaping = scaledInput + ditherLsb + shapedErrorLsb
        val quantizedInt = round(targetWithDitherAndShaping).coerceIn(-maxLevel, maxLevel)
        val currentErrorLsb = targetWithDitherAndShaping - quantizedInt

        // Shift error history
        for (i in (errorHistory.size - 1) downTo 1) {
            errorHistory[i] = errorHistory[i - 1]
        }
        if (errorHistory.isNotEmpty()) {
            errorHistory[0] = currentErrorLsb
        }

        return (quantizedInt / maxLevel).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Calculates the apparent dynamic range in decibels for human psychoacoustic perception.
     * Theoretical 16-bit PCM dynamic range is 98.1 dB; Lipshitz shaping extends this to >116 dB.
     */
    fun calculateApparentDynamicRange(targetBits: Int, mode: DitherMode): Float {
        val baseSnr = (6.02f * targetBits) + 1.76f
        return (baseSnr + mode.snrBonusDb).coerceAtLeast(0f)
    }

    /**
     * Calculates detailed quantization metrics including theoretical dynamic range, effective SNR, and shaping gain.
     */
    fun calculateNoiseMetrics(targetBits: Int, mode: DitherMode): QuantizationNoiseMetrics {
        val base = (6.02f * targetBits) + 1.76f
        return QuantizationNoiseMetrics(
            theoreticalDynamicRangeDb = base,
            effectiveSnrDb = base + mode.snrBonusDb,
            noiseShapingGainDb = mode.snrBonusDb
        )
    }

    /**
     * Processes an entire buffer of audio samples, maintaining noise-shaping error state across the buffer.
     */
    fun processBuffer(
        input: FloatArray,
        targetBits: Int = 16,
        mode: DitherMode = DitherMode.TRIANGULAR_TPDF,
        randomSource: () -> Float = { Random.nextFloat() }
    ): FloatArray {
        val output = FloatArray(input.size)
        val errorHistory = FloatArray(4)
        for (i in input.indices) {
            output[i] = quantizeSample(
                input = input[i],
                targetBits = targetBits,
                mode = mode,
                errorHistory = errorHistory,
                randomSource = randomSource
            )
        }
        return output
    }

    /**
     * Estimates residual harmonic linearity error for a given quantization step size.
     * Linearized TPDF dither eliminates harmonic error completely (returns 0.0f).
     */
    fun estimateLinearityError(stepSize: Float, mode: DitherMode): Float {
        return when (mode) {
            DitherMode.OFF -> 0.5f * stepSize
            DitherMode.RECTANGULAR_RPDF -> 0.25f * stepSize
            DitherMode.TRIANGULAR_TPDF,
            DitherMode.PSYCHOACOUSTIC_LIPSHITZ,
            DitherMode.HIGH_PASS_CONTOUR -> 0.0f
        }
    }
}

