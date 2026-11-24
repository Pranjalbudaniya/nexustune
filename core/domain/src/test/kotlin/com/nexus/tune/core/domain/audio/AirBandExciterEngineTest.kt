package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class AirBandExciterEngineTest {

    @Test
    fun `biquad high shelf coefficients are stable and have unity gain at DC`() {
        val coeffs = AirBandExciterEngine.calculateBiquadCoefficients(
            frequencyHz = 16000,
            boostDb = 4.0f,
            sampleRate = 48000
        )

        // For high shelf, at DC (z = 1), H(1) = (b0 + b1 + b2) / (1 + a1 + a2) ~= 1.0 (0 dB)
        val numDC = coeffs.b0 + coeffs.b1 + coeffs.b2
        val denDC = 1.0f + coeffs.a1 + coeffs.a2
        val gainDC = numDC / denDC
        assertTrue("Gain at DC should be unity (~1.0), was $gainDC", abs(gainDC - 1.0f) < 0.05f)

        // Pole stability check: roots of 1 + a1*z^-1 + a2*z^-2 inside unit circle -> |a2| < 1
        assertTrue("a2 coefficient should be < 1.0 for stability, was ${coeffs.a2}", abs(coeffs.a2) < 1.0f)
    }

    @Test
    fun `hardware offset translates decibels to millibels properly`() {
        assertEquals(0, AirBandExciterEngine.calculateHardwareOffsetMb(4.5f, isEnabled = false))
        assertEquals(300, AirBandExciterEngine.calculateHardwareOffsetMb(3.0f, isEnabled = true))
        assertEquals(450, AirBandExciterEngine.calculateHardwareOffsetMb(4.5f, isEnabled = true))
        assertEquals(600, AirBandExciterEngine.calculateHardwareOffsetMb(6.0f, isEnabled = true))
        assertEquals(1000, AirBandExciterEngine.calculateHardwareOffsetMb(15.0f, isEnabled = true)) // Clamped
    }

    @Test
    fun `generateHarmonics introduces non-linear saturation without clipping`() {
        val input = 0.5f
        val clean = AirBandExciterEngine.generateHarmonics(input, 0)
        assertEquals(0.5f, clean, 0.0001f)

        val excited = AirBandExciterEngine.generateHarmonics(input, 50)
        assertTrue("Harmonics should add positive even distortion to positive input", excited > input)
        assertTrue("Excited output should not exceed 1.0", excited <= 1.0f)

        val negativeInput = -0.5f
        val excitedNegative = AirBandExciterEngine.generateHarmonics(negativeInput, 50)
        assertTrue("Harmonics should preserve odd symmetry or bound", excitedNegative < 0.0f)
    }

    @Test
    fun `calculateMetrics reflects state correctly when enabled vs bypassed`() {
        val bypassedConfig = AirBandExciterConfig(isEnabled = false)
        val bypassedMetrics = AirBandExciterEngine.calculateMetrics(bypassedConfig)
        assertEquals(0.0f, bypassedMetrics.boostDb, 0.001f)
        assertEquals(0.0f, bypassedMetrics.harmonicDistortionPercent, 0.001f)
        assertTrue(bypassedMetrics.sheenDescriptor.contains("Bypassed"))

        val activeConfig = AirBandExciterConfig(
            isEnabled = true,
            mode = AirBandMode.MASTERING_AIR,
            frequency = AirBandFrequency.HZ_20000,
            boostDb = 4.5f,
            harmonicExcitationPercent = 50
        )
        val activeMetrics = AirBandExciterEngine.calculateMetrics(activeConfig)
        assertEquals(4.5f, activeMetrics.boostDb, 0.001f)
        assertEquals(20000, activeMetrics.frequencyHz)
        assertTrue(activeMetrics.harmonicDistortionPercent > 1.0f)
        assertTrue(activeMetrics.airEnergyIndexPercent > 70)
        assertTrue(activeMetrics.sheenDescriptor.contains("Pultec") || activeMetrics.sheenDescriptor.contains("Sparkle"))
    }
}

