package com.nexus.tune.core.domain.ambient

import com.nexus.tune.core.model.AmbientSoundType
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural DSP audio engine for real-time synthesis of ambient soundscapes,
 * noise colors (Brown, Pink, White), and binaural brainwave frequencies.
 * Produces stereo 16-bit interleaved PCM samples at 44.1 kHz.
 */
class AmbientSoundGenerator(seed: Long? = null) {

    private val random = if (seed != null) Random(seed) else Random()

    // Pink noise IIR filter states (Paul Kellet's filter algorithm)
    private var pinkB0L = 0f
    private var pinkB1L = 0f
    private var pinkB2L = 0f
    private var pinkB0R = 0f
    private var pinkB1R = 0f
    private var pinkB2R = 0f

    // Brown noise states (integrated Brownian walk with leakage)
    private var brownLastL = 0f
    private var brownLastR = 0f

    // Binaural and pure sine wave continuous phase accumulators
    private var phaseL = 0.0
    private var phaseR = 0.0

    /**
     * Resets filter states and phase accumulators to prevent DC offset or pops
     * when switching sound profiles.
     */
    fun reset() {
        pinkB0L = 0f
        pinkB1L = 0f
        pinkB2L = 0f
        pinkB0R = 0f
        pinkB1R = 0f
        pinkB2R = 0f
        brownLastL = 0f
        brownLastR = 0f
        phaseL = 0.0
        phaseR = 0.0
    }

    /**
     * Generates an interleaved stereo [ShortArray] of [numFrames] audio frames.
     * Returned array size is exactly [numFrames] * 2.
     */
    fun generateStereoChunk(
        type: AmbientSoundType,
        numFrames: Int,
        sampleRate: Int = 44100
    ): ShortArray {
        val buffer = ShortArray(numFrames * 2)
        if (type == AmbientSoundType.OFF || numFrames <= 0) {
            return buffer
        }

        when (type) {
            AmbientSoundType.BROWN_NOISE -> generateBrownNoise(buffer, numFrames)
            AmbientSoundType.PINK_NOISE -> generatePinkNoise(buffer, numFrames)
            AmbientSoundType.WHITE_NOISE -> generateWhiteNoise(buffer, numFrames)
            AmbientSoundType.BINAURAL_ALPHA -> generateBinauralBeat(buffer, numFrames, 216.0, 226.0, sampleRate)
            AmbientSoundType.BINAURAL_THETA -> generateBinauralBeat(buffer, numFrames, 197.0, 203.0, sampleRate)
            AmbientSoundType.BINAURAL_DELTA -> generateBinauralBeat(buffer, numFrames, 148.75, 151.25, sampleRate)
            AmbientSoundType.SOLFEGGIO_432HZ -> generateSineWave(buffer, numFrames, 432.0, sampleRate)
            AmbientSoundType.SOLFEGGIO_528HZ -> generateSineWave(buffer, numFrames, 528.0, sampleRate)
            AmbientSoundType.OFF -> { /* already zeroed */ }
        }

        return buffer
    }

    private fun generateBrownNoise(buffer: ShortArray, numFrames: Int) {
        var idx = 0
        for (i in 0 until numFrames) {
            val whiteL = (random.nextFloat() * 2f - 1f)
            val whiteR = (random.nextFloat() * 2f - 1f)

            brownLastL = (brownLastL * 0.985f) + (whiteL * 0.06f)
            brownLastR = (brownLastR * 0.985f) + (whiteR * 0.06f)

            val outL = (brownLastL.coerceIn(-1.0f, 1.0f) * 0.55f * 32767).toInt().toShort()
            val outR = (brownLastR.coerceIn(-1.0f, 1.0f) * 0.55f * 32767).toInt().toShort()

            buffer[idx++] = outL
            buffer[idx++] = outR
        }
    }

    private fun generatePinkNoise(buffer: ShortArray, numFrames: Int) {
        var idx = 0
        for (i in 0 until numFrames) {
            val whiteL = (random.nextFloat() * 2f - 1f)
            val whiteR = (random.nextFloat() * 2f - 1f)

            pinkB0L = 0.99886f * pinkB0L + whiteL * 0.0555179f
            pinkB1L = 0.99332f * pinkB1L + whiteL * 0.0750759f
            pinkB2L = 0.96900f * pinkB2L + whiteL * 0.1538520f
            val pinkL = (pinkB0L + pinkB1L + pinkB2L + whiteL * 0.5362f) * 0.16f

            pinkB0R = 0.99886f * pinkB0R + whiteR * 0.0555179f
            pinkB1R = 0.99332f * pinkB1R + whiteR * 0.0750759f
            pinkB2R = 0.96900f * pinkB2R + whiteR * 0.1538520f
            val pinkR = (pinkB0R + pinkB1R + pinkB2R + whiteR * 0.5362f) * 0.16f

            val outL = (pinkL.coerceIn(-1.0f, 1.0f) * 32767).toInt().toShort()
            val outR = (pinkR.coerceIn(-1.0f, 1.0f) * 32767).toInt().toShort()

            buffer[idx++] = outL
            buffer[idx++] = outR
        }
    }

    private fun generateWhiteNoise(buffer: ShortArray, numFrames: Int) {
        var idx = 0
        for (i in 0 until numFrames) {
            val whiteL = (random.nextFloat() * 2f - 1f) * 0.35f
            val whiteR = (random.nextFloat() * 2f - 1f) * 0.35f

            buffer[idx++] = (whiteL * 32767).toInt().toShort()
            buffer[idx++] = (whiteR * 32767).toInt().toShort()
        }
    }

    private fun generateBinauralBeat(
        buffer: ShortArray,
        numFrames: Int,
        freqL: Double,
        freqR: Double,
        sampleRate: Int
    ) {
        val deltaL = 2.0 * PI * freqL / sampleRate
        val deltaR = 2.0 * PI * freqR / sampleRate
        val twoPi = 2.0 * PI

        var idx = 0
        for (i in 0 until numFrames) {
            phaseL += deltaL
            if (phaseL >= twoPi) phaseL -= twoPi

            phaseR += deltaR
            if (phaseR >= twoPi) phaseR -= twoPi

            val outL = (sin(phaseL) * 0.45 * 32767).toInt().toShort()
            val outR = (sin(phaseR) * 0.45 * 32767).toInt().toShort()

            buffer[idx++] = outL
            buffer[idx++] = outR
        }
    }

    private fun generateSineWave(
        buffer: ShortArray,
        numFrames: Int,
        freq: Double,
        sampleRate: Int
    ) {
        val delta = 2.0 * PI * freq / sampleRate
        val twoPi = 2.0 * PI

        var idx = 0
        for (i in 0 until numFrames) {
            phaseL += delta
            if (phaseL >= twoPi) phaseL -= twoPi

            val sample = (sin(phaseL) * 0.45 * 32767).toInt().toShort()
            buffer[idx++] = sample
            buffer[idx++] = sample
        }
    }
}

