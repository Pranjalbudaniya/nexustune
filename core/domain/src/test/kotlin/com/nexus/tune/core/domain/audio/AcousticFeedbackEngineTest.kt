package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.AcousticFeedbackConfig
import com.nexus.tune.core.model.AcousticFeedbackPreset
import com.nexus.tune.core.model.FeedbackHuntMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class AcousticFeedbackEngineTest {

    private lateinit var engine: AcousticFeedbackEngine
    private val sampleRate = 48000

    @Before
    fun setUp() {
        engine = AcousticFeedbackEngine(sampleRate)
    }

    @Test
    fun `bypassed configuration leaves audio unaltered`() {
        val config = AcousticFeedbackConfig(isEnabled = false)
        val original = FloatArray(512) { i -> sin(2.0 * PI * 1000.0 * i / sampleRate).toFloat() * 0.5f }
        val buffer = original.clone()

        val metrics = engine.process(buffer, config)

        for (i in buffer.indices) {
            assertEquals(original[i], buffer[i], 0.00001f)
        }
        assertEquals(0.0f, metrics.totalSuppressionDb, 0.001f)
        assertEquals(0, metrics.activeNotchCount)
    }

    @Test
    fun `findResonantFrequency identifies dominant ringing tone`() {
        val numSamples = 2048
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 2400.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        val detectedFreq = engine.findResonantFrequency(buffer, FeedbackHuntMode.SURGICAL_AUTO)
        assertEquals(2400.0f, detectedFreq, 1.0f)
    }

    @Test
    fun `acoustic feedback ringing is attenuated significantly`() {
        val config = AcousticFeedbackConfig(
            isEnabled = true,
            preset = AcousticFeedbackPreset.LIVE_CONCERT_DE_SQUEAL,
            mode = FeedbackHuntMode.SURGICAL_AUTO,
            activeNotchesCount = 2,
            notchSharpnessQ = 40.0f,
            suppressionDepthDb = -24.0f,
            fastPanicClamp = false
        )

        // Generate 2400 Hz squeal
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 2400.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        engine.reset()
        val metrics = engine.process(buffer, config)

        // Settle check
        val tailStart = numSamples / 2
        var sumSquares = 0.0
        for (i in tailStart until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / (numSamples - tailStart))

        assertTrue("Ringing should be attenuated: rmsOut=$rmsOut", rmsOut < 0.15)
        assertTrue(metrics.totalSuppressionDb >= 24.0f)
    }

    @Test
    fun `emergency panic clamp limits runaway bursts`() {
        val config = AcousticFeedbackConfig(
            isEnabled = true,
            preset = AcousticFeedbackPreset.HIGH_GAIN_PANIC_SHIELD,
            fastPanicClamp = true
        )

        // Over-driven runaway burst at 1.5 amplitude
        val numSamples = 1024
        val buffer = FloatArray(numSamples) { 1.5f }

        engine.reset()
        engine.process(buffer, config)

        val maxVal = buffer.maxOf { abs(it) }
        assertTrue("Burst should be limited to safety ceiling: maxVal=$maxVal", maxVal <= 0.95f)
    }

    @Test
    fun `hardware eq compensation reflects feedback hunting mode`() {
        val disabled = AcousticFeedbackConfig(isEnabled = false)
        assertEquals(0, engine.calculateHardwareEqCompensation(disabled))

        val concert = AcousticFeedbackConfig(
            isEnabled = true,
            preset = AcousticFeedbackPreset.LIVE_CONCERT_DE_SQUEAL,
            mode = FeedbackHuntMode.LIVE_CONCERT
        )
        assertEquals(-120, engine.calculateHardwareEqCompensation(concert))

        val speaker = AcousticFeedbackConfig(
            isEnabled = true,
            preset = AcousticFeedbackPreset.SPEAKER_ROOM_RESONANCE,
            mode = FeedbackHuntMode.SPEAKER_BOUNDARY
        )
        assertEquals(-80, engine.calculateHardwareEqCompensation(speaker))
    }

    @Test
    fun `calculateMetrics companion helper returns valid status`() {
        val activeConfig = AcousticFeedbackConfig(
            isEnabled = true,
            preset = AcousticFeedbackPreset.STUDIO_TRANSPARENT,
            mode = FeedbackHuntMode.SURGICAL_AUTO,
            suppressionDepthDb = -18.0f
        )
        val metrics = AcousticFeedbackEngine.calculateMetrics(activeConfig)
        assertEquals(2400.0f, metrics.detectedRingingFrequencyHz, 0.1f)
        assertEquals(18.0f, metrics.totalSuppressionDb, 0.1f)
        assertTrue(metrics.safetyStatus.contains("Locked"))
    }
}

