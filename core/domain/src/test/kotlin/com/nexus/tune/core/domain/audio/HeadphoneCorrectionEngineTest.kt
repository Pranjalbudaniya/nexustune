package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.HeadphoneCorrectionConfig
import com.nexus.tune.core.model.HeadphoneTargetCurve
import org.junit.Assert.*
import org.junit.Test

class HeadphoneCorrectionEngineTest {

    @Test
    fun `calculateTargetEqOffsets returns 5 bands for all target curves`() {
        HeadphoneTargetCurve.entries.forEach { curve ->
            val offsets = HeadphoneCorrectionEngine.calculateTargetEqOffsets(curve)
            assertEquals("Curve $curve must have 5 bands", 5, offsets.size)
        }
    }

    @Test
    fun `calculateTargetEqOffsets flat for STUDIO_NEUTRAL`() {
        val offsets = HeadphoneCorrectionEngine.calculateTargetEqOffsets(HeadphoneTargetCurve.STUDIO_NEUTRAL)
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun `calculateTargetEqOffsets has subbass shelf and pinna gain for HARMAN_OVER_EAR`() {
        val offsets = HeadphoneCorrectionEngine.calculateTargetEqOffsets(HeadphoneTargetCurve.HARMAN_OVER_EAR)
        assertTrue("Sub-bass (60Hz) should have shelf boost", offsets[0] > 0)
        assertTrue("Ear canal (3.6kHz) should have pinna boost", offsets[3] > 0)
        assertEquals("Mid-range fundamental (910Hz) is flat", 0, offsets[2])
    }

    @Test
    fun `calculateBassBoostStrength assigns highest boost to V_SHAPED_BASSHEAD`() {
        val bassheadBoost = HeadphoneCorrectionEngine.calculateBassBoostStrength(HeadphoneTargetCurve.V_SHAPED_BASSHEAD)
        val neutralBoost = HeadphoneCorrectionEngine.calculateBassBoostStrength(HeadphoneTargetCurve.STUDIO_NEUTRAL)

        assertEquals(700, bassheadBoost)
        assertEquals(0, neutralBoost)
    }

    @Test
    fun `calculateCrossfeedDelayMs and Attenuation clamp within realistic acoustic boundaries`() {
        val delay0 = HeadphoneCorrectionEngine.calculateCrossfeedDelayMs(0)
        val delay100 = HeadphoneCorrectionEngine.calculateCrossfeedDelayMs(100)
        assertTrue(delay0 >= 0.25f)
        assertTrue(delay100 <= 0.45f)

        val atten0 = HeadphoneCorrectionEngine.calculateCrossfeedAttenuationDb(0)
        val atten100 = HeadphoneCorrectionEngine.calculateCrossfeedAttenuationDb(100)
        assertEquals(-14.0f, atten0, 0.01f)
        assertEquals(-6.0f, atten100, 0.01f)
    }

    @Test
    fun `formatTargetDescription formats displayName and tonal character`() {
        val desc = HeadphoneCorrectionEngine.formatTargetDescription(HeadphoneTargetCurve.HARMAN_OVER_EAR)
        assertTrue(desc.contains("Harman Over-Ear (2019)"))
        assertTrue(desc.contains("Warm & Balanced"))
    }
}

