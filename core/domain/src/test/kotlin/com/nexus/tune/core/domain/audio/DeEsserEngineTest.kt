package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class DeEsserEngineTest {

    @Test
    fun `bandpass detection biquad is unconditionally stable and zeroes DC`() {
        val coeffs = DeEsserEngine.calculateBandpassDetectionCoefficients(
            centerFrequencyHz = 6500.0f,
            q = 2.0f,
            sampleRate = 48000
        )

        // For bandpass with b1 = 0, b2 = -b0: at DC (z = 1), numerator = b0 + b1 + b2 = b0 - b0 = 0
        val numDC = coeffs.b0 + coeffs.b1 + coeffs.b2
        assertEquals(0.0f, numDC, 0.0001f)

        // Stability: |a2| < 1
        assertTrue("a2 coefficient should be stable (< 1.0), was ${coeffs.a2}", abs(coeffs.a2) < 1.0f)
    }

    @Test
    fun `dynamic reduction engages only above threshold`() {
        val threshold = 30
        val maxReduction = 4.0f

        // Sub-threshold ratio (20% <= 30%)
        val subReduction = DeEsserEngine.calculateDynamicReductionDb(0.20f, threshold, maxReduction)
        assertEquals(0.0f, subReduction, 0.001f)

        // Above threshold ratio (65% > 30%)
        val aboveReduction = DeEsserEngine.calculateDynamicReductionDb(0.65f, threshold, maxReduction)
        assertTrue("Reduction should be negative, was $aboveReduction", aboveReduction < 0.0f)
        assertTrue("Reduction should be within -maxReduction, was $aboveReduction", aboveReduction >= -maxReduction)

        // Saturated 100% ratio
        val saturatedReduction = DeEsserEngine.calculateDynamicReductionDb(1.0f, threshold, maxReduction)
        assertEquals(-maxReduction, saturatedReduction, 0.001f)
    }

    @Test
    fun `hardware offset maps negative decibels to negative millibels`() {
        assertEquals(0, DeEsserEngine.calculateHardwareOffsetMb(-4.5f, isEnabled = false))
        assertEquals(-300, DeEsserEngine.calculateHardwareOffsetMb(-3.0f, isEnabled = true))
        assertEquals(-450, DeEsserEngine.calculateHardwareOffsetMb(-4.5f, isEnabled = true))
        assertEquals(-800, DeEsserEngine.calculateHardwareOffsetMb(-8.0f, isEnabled = true))
        assertEquals(-1000, DeEsserEngine.calculateHardwareOffsetMb(-15.0f, isEnabled = true)) // Clamped
    }

    @Test
    fun `calculateMetrics reflects state correctly when enabled vs bypassed`() {
        val bypassed = DeEsserConfig(isEnabled = false)
        val bypassedMetrics = DeEsserEngine.calculateMetrics(bypassed)
        assertEquals(0.0f, bypassedMetrics.dynamicReductionDb, 0.001f)
        assertEquals(0, bypassedMetrics.fatigueReductionPercent)
        assertTrue(bypassedMetrics.vocalSmoothnessRating.contains("Bypassed"))

        val active = DeEsserConfig(
            isEnabled = true,
            mode = DeEsserMode.BRIGHT_HEADPHONE_TAMER,
            centerFrequencyHz = 7000.0f,
            thresholdPercent = 22,
            maxReductionDb = 6.0f
        )
        val activeMetrics = DeEsserEngine.calculateMetrics(active, observedSibilanceActivityPercent = 60)
        assertTrue(activeMetrics.dynamicReductionDb < 0.0f)
        assertTrue(activeMetrics.fatigueReductionPercent > 50)
        assertTrue(activeMetrics.vocalSmoothnessRating.contains("Headphone"))
    }
}

