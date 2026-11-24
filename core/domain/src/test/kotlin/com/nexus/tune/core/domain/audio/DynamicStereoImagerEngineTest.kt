package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class DynamicStereoImagerEngineTest {

    @Test
    fun `bypassed when disabled returns identical input sample pair`() {
        val config = DynamicStereoImagerConfig(isEnabled = false)
        val (l, r) = DynamicStereoImagerEngine.processStereoSample(0.6f, -0.4f, config)
        assertEquals(0.6f, l, 1e-6f)
        assertEquals(-0.4f, r, 1e-6f)
    }

    @Test
    fun `bypassed when preset is off returns identical input sample pair`() {
        val config = DynamicStereoImagerConfig(isEnabled = true, preset = StereoImagerPreset.OFF)
        val (l, r) = DynamicStereoImagerEngine.processStereoSample(-0.3f, 0.7f, config)
        assertEquals(-0.3f, l, 1e-6f)
        assertEquals(0.7f, r, 1e-6f)
    }

    @Test
    fun `mono width setting collapses stereo difference to pure mono`() {
        val config = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.MONO_BASS_FOCUS,
            stereoWidth = 0.0f
        )

        val left = 0.8f
        val right = -0.2f
        val (lOut, rOut) = DynamicStereoImagerEngine.processStereoSample(left, right, config)

        // At width=0 (pure mono), L and R must be identical (Sum / 2)
        assertEquals(lOut, rOut, 1e-4f)
        assertEquals((left + right) / 2.0f, lOut, 1e-4f)
    }

    @Test
    fun `stereo widening expands channel separation`() {
        val normalConfig = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.WIDE_CONCERT_HALL,
            stereoWidth = 1.0f,
            monoBassCutoffHz = 0.0f
        )
        val wideConfig = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.WIDE_CONCERT_HALL,
            stereoWidth = 1.6f,
            monoBassCutoffHz = 0.0f
        )

        val left = 0.6f
        val right = -0.2f

        val (normalL, normalR) = DynamicStereoImagerEngine.processStereoSample(left, right, normalConfig)
        val (wideL, wideR) = DynamicStereoImagerEngine.processStereoSample(left, right, wideConfig)

        val normalDiff = abs(normalL - normalR)
        val wideDiff = abs(wideL - wideR)

        assertTrue(
            "Widening stereo width must increase L-R difference (channel separation)",
            wideDiff > normalDiff
        )
    }

    @Test
    fun `mono bass highpass filter collapses low frequencies to mono while keeping highs wide`() {
        val config = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.MONO_BASS_FOCUS,
            stereoWidth = 1.5f,
            monoBassCutoffHz = 160.0f
        )
        val state = DynamicStereoImagerEngine.StereoImagerState(sampleRate = 48000)

        // Simulate 40 Hz low bass tone in stereo anti-phase (out of phase bass)
        val sampleRate = 48000.0
        val freqLow = 40.0
        var maxDiffLow = 0.0f

        for (i in 0 until 500) {
            val angle = 2.0 * Math.PI * freqLow * i / sampleRate
            val l = (0.5 * sin(angle)).toFloat()
            val r = (-0.5 * sin(angle)).toFloat() // out of phase
            val (outL, outR) = DynamicStereoImagerEngine.processStereoSample(l, r, config, state)
            if (i > 300) {
                maxDiffLow = maxOf(maxDiffLow, abs(outL - outR))
            }
        }

        // Bass difference should be heavily attenuated (collapsed toward mono)
        assertTrue("Mono-bass highpass must attenuate low frequency out-of-phase side signal", maxDiffLow < 0.35f)
    }

    @Test
    fun `mono guard prevents destructive phase cancellation when correlation is low`() {
        val state = DynamicStereoImagerEngine.StereoImagerState()
        // Feed negative correlation (anti-phase) signals
        for (i in 0 until 600) {
            state.updateCorrelation(0.8f, -0.8f)
        }

        assertTrue("Anti-phase signals must yield negative correlation", state.currentCorrelation < 0.0f)
        assertTrue("Mono guard must engage and attenuate side gain (gain < 1.0)", state.monoGuardGain < 0.8f)
    }

    @Test
    fun `hardware spatial offset scales with stereo width`() {
        val disabledConfig = DynamicStereoImagerConfig(isEnabled = false)
        assertEquals(0.toShort(), DynamicStereoImagerEngine.calculateHardwareSpatialOffset(disabledConfig))

        val wideConfig = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.WIDE_CONCERT_HALL,
            stereoWidth = 1.5f
        )
        val strength = DynamicStereoImagerEngine.calculateHardwareSpatialOffset(wideConfig)
        assertTrue("Widened strength must be > 0", strength > 0)
        assertTrue("Widened strength must be <= 1000", strength <= 1000)
    }

    @Test
    fun `metrics calculation reflects width and mono compatibility score`() {
        val bypassed = DynamicStereoImagerConfig(isEnabled = false)
        val bypassedMetrics = DynamicStereoImagerEngine.calculateMetrics(bypassed)
        assertEquals(100, bypassedMetrics.perceivedWidthPercent)
        assertEquals(100, bypassedMetrics.monoCompatibilityScore)
        assertTrue(bypassedMetrics.spatialDepthRating.contains("Bypassed"))

        val active = DynamicStereoImagerConfig(
            isEnabled = true,
            preset = StereoImagerPreset.WIDE_CONCERT_HALL,
            stereoWidth = 1.4f
        )
        val activeMetrics = DynamicStereoImagerEngine.calculateMetrics(active)
        assertEquals(140, activeMetrics.perceivedWidthPercent)
        assertTrue("Active compatibility score should be high", activeMetrics.monoCompatibilityScore >= 70)
    }
}

