package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.CrosstalkCancellationConfig
import com.nexus.tune.core.model.CrosstalkCancellationMode
import org.junit.Assert.*
import org.junit.Test

class CrosstalkCancellationEngineTest {

    @Test
    fun `calculateDelayMicroseconds computes accurate physical propagation delays`() {
        assertEquals(0.0f, CrosstalkCancellationEngine.calculateDelayMicroseconds(0, 44100), 1e-4f)

        // 5 samples at 44.1kHz is approx 113.38 us
        val delay5 = CrosstalkCancellationEngine.calculateDelayMicroseconds(5, 44100)
        assertEquals(113.38f, delay5, 0.5f)

        // 14 samples at 48kHz is approx 291.67 us
        val delay14 = CrosstalkCancellationEngine.calculateDelayMicroseconds(14, 48000)
        assertEquals(291.67f, delay14, 0.5f)
    }

    @Test
    fun `calculateVirtualStageSpan expands acoustic arc based on geometry and intensity`() {
        assertEquals(30, CrosstalkCancellationEngine.calculateVirtualStageSpan(CrosstalkCancellationMode.OFF, 100))

        val phoneSpan = CrosstalkCancellationEngine.calculateVirtualStageSpan(CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS, 100)
        assertEquals(140, phoneSpan)

        val soundbarSpan = CrosstalkCancellationEngine.calculateVirtualStageSpan(CrosstalkCancellationMode.SOUNDBAR_LIVING_ROOM, 100)
        assertEquals(175, soundbarSpan)

        val halfIntensity = CrosstalkCancellationEngine.calculateVirtualStageSpan(CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS, 50)
        assertTrue("Half intensity should yield intermediate expansion", halfIntensity in 80..130)
    }

    @Test
    fun `calculateMetrics produces accurate descriptive status`() {
        val config = CrosstalkCancellationConfig(
            isEnabled = true,
            mode = CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS,
            delaySamples = 5,
            cancellationIntensityPercent = 80
        )

        val metrics = CrosstalkCancellationEngine.calculateMetrics(config)
        assertTrue(metrics.delayMicroseconds > 100.0f)
        assertTrue(metrics.virtualStageSpanDegrees in 110..140)
        assertTrue(metrics.effectiveCancellationDb < 0.0f)
        assertTrue(metrics.binauralRating.contains("Panoramic") || metrics.binauralRating.contains("Expanded"))
    }

    @Test
    fun `processFrame injects inverted contralateral delayed acoustic cancellation`() {
        val config = CrosstalkCancellationConfig(
            isEnabled = true,
            mode = CrosstalkCancellationMode.PHONE_STEREO_SPEAKERS,
            delaySamples = 2,
            cancellationIntensityPercent = 100,
            headShadowDamping = 0.5f
        )
        val state = CrosstalkCancellationEngine.XtcState(bufferSize = 16)

        // Prime the delay line with right-channel impulses
        CrosstalkCancellationEngine.processFrame(0.0f, 0.8f, config, state)
        CrosstalkCancellationEngine.processFrame(0.0f, 0.8f, config, state)

        // On subsequent sample, the left channel should receive an inverted cancellation component
        val (outL, _) = CrosstalkCancellationEngine.processFrame(0.0f, 0.0f, config, state)
        assertTrue("Left channel must receive inverted cancellation component", outL < -0.05f)
    }

    @Test
    fun `bypassed config leaves audio completely unchanged`() {
        val config = CrosstalkCancellationConfig(isEnabled = false, mode = CrosstalkCancellationMode.OFF)
        val state = CrosstalkCancellationEngine.XtcState()

        val (outL, outR) = CrosstalkCancellationEngine.processFrame(0.7f, -0.3f, config, state)
        assertEquals(0.7f, outL, 1e-4f)
        assertEquals(-0.3f, outR, 1e-4f)
    }
}

