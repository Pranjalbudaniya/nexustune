package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DeClickerConfig
import com.nexus.tune.core.model.DeClickerMode
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin

class DeClickerEngineTest {

    @Test
    fun `interpolateHermite smoothly blends between endpoints matching boundary slopes`() {
        val p0 = 0.0f
        val p1 = 0.5f
        val p2 = 0.8f
        val p3 = 1.0f

        val atZero = DeClickerEngine.interpolateHermite(p0, p1, p2, p3, 0.0f)
        val atOne = DeClickerEngine.interpolateHermite(p0, p1, p2, p3, 1.0f)
        val atMid = DeClickerEngine.interpolateHermite(p0, p1, p2, p3, 0.5f)

        assertEquals(p1, atZero, 1e-4f)
        assertEquals(p2, atOne, 1e-4f)
        assertTrue("Midpoint must lie smoothly between p1 and p2", atMid in 0.5f..0.8f)
    }

    @Test
    fun `clean continuous sine wave suffers zero false positive detections`() {
        val size = 512
        val buffer = FloatArray(size) { i ->
            (sin(2.0 * Math.PI * 440.0 * i / 44100.0) * 0.5).toFloat()
        }
        val copy = buffer.clone()

        val config = DeClickerConfig(isEnabled = true, mode = DeClickerMode.TRANSPARENT_STREAM, sensitivityMultiplier = 4.0f)
        val metrics = DeClickerEngine.processBuffer(buffer, config)

        assertEquals(0, metrics.clicksRepaired)
        assertEquals("Clean Signal • No Artifacts", metrics.statusSummary)
        // Clean audio unchanged
        for (i in buffer.indices) {
            assertEquals(copy[i], buffer[i], 1e-4f)
        }
    }

    @Test
    fun `impulsive click spike is detected, repaired and smoothed`() {
        val size = 256
        val buffer = FloatArray(size) { 0.1f }

        // Inject severe impulsive spike click at index 100
        val clickIndex = 100
        buffer[clickIndex] = 0.95f // Massive jump (+0.85 above DC floor)

        val config = DeClickerConfig(isEnabled = true, mode = DeClickerMode.VINYL_RESTORATION, sensitivityMultiplier = 3.0f)
        val metrics = DeClickerEngine.processBuffer(buffer, config)

        assertTrue("Must detect and repair the spike", metrics.clicksRepaired >= 1)
        assertTrue("Repaired value should be restored to smooth level", buffer[clickIndex] < 0.3f)
    }

    @Test
    fun `bypassed mode leaves buffer completely untouched`() {
        val buffer = floatArrayOf(0.1f, 0.1f, 0.1f, 0.99f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
        val copy = buffer.clone()

        val config = DeClickerConfig(isEnabled = false, mode = DeClickerMode.OFF)
        val metrics = DeClickerEngine.processBuffer(buffer, config)

        assertEquals(0, metrics.clicksRepaired)
        assertEquals("Bypassed", metrics.statusSummary)
        assertArrayEquals(copy, buffer, 1e-5f)
    }

    @Test
    fun `status summary descriptions accurately reflect repaired count`() {
        assertEquals("Bypassed", DeClickerEngine.evaluateStatusSummary(0, DeClickerMode.OFF))
        assertEquals("Clean Signal • No Artifacts", DeClickerEngine.evaluateStatusSummary(0, DeClickerMode.TRANSPARENT_STREAM))
        assertEquals("Pristine • 3 Micro-Ticks Repaired", DeClickerEngine.evaluateStatusSummary(3, DeClickerMode.VINYL_RESTORATION))
        assertEquals("Active Guard • 12 Clicks Suppressed", DeClickerEngine.evaluateStatusSummary(12, DeClickerMode.HIGH_SENSITIVITY))
    }
}

