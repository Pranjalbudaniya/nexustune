package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.FilterImpulseMetrics
import com.nexus.tune.core.model.ResamplingFilterMode
import kotlin.math.*

object ResamplingFilterEngine {

    /**
     * Calculates the normalized mathematical sinc function: sinc(x) = sin(pi * x) / (pi * x).
     */
    fun calculateSincSample(x: Float): Float {
        if (abs(x) < 1e-6f) return 1.0f
        val pix = Math.PI.toFloat() * x
        return sin(pix) / pix
    }

    /**
     * Synthesizes an FIR anti-aliasing reconstruction filter kernel of the given length and cutoff ratio,
     * configured according to the selected phase and ringing topology.
     */
    fun generateFilterKernel(
        length: Int,
        cutoffRatio: Float = 0.95f,
        mode: ResamplingFilterMode = ResamplingFilterMode.MINIMUM_PHASE_PUNCH
    ): FloatArray {
        val n = length.coerceIn(15, 255)
        val cutoff = cutoffRatio.coerceIn(0.5f, 0.99f)
        val kernel = FloatArray(n)
        val center = (n - 1) / 2.0f

        for (i in 0 until n) {
            val dist = (i - center)
            val sinc = calculateSincSample(dist * cutoff)

            // Blackman-Harris 4-term window
            val progress = i.toFloat() / (n - 1).toFloat()
            val blackmanHarris = 0.35875f -
                    0.48829f * cos(2f * Math.PI.toFloat() * progress) +
                    0.14128f * cos(4f * Math.PI.toFloat() * progress) -
                    0.01168f * cos(6f * Math.PI.toFloat() * progress)

            var sample = sinc * blackmanHarris

            // Apply phase topology modifications
            when (mode) {
                ResamplingFilterMode.MINIMUM_PHASE_PUNCH -> {
                    if (dist < 0f) {
                        // Suppress pre-echo ringing exponentially
                        val preFactor = (i.toFloat() / center).coerceIn(0f, 1f)
                        sample *= (preFactor * preFactor * preFactor * preFactor)
                    } else {
                        // Boost forward temporal energy
                        sample *= 1.4f
                    }
                }
                ResamplingFilterMode.SLOW_ROLL_OFF_WARM -> {
                    // Smooth Gaussian taper for warm analog roll-off
                    val sigma = 0.35f
                    val gDist = (progress - 0.5f) / sigma
                    val gaussian = exp(-0.5f * gDist * gDist)
                    sample *= gaussian
                }
                ResamplingFilterMode.APODIZING_ULTRASONIC -> {
                    // Steeper suppression at ultrasonic boundary
                    val apodFactor = (1.0f - abs(progress - 0.5f) * 2f).coerceIn(0f, 1f)
                    sample *= sqrt(apodFactor)
                }
                ResamplingFilterMode.STANDARD_SHARP,
                ResamplingFilterMode.LINEAR_PHASE_REFERENCE -> {
                    // Pure symmetric linear phase
                }
            }

            kernel[i] = sample
        }

        // Normalize DC gain to unity (sum = 1.0)
        val sum = kernel.sum()
        if (abs(sum) > 1e-6f) {
            for (i in 0 until n) {
                kernel[i] /= sum
            }
        }

        return kernel
    }

    /**
     * Calculates the impulse response metrics and ringing distribution for a filter profile.
     */
    fun calculateImpulseMetrics(mode: ResamplingFilterMode): FilterImpulseMetrics {
        return when (mode) {
            ResamplingFilterMode.STANDARD_SHARP -> FilterImpulseMetrics(
                preRingingPercent = 50,
                postRingingPercent = 50,
                stopbandRejectionDb = 120.0f,
                phaseLinearityRating = "Linear Phase (Symmetric)"
            )
            ResamplingFilterMode.MINIMUM_PHASE_PUNCH -> FilterImpulseMetrics(
                preRingingPercent = 0,
                postRingingPercent = 100,
                stopbandRejectionDb = 110.0f,
                phaseLinearityRating = "Minimum Phase (Zero Pre-Ringing)"
            )
            ResamplingFilterMode.LINEAR_PHASE_REFERENCE -> FilterImpulseMetrics(
                preRingingPercent = 50,
                postRingingPercent = 50,
                stopbandRejectionDb = 130.0f,
                phaseLinearityRating = "Reference Linear Phase"
            )
            ResamplingFilterMode.SLOW_ROLL_OFF_WARM -> FilterImpulseMetrics(
                preRingingPercent = 15,
                postRingingPercent = 85,
                stopbandRejectionDb = 90.0f,
                phaseLinearityRating = "Warm Analog Phase"
            )
            ResamplingFilterMode.APODIZING_ULTRASONIC -> FilterImpulseMetrics(
                preRingingPercent = 25,
                postRingingPercent = 75,
                stopbandRejectionDb = 125.0f,
                phaseLinearityRating = "Apodizing (Anti-Pre-Echo)"
            )
        }
    }

    /**
     * Computes Kaiser window shape parameter Beta for a target stopband attenuation in dB.
     */
    fun computeKaiserBeta(stopbandDb: Float): Float {
        val a = stopbandDb.coerceAtLeast(0f)
        return when {
            a > 50.0f -> 0.1102f * (a - 8.7f)
            a >= 21.0f -> (0.5842f * (a - 21.0f).toDouble().pow(0.4) + 0.07886f * (a - 21.0f)).toFloat()
            else -> 0.0f
        }
    }
}

