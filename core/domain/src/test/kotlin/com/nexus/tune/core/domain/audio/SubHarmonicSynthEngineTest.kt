package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test

class SubHarmonicSynthEngineTest {

    @Test
    fun `perceived bass gain scales linearly with intensity`() {
        assertEquals(0.0f, SubHarmonicSynthEngine.calculatePerceivedBassGainDb(50, isEnabled = false), 0.001f)
        assertEquals(3.25f, SubHarmonicSynthEngine.calculatePerceivedBassGainDb(50, isEnabled = true), 0.01f)
        assertEquals(6.5f, SubHarmonicSynthEngine.calculatePerceivedBassGainDb(100, isEnabled = true), 0.01f)
    }

    @Test
    fun `hardware offset accounts for transducer safety and target profile`() {
        assertEquals(0, SubHarmonicSynthEngine.calculateHardwareOffsetMb(50, false, SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS))

        val earbudOffset = SubHarmonicSynthEngine.calculateHardwareOffsetMb(80, true, SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS)
        val subwooferOffset = SubHarmonicSynthEngine.calculateHardwareOffsetMb(80, true, SubHarmonicTarget.FULL_RANGE_SUBWOOFER)

        assertTrue("Subwoofer offset should be more aggressive than earbud offset", subwooferOffset > earbudOffset)
        assertTrue("Offset must not exceed 600 mB ceiling", subwooferOffset <= 600)
    }

    @Test
    fun `chebyshev harmonic generation injects rich harmonics without clipping`() {
        val input = 0.6f
        val clean = SubHarmonicSynthEngine.synthesizeMissingFundamentalHarmonics(input, 0)
        assertEquals(0.6f, clean, 0.001f)

        val processed = SubHarmonicSynthEngine.synthesizeMissingFundamentalHarmonics(input, 75)
        assertNotEquals(input, processed, 0.001f)
        assertTrue("Processed output must be bounded to [-1.0, 1.0]", processed in -1.0f..1.0f)
    }

    @Test
    fun `sub-octave synthesis toggles on zero-crossing`() {
        var state = false
        var prev = -0.2f
        var current = 0.3f // Positive zero-crossing
        val (sampleOut, nextState) = SubHarmonicSynthEngine.synthesizeSubOctaveSample(current, prev, state, 50)
        assertTrue("State should toggle on zero-crossing", nextState != state)

        // Non zero-crossing step
        prev = 0.3f
        current = 0.5f
        val (_, nonToggledState) = SubHarmonicSynthEngine.synthesizeSubOctaveSample(current, prev, nextState, 50)
        assertEquals("State should not toggle when not crossing zero", nextState, nonToggledState)
    }

    @Test
    fun `calculateMetrics produces expected ratings and safety flags`() {
        val bypassed = SubHarmonicSynthConfig(isEnabled = false)
        val bypassedMetrics = SubHarmonicSynthEngine.calculateMetrics(bypassed)
        assertEquals(0.0f, bypassedMetrics.perceivedBassGainDb, 0.001f)
        assertFalse(bypassedMetrics.excursionSafeguardActive)

        val earbudConfig = SubHarmonicSynthConfig(
            isEnabled = true,
            mode = SubHarmonicMode.EARBUD_MAX_PUNCH,
            target = SubHarmonicTarget.SMALL_SPEAKERS_EARBUDS,
            intensityPercent = 70
        )
        val earbudMetrics = SubHarmonicSynthEngine.calculateMetrics(earbudConfig)
        assertTrue(earbudMetrics.perceivedBassGainDb > 4.0f)
        assertTrue(earbudMetrics.excursionSafeguardActive)
        assertTrue(earbudMetrics.phantomFundamentalRating.contains("MaxxBass"))
    }
}

