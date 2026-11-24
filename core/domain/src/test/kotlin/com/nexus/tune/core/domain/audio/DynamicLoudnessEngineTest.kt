package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DynamicLoudnessMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicLoudnessEngineTest {

    @Test
    fun testPerceivedAttenuationFactor() {
        val factorFull = DynamicLoudnessEngine.calculatePerceivedAttenuationFactor(1.0f)
        assertEquals(0.0f, factorFull, 0.001f)

        val factorZero = DynamicLoudnessEngine.calculatePerceivedAttenuationFactor(0.0f)
        assertEquals(1.0f, factorZero, 0.001f)

        val factorHalf = DynamicLoudnessEngine.calculatePerceivedAttenuationFactor(0.5f)
        assertTrue(factorHalf in 0.2f..0.5f)
    }

    @Test
    fun testContourOffsetsAtFullVolumeAreZero() {
        val offsets = DynamicLoudnessEngine.calculateContourOffsets(
            mode = DynamicLoudnessMode.ISO_226_REFERENCE,
            currentVolume = 1.0f,
            intensityPercent = 100
        )
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun testContourOffsetsWhenModeIsOffAreZero() {
        val offsets = DynamicLoudnessEngine.calculateContourOffsets(
            mode = DynamicLoudnessMode.OFF,
            currentVolume = 0.2f,
            intensityPercent = 100
        )
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun testContourOffsetsWhenIntensityIsZeroAreZero() {
        val offsets = DynamicLoudnessEngine.calculateContourOffsets(
            mode = DynamicLoudnessMode.DEEP_PUNCH,
            currentVolume = 0.2f,
            intensityPercent = 0
        )
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun testContourOffsetsAtLowVolume() {
        val offsets = DynamicLoudnessEngine.calculateContourOffsets(
            mode = DynamicLoudnessMode.ISO_226_REFERENCE,
            currentVolume = 0.0f,
            intensityPercent = 100
        )
        // At volume 0, factor = 1.0
        // maxBassBoostDb = 8.0 -> 800 mB
        // midBass = 8.0 * 55 = 440 mB
        // mid = 0
        // presence = -4.5 * 10 = -45 mB
        // treble = 4.5 * 100 = 450 mB
        assertEquals(800, offsets[0])
        assertEquals(440, offsets[1])
        assertEquals(0, offsets[2])
        assertEquals(-45, offsets[3])
        assertEquals(450, offsets[4])
    }

    @Test
    fun testLoudnessCurveMetrics() {
        val curve = DynamicLoudnessEngine.calculateLoudnessCurve(
            mode = DynamicLoudnessMode.NIGHT_WHISPER,
            currentVolume = 0.0f,
            intensityPercent = 100
        )
        assertEquals(12.0f, curve.effectiveBassGainDb, 0.01f)
        assertEquals(7.0f, curve.effectiveTrebleGainDb, 0.01f)
    }

    @Test
    fun testPerceivedBassLossDb() {
        val lossFull = DynamicLoudnessEngine.estimatePerceivedBassLossDb(1.0f)
        assertEquals(0.0f, lossFull, 0.001f)

        val lossLow = DynamicLoudnessEngine.estimatePerceivedBassLossDb(0.1f)
        assertTrue(lossLow > 5.0f)
    }

    @Test
    fun testSafeHeadroomPreAmpDb() {
        val headroom = DynamicLoudnessEngine.calculateSafeHeadroomPreAmpDb(10.0f, 100)
        assertEquals(-4.0f, headroom, 0.01f)
    }
}

