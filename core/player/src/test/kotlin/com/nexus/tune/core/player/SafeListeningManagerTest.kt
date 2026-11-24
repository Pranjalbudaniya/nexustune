package com.nexus.tune.core.player

import com.nexus.tune.core.player.hearing.SafeListeningManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SafeListeningManagerTest {

    private lateinit var manager: SafeListeningManager

    @Before
    fun setup() {
        manager = SafeListeningManager()
    }

    @Test
    fun `initial dose is zero and not overexposed`() {
        val info = manager.soundDoseInfo.value
        assertEquals(0f, info.dosePercent, 0.01f)
        assertFalse(info.isOverExposed)
        assertEquals(0L, info.listeningMinutes)
    }

    @Test
    fun `recordExposure increases dose progressively`() {
        manager.recordExposure(durationSeconds = 3600L, volumeLevel = 0.8f)
        val info = manager.soundDoseInfo.value
        assertTrue(info.dosePercent > 0f)
        assertEquals(60L, info.listeningMinutes)
        assertFalse(info.isOverExposed)
    }

    @Test
    fun `recordExposure sets isOverExposed at high volume over duration`() {
        // High volume (1.0) produces steep acoustic exposure dose
        manager.recordExposure(durationSeconds = 150_000L, volumeLevel = 1.0f)
        val info = manager.soundDoseInfo.value
        assertTrue(info.dosePercent >= 100f)
        assertTrue(info.isOverExposed)
    }

    @Test
    fun `resetDose restores initial state`() {
        manager.recordExposure(durationSeconds = 3600L, volumeLevel = 0.9f)
        assertTrue(manager.soundDoseInfo.value.dosePercent > 0f)

        manager.resetDose()
        assertEquals(0f, manager.soundDoseInfo.value.dosePercent, 0.01f)
        assertFalse(manager.soundDoseInfo.value.isOverExposed)
    }
}
