package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.SleepDriftProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepDriftEngineTest {

    @Test
    fun `calculateDriftProgress maps session bounds correctly`() {
        assertEquals(0.0f, SleepDriftEngine.calculateDriftProgress(0, 1800), 0.001f)
        assertEquals(0.5f, SleepDriftEngine.calculateDriftProgress(900, 1800), 0.001f)
        assertEquals(1.0f, SleepDriftEngine.calculateDriftProgress(1800, 1800), 0.001f)
        assertEquals(1.0f, SleepDriftEngine.calculateDriftProgress(2000, 1800), 0.001f)
        assertEquals(0.0f, SleepDriftEngine.calculateDriftProgress(100, 0), 0.001f)
    }

    @Test
    fun `calculateEqRollOffBands returns flat for OFF profile`() {
        val bands = SleepDriftEngine.calculateEqRollOffBands(SleepDriftProfile.OFF, 0.75f)
        assertEquals(listOf(0, 0, 0, 0, 0), bands)
    }

    @Test
    fun `calculateEqRollOffBands progressively rolls off high bands in Warm Mellow`() {
        // At 50% progress
        val bandsHalf = SleepDriftEngine.calculateEqRollOffBands(SleepDriftProfile.WARM_MELLOW, 0.5f)
        assertEquals(0, bandsHalf[0])
        assertEquals(0, bandsHalf[1])
        assertEquals(0, bandsHalf[2])
        assertEquals(-200, bandsHalf[3])
        assertEquals(-400, bandsHalf[4])

        // At 100% progress
        val bandsFull = SleepDriftEngine.calculateEqRollOffBands(SleepDriftProfile.WARM_MELLOW, 1.0f)
        assertEquals(-400, bandsFull[3])
        assertEquals(-800, bandsFull[4])
    }

    @Test
    fun `calculateEqRollOffBands applies deep cuts and sub boost for Binaural Delta`() {
        val bandsFull = SleepDriftEngine.calculateEqRollOffBands(SleepDriftProfile.BINAURAL_DELTA, 1.0f)
        assertEquals(100, bandsFull[0])
        assertEquals(50, bandsFull[1])
        assertEquals(0, bandsFull[2])
        assertEquals(-550, bandsFull[3])
        assertEquals(-900, bandsFull[4])
    }

    @Test
    fun `calculateDriftSpeed decelerates smoothly only in final half of Deep Drift`() {
        assertEquals(1.0f, SleepDriftEngine.calculateDriftSpeed(SleepDriftProfile.DEEP_DRIFT, 0.25f), 0.001f)
        assertEquals(1.0f, SleepDriftEngine.calculateDriftSpeed(SleepDriftProfile.DEEP_DRIFT, 0.5f), 0.001f)
        assertEquals(0.95f, SleepDriftEngine.calculateDriftSpeed(SleepDriftProfile.DEEP_DRIFT, 0.75f), 0.01f)
        assertEquals(0.90f, SleepDriftEngine.calculateDriftSpeed(SleepDriftProfile.DEEP_DRIFT, 1.0f), 0.01f)

        // OFF profile remains steady at 1.0
        assertEquals(1.0f, SleepDriftEngine.calculateDriftSpeed(SleepDriftProfile.OFF, 1.0f), 0.001f)
    }

    @Test
    fun `calculateBinauralFrequencies returns correct theta and delta carrier pairs`() {
        val theta = SleepDriftEngine.calculateBinauralFrequencies(SleepDriftProfile.BINAURAL_THETA)
        assertNotNull(theta)
        assertEquals(6.0f, (theta!!.second - theta.first), 0.01f)

        val delta = SleepDriftEngine.calculateBinauralFrequencies(SleepDriftProfile.BINAURAL_DELTA)
        assertNotNull(delta)
        assertEquals(3.0f, (delta!!.second - delta.first), 0.01f)

        assertNull(SleepDriftEngine.calculateBinauralFrequencies(SleepDriftProfile.OFF))
    }
}

