package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.SilenceTrimConfig
import com.nexus.tune.core.model.SilenceTrimMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class SilenceTrimmerEngineTest {

    @Test
    fun testCalculateRmsDbSilentBuffer() {
        val zeros = FloatArray(256) { 0f }
        val rms = SilenceTrimmerEngine.calculateRmsDb(zeros)
        assertEquals(-96.0f, rms, 0.001f)
    }

    @Test
    fun testCalculateRmsDbFullScaleSquare() {
        val square = FloatArray(256) { 1.0f }
        val rms = SilenceTrimmerEngine.calculateRmsDb(square)
        assertEquals(0.0f, rms, 0.001f)
    }

    @Test
    fun testCalculateRmsDbSineWave() {
        // RMS of pure sine wave is 1/sqrt(2) = 0.7071 -> 20 * log10(0.7071) = -3.01 dBFS
        val sine = FloatArray(1000) { i ->
            sin(2.0 * Math.PI * 440.0 * (i.toDouble() / 44100.0)).toFloat()
        }
        val rms = SilenceTrimmerEngine.calculateRmsDb(sine)
        assertEquals(-3.01f, rms, 0.1f)
    }

    @Test
    fun testDetectSilenceWhenDisabled() {
        val config = SilenceTrimConfig(isEnabled = false)
        val res = SilenceTrimmerEngine.detectSilence(-80f, 2000L, 100L, config)
        assertFalse(res.isSilent)
        assertFalse(res.shouldSkip)
    }

    @Test
    fun testDetectSilenceAccumulatesAndSkips() {
        val config = SilenceTrimConfig(
            isEnabled = true,
            mode = SilenceTrimMode.FAST_TALK,
            thresholdDb = -36.0f,
            minDurationMs = 350L
        )

        // Frame 1: 100ms silence (< 350ms, should not skip yet)
        val res1 = SilenceTrimmerEngine.detectSilence(-50f, 0L, 100L, config)
        assertTrue(res1.isSilent)
        assertEquals(100L, res1.silenceDurationMs)
        assertFalse(res1.shouldSkip)

        // Frame 2: accumulated 300ms silence + 100ms = 400ms (>= 350ms, should skip)
        val res2 = SilenceTrimmerEngine.detectSilence(-50f, 300L, 100L, config)
        assertTrue(res2.isSilent)
        assertEquals(400L, res2.silenceDurationMs)
        assertTrue(res2.shouldSkip)
    }

    @Test
    fun testDetectSilenceSoundResetsDuration() {
        val config = SilenceTrimConfig(
            isEnabled = true,
            mode = SilenceTrimMode.NATURAL_MUSIC,
            thresholdDb = -45.0f
        )
        // Audio burst (-10 dBFS > -45 dBFS) resets accumulated silence to 0
        val res = SilenceTrimmerEngine.detectSilence(-10f, 1000L, 100L, config)
        assertFalse(res.isSilent)
        assertEquals(0L, res.silenceDurationMs)
        assertFalse(res.shouldSkip)
    }

    @Test
    fun testEnvelopeSmoothingFadeInAndOut() {
        val samples = FloatArray(100) { 1.0f }
        val fadeIn = SilenceTrimmerEngine.applyEnvelopeSmoothing(samples, isFadeIn = true, rampLength = 20)
        assertEquals(0.0f, fadeIn[0], 0.001f)
        assertTrue(fadeIn[19] > 0.8f)
        assertEquals(1.0f, fadeIn[50], 0.001f)

        val fadeOut = SilenceTrimmerEngine.applyEnvelopeSmoothing(samples, isFadeIn = false, rampLength = 20)
        assertEquals(0.0f, fadeOut[99], 0.001f)
        assertEquals(1.0f, fadeOut[50], 0.001f)
    }

    @Test
    fun testEstimateTimeSavingsPercent() {
        val savings = SilenceTrimmerEngine.estimateTimeSavingsPercent(30_000L, 120_000L)
        assertEquals(25.0f, savings, 0.001f)
    }

    @Test
    fun testCalculateDynamicThresholdDb() {
        val threshold = SilenceTrimmerEngine.calculateDynamicThresholdDb(-52.0f, SilenceTrimMode.SMART_DYNAMIC)
        assertEquals(-46.0f, threshold, 0.001f)
    }
}

