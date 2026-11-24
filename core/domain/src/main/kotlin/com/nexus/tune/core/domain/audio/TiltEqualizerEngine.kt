package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import kotlin.math.*

object TiltEqualizerEngine {

    val STANDARD_5_BAND_FREQS = listOf(60.0f, 230.0f, 910.0f, 3600.0f, 14000.0f)
    private val TEST_OCTAVES = listOf(63.0f, 125.0f, 250.0f, 500.0f, 1000.0f, 2000.0f, 4000.0f, 8000.0f, 16000.0f)

    /**
     * Calculates the exact gain in decibels at a given frequency under a tilt rotation.
     * Pivot frequency receives exactly 0 dB gain.
     */
    fun calculateTiltGainAtFrequency(freqHz: Float, tiltDb: Float, pivotHz: Float): Float {
        if (abs(tiltDb) < 1e-4f || abs(freqHz - pivotHz) < 1e-3f) return 0.0f
        val safeFreq = freqHz.coerceIn(20.0f, 24000.0f)
        val safePivot = pivotHz.coerceIn(100.0f, 10000.0f)

        // Logarithmic frequency tilt around pivot
        val octRatio = ln(safeFreq.toDouble() / safePivot.toDouble())
        val normFactor = ln(20000.0 / safePivot.toDouble())
        if (abs(normFactor) < 1e-6) return 0.0f

        val gainDb = (tiltDb.toDouble() * (octRatio / normFactor)).toFloat()
        return gainDb.coerceIn(-12.0f, 12.0f)
    }

    /**
     * Calculates 5-band Equalizer millibel offsets for hardware audio effects.
     */
    fun calculate5BandOffsets(tiltDb: Float, pivotHz: Float): List<Int> {
        if (abs(tiltDb) < 1e-4f) return List(5) { 0 }
        return STANDARD_5_BAND_FREQS.map { freq ->
            val gainDb = calculateTiltGainAtFrequency(freq, tiltDb, pivotHz)
            (gainDb * 100.0f).roundToInt().coerceIn(-1200, 1200)
        }
    }

    /**
     * Categorizes the tonal character into rich audiophile descriptors.
     */
    fun evaluateTonalBalanceDescription(tiltDb: Float): String {
        return when {
            tiltDb <= -4.5f -> "Heavy Vintage Warmth"
            tiltDb <= -2.0f -> "Warm Analog Tube"
            tiltDb <= -0.5f -> "Gentle Warmth"
            tiltDb < 0.5f -> "Neutral Reference"
            tiltDb < 2.0f -> "Subtle Modern Air"
            tiltDb < 4.5f -> "Crisp Detail Sheen"
            else -> "Ultra-Airy Highs"
        }
    }

    /**
     * Calculates the apparent RMS energy deviation across standard octave bands
     * to enable equal-loudness volume compensation.
     */
    fun calculateApparentLoudnessOffsetDb(tiltDb: Float, pivotHz: Float): Float {
        if (abs(tiltDb) < 1e-3f) return 0.0f
        var powerSum = 0.0
        for (f in TEST_OCTAVES) {
            val g = calculateTiltGainAtFrequency(f, tiltDb, pivotHz)
            powerSum += 10.0.pow(g.toDouble() / 10.0)
        }
        val avgPower = powerSum / TEST_OCTAVES.size.toDouble()
        val offsetDb = (10.0 * log10(avgPower)).toFloat()
        return offsetDb
    }

    /**
     * Synthesizes comprehensive spectral metrics for display and UI visualization.
     */
    fun calculateSpectralMetrics(config: TiltEqualizerConfig): TiltSpectralMetrics {
        val bassGain = calculateTiltGainAtFrequency(100.0f, config.tiltDb, config.pivotHz)
        val trebleGain = calculateTiltGainAtFrequency(10000.0f, config.tiltDb, config.pivotHz)
        val loudnessOffset = calculateApparentLoudnessOffsetDb(config.tiltDb, config.pivotHz)
        val desc = evaluateTonalBalanceDescription(config.tiltDb)
        return TiltSpectralMetrics(
            bassGainDb = bassGain,
            trebleGainDb = trebleGain,
            pivotHz = config.pivotHz,
            apparentLoudnessOffsetDb = loudnessOffset,
            tonalBalanceDescription = desc
        )
    }

    /**
     * Calculates unconditionally stable first-order shelving biquad coefficients for real-time DSP.
     */
    fun calculateBiquadCoefficients(tiltDb: Float, pivotHz: Float, sampleRate: Int = 44100): TiltBiquadCoefficients {
        if (abs(tiltDb) < 1e-4f) {
            return TiltBiquadCoefficients(b0 = 1.0f, b1 = 0.0f, b2 = 0.0f, a1 = 0.0f, a2 = 0.0f)
        }

        val omega0 = 2.0 * Math.PI * pivotHz.toDouble()
        val omegaZ = omega0 * 10.0.pow(-tiltDb.toDouble() / 20.0)
        val omegaP = omega0 * 10.0.pow(+tiltDb.toDouble() / 20.0)
        val k = 2.0 * sampleRate.toDouble()

        val scale = 10.0.pow(tiltDb.toDouble() / 20.0)
        val a0 = k + omegaP

        val b0 = (scale * (k + omegaZ) / a0).toFloat()
        val b1 = (scale * (omegaZ - k) / a0).toFloat()
        val a1 = ((omegaP - k) / a0).toFloat()

        return TiltBiquadCoefficients(b0 = b0, b1 = b1, b2 = 0.0f, a1 = a1, a2 = 0.0f)
    }

    class TiltFilterState(
        var x1L: Float = 0.0f,
        var y1L: Float = 0.0f,
        var x1R: Float = 0.0f,
        var y1R: Float = 0.0f
    ) {
        fun reset() {
            x1L = 0.0f
            y1L = 0.0f
            x1R = 0.0f
            y1R = 0.0f
        }
    }

    /**
     * Processes a single stereo frame (left, right) through the tilt equalizer DSP filter.
     */
    fun processFrame(
        left: Float,
        right: Float,
        config: TiltEqualizerConfig,
        state: TiltFilterState,
        sampleRate: Int = 44100
    ): Pair<Float, Float> {
        if (!config.isEnabled || abs(config.tiltDb) < 1e-4f) {
            return Pair(left, right)
        }

        val coeffs = calculateBiquadCoefficients(config.tiltDb, config.pivotHz, sampleRate)
        val compGain = if (config.loudnessCompensate) {
            val offset = calculateApparentLoudnessOffsetDb(config.tiltDb, config.pivotHz)
            10.0.pow(-offset.toDouble() / 20.0).toFloat()
        } else {
            1.0f
        }

        val outL = (coeffs.b0 * left + coeffs.b1 * state.x1L - coeffs.a1 * state.y1L) * compGain
        state.x1L = left
        state.y1L = outL

        val outR = (coeffs.b0 * right + coeffs.b1 * state.x1R - coeffs.a1 * state.y1R) * compGain
        state.x1R = right
        state.y1R = outR

        return Pair(outL.coerceIn(-1.0f, 1.0f), outR.coerceIn(-1.0f, 1.0f))
    }
}

