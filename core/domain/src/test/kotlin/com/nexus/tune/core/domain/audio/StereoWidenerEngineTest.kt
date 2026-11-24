package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.StereoWidthMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StereoWidenerEngineTest {

    @Test
    fun `calculateGains returns correct neutral mid and side for 100 percent width`() {
        val (mid, side) = StereoWidenerEngine.calculateGains(100, 0f, 0f)
        assertEquals(1.0f, mid, 0.01f)
        assertEquals(1.0f, side, 0.01f)
    }

    @Test
    fun `calculateGains zeroes out side channel for mono mode`() {
        val (mid, side) = StereoWidenerEngine.calculateGains(0, 0f, 0f)
        assertEquals(1.0f, mid, 0.01f)
        assertEquals(0.0f, side, 0.001f)
    }

    @Test
    fun `calculateGains boosts side gain for 175 percent super wide`() {
        val (mid, side) = StereoWidenerEngine.calculateGains(175, 0f, 0f)
        assertEquals(1.0f, mid, 0.01f)
        assertEquals(1.75f, side, 0.01f)
    }

    @Test
    fun `reconstructStereo collapses to identical mono when side gain is 0`() {
        val (leftOut, rightOut) = StereoWidenerEngine.reconstructStereo(
            left = 0.8f,
            right = 0.2f,
            midGain = 1.0f,
            sideGain = 0.0f
        )
        // Mid = (0.8 + 0.2) * 0.5 = 0.5
        // Side = (0.8 - 0.2) * 0.5 * 0 = 0
        // LeftOut = 0.5, RightOut = 0.5
        assertEquals(0.5f, leftOut, 0.001f)
        assertEquals(0.5f, rightOut, 0.001f)
    }

    @Test
    fun `reconstructStereo preserves original stereo when mid and side are 1`() {
        val (leftOut, rightOut) = StereoWidenerEngine.reconstructStereo(
            left = 0.7f,
            right = -0.3f,
            midGain = 1.0f,
            sideGain = 1.0f
        )
        assertEquals(0.7f, leftOut, 0.001f)
        assertEquals(-0.3f, rightOut, 0.001f)
    }

    @Test
    fun `calculatePhaseCorrelation detects pure mono identical signals`() {
        val left = floatArrayOf(0.1f, 0.5f, 0.9f, 0.2f, -0.4f)
        val right = floatArrayOf(0.1f, 0.5f, 0.9f, 0.2f, -0.4f)
        val corr = StereoWidenerEngine.calculatePhaseCorrelation(left, right)
        assertEquals(1.0f, corr, 0.001f)

        val rating = StereoWidenerEngine.evaluatePhaseRating(corr)
        assertTrue(rating.isPhaseSafe)
        assertTrue(rating.rating.contains("Monophonic"))
    }

    @Test
    fun `calculatePhaseCorrelation detects inverted out-of-phase signals`() {
        val left = floatArrayOf(0.2f, 0.6f, 0.8f, -0.5f)
        val right = floatArrayOf(-0.2f, -0.6f, -0.8f, 0.5f)
        val corr = StereoWidenerEngine.calculatePhaseCorrelation(left, right)
        assertEquals(-1.0f, corr, 0.001f)

        val rating = StereoWidenerEngine.evaluatePhaseRating(corr)
        assertFalse(rating.isPhaseSafe)
        assertTrue(rating.rating.contains("Phase Cancellation Alert"))
    }

    @Test
    fun `calculatePhaseCorrelation handles silence gracefully`() {
        val left = floatArrayOf(0f, 0f, 0f)
        val right = floatArrayOf(0f, 0f, 0f)
        val corr = StereoWidenerEngine.calculatePhaseCorrelation(left, right)
        assertEquals(1.0f, corr, 0.001f)
    }

    @Test
    fun `calculateHaasDelaySamples calculates exact sample offsets`() {
        // At 44.1kHz, 10ms = 441 samples
        val samples = StereoWidenerEngine.calculateHaasDelaySamples(10.0f, 44100)
        assertEquals(441, samples)

        // At 48kHz, 5ms = 240 samples
        val samples48k = StereoWidenerEngine.calculateHaasDelaySamples(5.0f, 48000)
        assertEquals(240, samples48k)
    }

    @Test
    fun `mapWidthToVirtualizerStrength maps bounds correctly`() {
        assertEquals(0.toShort(), StereoWidenerEngine.mapWidthToVirtualizerStrength(0))
        assertEquals(500.toShort(), StereoWidenerEngine.mapWidthToVirtualizerStrength(100))
        assertEquals(1000.toShort(), StereoWidenerEngine.mapWidthToVirtualizerStrength(200))
    }

    @Test
    fun `getPresetConfig returns valid mode configurations`() {
        val superWide = StereoWidenerEngine.getPresetConfig(StereoWidthMode.SUPER_WIDE)
        assertEquals(175, superWide.customWidthPercent)
        assertEquals(12.0f, superWide.haasDelayMs, 0.01f)

        val mono = StereoWidenerEngine.getPresetConfig(StereoWidthMode.MONO)
        assertEquals(0, mono.customWidthPercent)
        assertEquals(-12f, mono.sideLevelDb, 0.01f)
    }
}

