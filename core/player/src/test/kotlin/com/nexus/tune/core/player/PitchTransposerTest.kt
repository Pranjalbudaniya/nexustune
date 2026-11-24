package com.nexus.tune.core.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchTransposerTest {

    @Test
    fun `semitonesToPitchRatio returns 1_0 for 0 semitones`() {
        val ratio = PitchTransposer.semitonesToPitchRatio(0)
        assertEquals(1.0f, ratio, 0.001f)
    }

    @Test
    fun `semitonesToPitchRatio converts standard semitones accurately`() {
        // +1 semitone ~ 1.05946
        assertEquals(1.0595f, PitchTransposer.semitonesToPitchRatio(1), 0.001f)
        // +2 semitones ~ 1.12246
        assertEquals(1.1225f, PitchTransposer.semitonesToPitchRatio(2), 0.001f)
        // -1 semitone ~ 0.94387
        assertEquals(0.9439f, PitchTransposer.semitonesToPitchRatio(-1), 0.001f)
        // -2 semitones ~ 0.89090
        assertEquals(0.8909f, PitchTransposer.semitonesToPitchRatio(-2), 0.001f)
        // +6 semitones ~ sqrt(2) = 1.4142
        assertEquals(1.4142f, PitchTransposer.semitonesToPitchRatio(6), 0.001f)
        // -6 semitones ~ 1/sqrt(2) = 0.7071
        assertEquals(0.7071f, PitchTransposer.semitonesToPitchRatio(-6), 0.001f)
    }

    @Test
    fun `semitonesToPitchRatio clamps to bounds`() {
        val minRatio = PitchTransposer.semitonesToPitchRatio(-10)
        assertEquals(PitchTransposer.semitonesToPitchRatio(-6), minRatio, 0.001f)

        val maxRatio = PitchTransposer.semitonesToPitchRatio(10)
        assertEquals(PitchTransposer.semitonesToPitchRatio(6), maxRatio, 0.001f)
    }

    @Test
    fun `pitchRatioToSemitones inverts correctly`() {
        for (st in -6..6) {
            val ratio = PitchTransposer.semitonesToPitchRatio(st)
            val convertedSt = PitchTransposer.pitchRatioToSemitones(ratio)
            assertEquals(st, convertedSt)
        }
    }

    @Test
    fun `formatSemitones outputs correct labels`() {
        assertEquals("Original (0)", PitchTransposer.formatSemitones(0))
        assertEquals("+2 st", PitchTransposer.formatSemitones(2))
        assertEquals("-3 st", PitchTransposer.formatSemitones(-3))
    }

    @Test
    fun `formatPitchMultiplier formats to two decimal places`() {
        assertEquals("1.00x", PitchTransposer.formatPitchMultiplier(1.0f))
        assertEquals("1.12x", PitchTransposer.formatPitchMultiplier(1.1225f))
    }
}
