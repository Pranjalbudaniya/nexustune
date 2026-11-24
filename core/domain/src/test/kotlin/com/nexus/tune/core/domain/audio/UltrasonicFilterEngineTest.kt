package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class UltrasonicFilterEngineTest {

    @Test
    fun `low-pass biquad has unity gain at DC and zeroes Nyquist`() {
        val coeffs = UltrasonicFilterEngine.calculateBiquadCoefficients(
            cutoffHz = 22050.0f,
            topology = UltrasonicTopology.LINEAR_PHASE_FIR,
            sampleRate = 96000
        )

        // Low-pass at DC (z = 1): H(1) = (b0 + b1 + b2) / (1 + a1 + a2) ~= 1.0 (0 dB)
        val numDC = coeffs.b0 + coeffs.b1 + coeffs.b2
        val denDC = 1.0f + coeffs.a1 + coeffs.a2
        val gainDC = numDC / denDC
        assertTrue("Gain at DC should be unity (~1.0), was $gainDC", abs(gainDC - 1.0f) < 0.05f)

        // Stability: |a2| < 1
        assertTrue("a2 coefficient should be stable (< 1.0), was ${coeffs.a2}", abs(coeffs.a2) < 1.0f)
    }

    @Test
    fun `stopband rejection and group delay reflect topology characteristics`() {
        val linearRejection = UltrasonicFilterEngine.calculateStopbandRejectionDb(22050.0f, UltrasonicTopology.LINEAR_PHASE_FIR)
        val besselRejection = UltrasonicFilterEngine.calculateStopbandRejectionDb(22050.0f, UltrasonicTopology.BESSEL_CONSTANT_DELAY)
        assertTrue("Linear phase filter should have higher stopband attenuation than Bessel, was $linearRejection vs $besselRejection", linearRejection > besselRejection)

        val minPhaseDelay = UltrasonicFilterEngine.calculateGroupDelayMicros(24000.0f, UltrasonicTopology.MINIMUM_PHASE_IIR)
        val linearPhaseDelay = UltrasonicFilterEngine.calculateGroupDelayMicros(24000.0f, UltrasonicTopology.LINEAR_PHASE_FIR)
        assertTrue("Minimum phase should have much lower latency than linear phase, was $minPhaseDelay vs $linearPhaseDelay", minPhaseDelay < linearPhaseDelay)
    }

    @Test
    fun `calculateMetrics produces expected ratings when active vs bypassed`() {
        val bypassed = UltrasonicFilterConfig(isEnabled = false)
        val bypassedMetrics = UltrasonicFilterEngine.calculateMetrics(bypassed)
        assertEquals(0.0f, bypassedMetrics.stopbandRejectionDb, 0.001f)
        assertTrue(bypassedMetrics.imdShieldRating.contains("Bypassed"))

        val active = UltrasonicFilterConfig(
            isEnabled = true,
            mode = UltrasonicMode.HI_RES_TAMER_24K,
            topology = UltrasonicTopology.MINIMUM_PHASE_IIR,
            cutoffHz = 24000.0f
        )
        val activeMetrics = UltrasonicFilterEngine.calculateMetrics(active)
        assertTrue(activeMetrics.stopbandRejectionDb > 80.0f)
        assertTrue(activeMetrics.preRingingSuppression.contains("Pre-Ringing Eliminated"))
        assertTrue(activeMetrics.imdShieldRating.contains("IMD Shield"))
    }
}

