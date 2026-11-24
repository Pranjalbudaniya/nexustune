package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.CustomEqProfile
import com.nexus.tune.core.model.EqualizerPreset
import org.junit.Assert.*
import org.junit.Test

class EqCurveCalculatorTest {

    @Test
    fun `calculateBandCoordinates maps 5 bands to valid canvas coordinates`() {
        val flatBands = listOf(0, 0, 0, 0, 0)
        val coords = EqCurveCalculator.calculateBandCoordinates(flatBands, width = 400f, height = 200f)

        assertEquals(5, coords.size)
        // Middle gain (0mB) is exactly midway in height (y = 100f)
        assertEquals(100f, coords[0].y, 0.01f)
        assertEquals(100f, coords[2].y, 0.01f)

        // X steps are evenly distributed: 0, 100, 200, 300, 400
        assertEquals(0f, coords[0].x, 0.01f)
        assertEquals(100f, coords[1].x, 0.01f)
        assertEquals(200f, coords[2].x, 0.01f)
        assertEquals(300f, coords[3].x, 0.01f)
        assertEquals(400f, coords[4].x, 0.01f)
    }

    @Test
    fun `calculateBandCoordinates clamps extreme gains within canvas bounds`() {
        val extremeBands = listOf(1500, -2000, 0, 1200, -1200)
        val coords = EqCurveCalculator.calculateBandCoordinates(extremeBands, width = 400f, height = 200f)

        // +1200mB (top) should be at y = 0
        assertEquals(0f, coords[0].y, 0.01f)
        // -1200mB (bottom) should be at y = 200
        assertEquals(200f, coords[1].y, 0.01f)
        assertEquals(100f, coords[2].y, 0.01f)
        assertEquals(0f, coords[3].y, 0.01f)
        assertEquals(200f, coords[4].y, 0.01f)
    }

    @Test
    fun `findClosestPreset matches exact preset signatures with 100 percent accuracy`() {
        val bassBoostBands = listOf(600, 400, 0, 0, 0)
        val (matchedPreset, score) = EqCurveCalculator.findClosestPreset(bassBoostBands)

        assertEquals(EqualizerPreset.BASS_BOOST, matchedPreset)
        assertEquals(100, score)

        val flatBands = listOf(0, 0, 0, 0, 0)
        val (flatPreset, flatScore) = EqCurveCalculator.findClosestPreset(flatBands)
        assertEquals(EqualizerPreset.FLAT, flatPreset)
        assertEquals(100, flatScore)
    }

    @Test
    fun `findClosestPreset identifies closest standard preset for custom user curves`() {
        // Slightly customized bass curve: [550, 380, 50, 0, 0]
        val customBass = listOf(550, 380, 50, 0, 0)
        val (matched, score) = EqCurveCalculator.findClosestPreset(customBass)

        assertEquals(EqualizerPreset.BASS_BOOST, matched)
        assertTrue(score >= 95)
    }

    @Test
    fun `formatBandsSummary formats decibel signs correctly`() {
        val bands = listOf(600, -300, 0, 150, 1200)
        val formatted = EqCurveCalculator.formatBandsSummary(bands)

        assertEquals("+6dB, -3dB, 0dB, +1.5dB, +12dB", formatted)
    }

    @Test
    fun `serializeProfile and deserializeProfile round-trip successfully`() {
        val original = CustomEqProfile(
            id = "profile-1",
            name = "Sony XM5 Bass",
            bands = listOf(500, 200, -100, 300, 600),
            createdAtTimestamp = 1700000000000L
        )

        val serialized = EqCurveCalculator.serializeProfile(original)
        val restored = EqCurveCalculator.deserializeProfile(serialized)

        assertNotNull(restored)
        assertEquals(original.id, restored!!.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.bands, restored.bands)
        assertEquals(original.createdAtTimestamp, restored.createdAtTimestamp)
    }

    @Test
    fun `serializeProfilesList and deserializeProfilesList handle multiple profiles`() {
        val profiles = listOf(
            CustomEqProfile(id = "1", name = "P1", bands = listOf(100, 200, 300, 400, 500)),
            CustomEqProfile(id = "2", name = "P2", bands = listOf(-100, -200, 0, 200, 100))
        )

        val raw = EqCurveCalculator.serializeProfilesList(profiles)
        val restored = EqCurveCalculator.deserializeProfilesList(raw)

        assertEquals(2, restored.size)
        assertEquals("P1", restored[0].name)
        assertEquals("P2", restored[1].name)
    }
}
