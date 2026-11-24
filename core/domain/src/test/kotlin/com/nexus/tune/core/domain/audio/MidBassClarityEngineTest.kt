package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.MidBassClarityConfig
import com.nexus.tune.core.model.MidBassClarityMode
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class MidBassClarityEngineTest {

    @Test
    fun `calculateCongestionRatio correctly normalizes energy and clamps to unit interval`() {
        assertEquals(0.5f, MidBassClarityEngine.calculateCongestionRatio(50.0f, 100.0f), 1e-4f)
        assertEquals(0.0f, MidBassClarityEngine.calculateCongestionRatio(0.0f, 100.0f), 1e-4f)
        assertEquals(1.0f, MidBassClarityEngine.calculateCongestionRatio(150.0f, 100.0f), 1e-4f)
        assertEquals(0.0f, MidBassClarityEngine.calculateCongestionRatio(0.0f, 0.0f), 1e-4f)
    }

    @Test
    fun `calculateDynamicAttenuationDb remains zero below threshold and scales linearly above`() {
        val maxDip = -4.5f
        val threshold = 0.40f

        // Below threshold: zero dip
        assertEquals(0.0f, MidBassClarityEngine.calculateDynamicAttenuationDb(0.30f, threshold, maxDip), 1e-4f)
        assertEquals(0.0f, MidBassClarityEngine.calculateDynamicAttenuationDb(0.40f, threshold, maxDip), 1e-4f)

        // Halfway above threshold (0.70 is midpoint of 0.40..1.0)
        val midDip = MidBassClarityEngine.calculateDynamicAttenuationDb(0.70f, threshold, maxDip)
        assertEquals(-2.25f, midDip, 0.05f)

        // At maximum congestion (1.0)
        val maxResult = MidBassClarityEngine.calculateDynamicAttenuationDb(1.0f, threshold, maxDip)
        assertEquals(maxDip, maxResult, 1e-4f)
    }

    @Test
    fun `hardware band offset converts negative dB to negative millibels`() {
        assertEquals(0, MidBassClarityEngine.calculateHardwareBandOffset(0.0f))
        assertEquals(-250, MidBassClarityEngine.calculateHardwareBandOffset(-2.5f))
        assertEquals(-450, MidBassClarityEngine.calculateHardwareBandOffset(-4.5f))
        assertEquals(-600, MidBassClarityEngine.calculateHardwareBandOffset(-6.0f))
    }

    @Test
    fun `calculateBiquadDipCoefficients are stable with poles inside unit circle`() {
        val coeffsZero = MidBassClarityEngine.calculateBiquadDipCoefficients(300.0f, 0.0f)
        assertEquals(1.0f, coeffsZero.b0, 1e-4f)
        assertEquals(0.0f, coeffsZero.b1, 1e-4f)

        for (dip in listOf(-2.5f, -4.5f, -6.0f)) {
            val c = MidBassClarityEngine.calculateBiquadDipCoefficients(300.0f, dip, 1.4f, 44100)
            assertTrue("b0 must be positive", c.b0 > 0.0f)
            assertTrue("|a1| must be bounded", abs(c.a1) < 2.0f)
            assertTrue("|a2| must be strictly less than 1.0", abs(c.a2) < 1.0f)
        }
    }

    @Test
    fun `calculateMetrics produces accurate live status descriptions`() {
        val config = MidBassClarityConfig(
            isEnabled = true,
            mode = MidBassClarityMode.BALANCED_DE_MUD,
            dynamicThresholdPercent = 40
        )

        // Clean signal below threshold
        val cleanMetrics = MidBassClarityEngine.calculateMetrics(20f, 100f, config)
        assertFalse(cleanMetrics.isDecongesting)
        assertEquals(0.0f, cleanMetrics.dynamicAttenuationDb, 1e-4f)
        assertEquals("Pristine Balance (Uncongested)", cleanMetrics.clarityRating)

        // Heavily congested signal (ratio = 0.80)
        val congestedMetrics = MidBassClarityEngine.calculateMetrics(80f, 100f, config)
        assertTrue(congestedMetrics.isDecongesting)
        assertTrue(congestedMetrics.dynamicAttenuationDb < -2.0f)
        assertTrue(congestedMetrics.clarityRating.contains("De-Mudding") || congestedMetrics.clarityRating.contains("Decongesting"))
    }

    @Test
    fun `processFrame filters stereo audio and bypasses when disabled`() {
        val config = MidBassClarityConfig(isEnabled = true, mode = MidBassClarityMode.BALANCED_DE_MUD)
        val state = MidBassClarityEngine.MidBassFilterState()

        val (outL, outR) = MidBassClarityEngine.processFrame(0.5f, -0.5f, -3.0f, config, state)
        assertTrue(outL in -1.0f..1.0f)
        assertTrue(outR in -1.0f..1.0f)

        // Disabled returns identity
        val disabledConfig = config.copy(isEnabled = false)
        val (bL, bR) = MidBassClarityEngine.processFrame(0.8f, -0.4f, -4.0f, disabledConfig, state)
        assertEquals(0.8f, bL, 1e-4f)
        assertEquals(-0.4f, bR, 1e-4f)
    }
}

