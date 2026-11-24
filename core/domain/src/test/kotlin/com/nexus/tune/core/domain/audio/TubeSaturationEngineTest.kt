package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class TubeSaturationEngineTest {

    @Test
    fun `bypassed when disabled returns identical input sample`() {
        val config = TubeSaturationConfig(isEnabled = false)
        val sample = 0.654f
        val result = TubeSaturationEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `bypassed when profile is off returns identical input sample`() {
        val config = TubeSaturationConfig(isEnabled = true, profile = TubeWarmthProfile.OFF)
        val sample = -0.432f
        val result = TubeSaturationEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `triode topology produces asymmetric transfer curve with even harmonics`() {
        val config = TubeSaturationConfig(
            isEnabled = true,
            profile = TubeWarmthProfile.LUSH_TRIODE_WARMTH,
            topology = TubeTopology.TRIODE_12AX7,
            drive = 0.5f,
            bias = 0.25f,
            autoMakeupGain = false
        )

        val positiveSample = 0.6f
        val negativeSample = -0.6f

        val posResult = TubeSaturationEngine.processSample(positiveSample, config)
        val negResult = TubeSaturationEngine.processSample(negativeSample, config)

        // Asymmetric transfer means |posResult| != |negResult|
        assertNotEquals(abs(posResult), abs(negResult), 0.01f)

        val metrics = TubeSaturationEngine.calculateMetrics(config)
        assertTrue("Second harmonic should be audible and strong (> -50 dB)", metrics.secondHarmonicDb > -50.0f)
        assertTrue("Warmth score should be elevated", metrics.warmthScore >= 30)
    }

    @Test
    fun `pentode topology creates punchy third harmonic saturation`() {
        val config = TubeSaturationConfig(
            isEnabled = true,
            profile = TubeWarmthProfile.VINTAGE_TUBE_HOT,
            topology = TubeTopology.PENTODE_EL34,
            drive = 0.6f,
            bias = 0.0f,
            autoMakeupGain = true
        )

        val metrics = TubeSaturationEngine.calculateMetrics(config)
        assertTrue("Pentode should generate prominent 3rd harmonic (> -40 dB)", metrics.thirdHarmonicDb > -40.0f)
        assertTrue("Total harmonic distortion should be > 1.0%", metrics.totalHarmonicDistortionPercent > 1.0f)
    }

    @Test
    fun `auto makeup gain attenuates high drive signal to protect dynamic headroom`() {
        val makeupGainAtLowDrive = TubeSaturationEngine.calculateAutoMakeupGain(0.1f)
        val makeupGainAtHighDrive = TubeSaturationEngine.calculateAutoMakeupGain(0.8f)

        assertTrue(
            "High drive makeup gain must be lower than low drive makeup gain",
            makeupGainAtHighDrive < makeupGainAtLowDrive
        )
        assertTrue("Makeup gain at 0 drive should be 1.0", abs(TubeSaturationEngine.calculateAutoMakeupGain(0.0f) - 1.0f) < 1e-4f)
    }

    @Test
    fun `dc filter removes accumulated dc drift`() {
        val filter = TubeSaturationEngine.DcFilterState(r = 0.999f)
        // Feed constant DC signal of 0.8
        var out = 0.0f
        for (i in 0 until 5000) {
            out = filter.filter(0.8f)
        }
        // After settling, DC should be close to 0.0
        assertTrue("DC filter must converge output to near 0 for static DC offset", abs(out) < 0.01f)
    }

    @Test
    fun `processBuffer transforms samples accurately with soft ceiling`() {
        val config = TubeSaturationConfig(
            isEnabled = true,
            profile = TubeWarmthProfile.LUSH_TRIODE_WARMTH,
            topology = TubeTopology.TRIODE_12AX7,
            drive = 0.9f
        )
        val input = floatArrayOf(-1.0f, -0.5f, 0.0f, 0.5f, 1.0f)
        val output = TubeSaturationEngine.processBuffer(input, config)

        assertEquals(input.size, output.size)
        for (sample in output) {
            assertTrue("Output must stay within safe audio boundaries [-1.0, 1.0]", sample in -1.0f..1.0f)
        }
    }

    @Test
    fun `hardware warmth offset scales with drive and profile`() {
        val disabledConfig = TubeSaturationConfig(isEnabled = false)
        assertEquals(0, TubeSaturationEngine.calculateHardwareWarmthOffset(disabledConfig))

        val activeConfig = TubeSaturationConfig(
            isEnabled = true,
            profile = TubeWarmthProfile.LUSH_TRIODE_WARMTH,
            topology = TubeTopology.TRIODE_12AX7,
            drive = 0.5f
        )
        val offset = TubeSaturationEngine.calculateHardwareWarmthOffset(activeConfig)
        assertTrue("Active tube warmth offset must be > 0 mB", offset > 0)
        assertTrue("Active tube warmth offset must be <= 300 mB", offset <= 300)
    }

    @Test
    fun `metrics calculation handles bypassed state gracefully`() {
        val config = TubeSaturationConfig(isEnabled = false)
        val metrics = TubeSaturationEngine.calculateMetrics(config)

        assertEquals(-96.0f, metrics.secondHarmonicDb, 1e-3f)
        assertEquals(-96.0f, metrics.thirdHarmonicDb, 1e-3f)
        assertEquals(0.0f, metrics.totalHarmonicDistortionPercent, 1e-3f)
        assertEquals(0, metrics.warmthScore)
        assertEquals(0.0f, metrics.headroomCompensationDb, 1e-3f)
        assertTrue(metrics.characterRating.contains("Bypassed"))
    }
}

