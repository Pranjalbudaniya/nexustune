package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.HornDiffuserConfig
import com.nexus.tune.core.model.HornDiffuserMetrics
import com.nexus.tune.core.model.HornDiffuserMode
import com.nexus.tune.core.model.HornFlareGeometry
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Audiophile Dynamic Acoustic Resonator & Cabinet Horn Diffuser Studio Engine.
 *
 * Simulates Tractrix, Exponential, and Conical acoustic horn flare loading, throat cavity
 * standing wave anti-resonance filtering, and quadratic residue horizontal dispersion for
 * hyper-efficient, lifelike acoustic reproduction.
 */
class HornDiffuserEngine(
    private val sampleRate: Int = 44100
) {
    // Throat cavity anti-resonance notch biquad filter states (L and R)
    private var notchX1L = 0.0f
    private var notchX2L = 0.0f
    private var notchY1L = 0.0f
    private var notchY2L = 0.0f

    private var notchX1R = 0.0f
    private var notchX2R = 0.0f
    private var notchY1R = 0.0f
    private var notchY2R = 0.0f

    // Horn flare acoustic loading peaking/shelving filter states
    private var flareX1L = 0.0f
    private var flareX2L = 0.0f
    private var flareY1L = 0.0f
    private var flareY2L = 0.0f

    private var flareX1R = 0.0f
    private var flareX2R = 0.0f
    private var flareY1R = 0.0f
    private var flareY2R = 0.0f

    // Horizontal flare micro-delay buffer (up to 64 samples for QRD dispersion ~1.4 ms)
    private val delayBufferL = FloatArray(128)
    private val delayBufferR = FloatArray(128)
    private var delayWriteIndex = 0

    fun reset() {
        notchX1L = 0.0f
        notchX2L = 0.0f
        notchY1L = 0.0f
        notchY2L = 0.0f
        notchX1R = 0.0f
        notchX2R = 0.0f
        notchY1R = 0.0f
        notchY2R = 0.0f

        flareX1L = 0.0f
        flareX2L = 0.0f
        flareY1L = 0.0f
        flareY2L = 0.0f
        flareX1R = 0.0f
        flareX2R = 0.0f
        flareY1R = 0.0f
        flareY2R = 0.0f

        delayBufferL.fill(0.0f)
        delayBufferR.fill(0.0f)
        delayWriteIndex = 0
    }

    /**
     * Process an interleaved stereo PCM frame buffer in-place.
     */
    fun processInterleaved(
        buffer: FloatArray,
        offset: Int = 0,
        length: Int = buffer.size,
        config: HornDiffuserConfig
    ): HornDiffuserMetrics {
        if (!config.isEnabled || length < 2) {
            return calculateMetrics(config, buffer, offset, length, sampleRate)
        }

        // 1. Calculate throat anti-resonance notch biquad coefficients (~1800 Hz)
        val notchGainDb = (config.throatAntiResonanceDb * config.flareIntensity).coerceIn(-12.0f, 0.0f)
        val notchCoeffs = calculatePeakingBiquadCoefficients(1800.0f, notchGainDb, 2.2f, sampleRate)

        // 2. Calculate acoustic loading boost coefficients (~800 Hz lower-mid punch)
        val loadingGainDb = when (config.geometry) {
            HornFlareGeometry.TRACTRIX -> 2.5f * config.flareIntensity
            HornFlareGeometry.EXPONENTIAL -> 3.2f * config.flareIntensity
            HornFlareGeometry.CONICAL_WAVEGUIDE -> 1.2f * config.flareIntensity
            HornFlareGeometry.SECTORAL_RADIAL -> 2.0f * config.flareIntensity
            HornFlareGeometry.CONSTANT_DIRECTIVITY -> 1.8f * config.flareIntensity
        }
        val flareCoeffs = calculatePeakingBiquadCoefficients(config.throatCutoffHz * 1.8f, loadingGainDb, 1.2f, sampleRate)

        // 3. Dispersion delay offset (in samples: 10 to 45 samples)
        val delaySamples = (10 + (config.horizontalCoverageAngle - 45) * 35 / 75).coerceIn(4, 120)
        val dispersionFactor = ((config.horizontalCoverageAngle - 45) / 150.0f) * 0.22f * config.flareIntensity

        var i = offset
        while (i < offset + length - 1) {
            val inL = buffer[i]
            val inR = buffer[i + 1]

            // Step A: Throat Anti-Resonance Notch Filter
            var filteredL = inL
            var filteredR = inR
            if (abs(notchGainDb) > 0.05f || config.mode == HornDiffuserMode.THROAT_ANTI_RESONANCE) {
                filteredL = notchCoeffs.b0 * inL + notchCoeffs.b1 * notchX1L + notchCoeffs.b2 * notchX2L -
                        notchCoeffs.a1 * notchY1L - notchCoeffs.a2 * notchY2L
                notchX2L = notchX1L
                notchX1L = inL
                notchY2L = notchY1L
                notchY1L = filteredL

                filteredR = notchCoeffs.b0 * inR + notchCoeffs.b1 * notchX1R + notchCoeffs.b2 * notchX2R -
                        notchCoeffs.a1 * notchY1R - notchCoeffs.a2 * notchY2R
                notchX2R = notchX1R
                notchX1R = inR
                notchY2R = notchY1R
                notchY1R = filteredR
            }

            // Step B: Horn Flare Acoustic Loading Filter
            var loadedL = filteredL
            var loadedR = filteredR
            if (abs(loadingGainDb) > 0.05f) {
                loadedL = flareCoeffs.b0 * filteredL + flareCoeffs.b1 * flareX1L + flareCoeffs.b2 * flareX2L -
                        flareCoeffs.a1 * flareY1L - flareCoeffs.a2 * flareY2L
                flareX2L = flareX1L
                flareX1L = filteredL
                flareY2L = flareY1L
                flareY1L = loadedL

                loadedR = flareCoeffs.b0 * filteredR + flareCoeffs.b1 * flareX1R + flareCoeffs.b2 * flareX2R -
                        flareCoeffs.a1 * flareY1R - flareCoeffs.a2 * flareY2R
                flareX2R = flareX1R
                flareX1R = filteredR
                flareY2R = flareY1R
                flareY1R = loadedR
            }

            // Step C: Compression Driver Air Viscosity Softening
            val viscL = loadedL - 0.05f * (loadedL * loadedL) * config.flareIntensity
            val viscR = loadedR - 0.05f * (loadedR * loadedR) * config.flareIntensity

            // Step D: Horizontal QRD Waveguide Dispersion
            delayBufferL[delayWriteIndex] = viscL
            delayBufferR[delayWriteIndex] = viscR

            val readIndex = (delayWriteIndex - delaySamples + 128) and 127
            val delayedL = delayBufferL[readIndex]
            val delayedR = delayBufferR[readIndex]

            delayWriteIndex = (delayWriteIndex + 1) and 127

            var outL: Float
            var outR: Float
            if (config.mode == HornDiffuserMode.WIDE_DISPERSION || dispersionFactor > 0.01f) {
                outL = viscL + delayedR * dispersionFactor
                outR = viscR + delayedL * dispersionFactor
            } else {
                outL = viscL
                outR = viscR
            }

            // Step E: Output Routing / Solo Coloration Mode
            if (config.mode == HornDiffuserMode.SOLO_HORN_COLORATION) {
                buffer[i] = ((outL - inL) * 3.0f).coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = ((outR - inR) * 3.0f).coerceIn(-1.0f, 1.0f)
            } else {
                buffer[i] = outL.coerceIn(-1.0f, 1.0f)
                buffer[i + 1] = outR.coerceIn(-1.0f, 1.0f)
            }

            i += 2
        }

        return calculateMetrics(config, buffer, offset, length, sampleRate)
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
         * Calculates peaking biquad filter coefficients according to Audio EQ Cookbook.
         */
        fun calculatePeakingBiquadCoefficients(
            frequencyHz: Float,
            gainDb: Float,
            q: Float,
            sampleRate: Int
        ): BiquadCoeffs {
            if (abs(gainDb) < 0.01f) {
                return BiquadCoeffs(1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            }

            val a = 10.0f.pow(gainDb / 40.0f)
            val w0 = (2.0 * PI * frequencyHz / sampleRate).toFloat()
            val alpha = sin(w0) / (2.0f * q)
            val cosW0 = cos(w0)

            val a0 = 1.0f + alpha / a
            val b0 = (1.0f + alpha * a) / a0
            val b1 = (-2.0f * cosW0) / a0
            val b2 = (1.0f - alpha * a) / a0
            val a1 = (-2.0f * cosW0) / a0
            val a2 = (1.0f - alpha / a) / a0

            return BiquadCoeffs(b0, b1, b2, a1, a2)
        }

        /**
         * Calculates real-time metrics from audio buffer and configuration.
         */
        fun calculateMetrics(
            config: HornDiffuserConfig,
            buffer: FloatArray,
            offset: Int = 0,
            length: Int = buffer.size,
            sampleRate: Int = 44100
        ): HornDiffuserMetrics {
            if (!config.isEnabled || length < 2) {
                return HornDiffuserMetrics(
                    throatAcousticImpedanceRatio = 1.0f,
                    effectiveCutoffFrequencyHz = config.throatCutoffHz,
                    horizontalCoverageAngle = config.horizontalCoverageAngle,
                    hornLoadingRating = "Bypassed (Direct Radiation)"
                )
            }

            val impedanceRatio = when (config.geometry) {
                HornFlareGeometry.TRACTRIX -> 2.8f + config.flareIntensity * 0.8f
                HornFlareGeometry.EXPONENTIAL -> 3.5f + config.flareIntensity * 1.0f
                HornFlareGeometry.CONICAL_WAVEGUIDE -> 1.6f + config.flareIntensity * 0.4f
                HornFlareGeometry.SECTORAL_RADIAL -> 2.4f + config.flareIntensity * 0.6f
                HornFlareGeometry.CONSTANT_DIRECTIVITY -> 2.1f + config.flareIntensity * 0.5f
            }

            val effectiveCutoff = config.throatCutoffHz * (1.0f - config.flareIntensity * 0.15f)

            val rating = when {
                impedanceRatio > 3.8f -> "Massive Acoustic Loading (${config.geometry.displayName})"
                config.throatAntiResonanceDb < -4.0f -> "Studio Anti-Honk Damped"
                config.horizontalCoverageAngle > 100 -> "Ultra-Wide QRD Soundstage (${config.horizontalCoverageAngle}°)"
                else -> "Balanced Horn Dispersion (${config.horizontalCoverageAngle}°)"
            }

            return HornDiffuserMetrics(
                throatAcousticImpedanceRatio = impedanceRatio,
                effectiveCutoffFrequencyHz = effectiveCutoff,
                horizontalCoverageAngle = config.horizontalCoverageAngle,
                hornLoadingRating = rating
            )
        }
    }
}

