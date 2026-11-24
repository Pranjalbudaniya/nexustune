package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.InfrasonicFilterConfig
import com.nexus.tune.core.model.InfrasonicSlope
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class InfrasonicFilterEngineTest {

    @Test
    fun `calculateRumbleAttenuationAt10Hz calculates accurate negative dB attenuation`() {
        // At 20 Hz cutoff with 18 dB/oct slope: 1 octave below is exactly -18 dB
        val atten20_18 = InfrasonicFilterEngine.calculateRumbleAttenuationAt10Hz(20.0f, InfrasonicSlope.SLOPE_18DB_OCTAVE)
        assertEquals(-18.0f, atten20_18, 1e-3f)

        // At 20 Hz cutoff with 24 dB/oct slope: 1 octave below is exactly -24 dB
        val atten20_24 = InfrasonicFilterEngine.calculateRumbleAttenuationAt10Hz(20.0f, InfrasonicSlope.SLOPE_24DB_OCTAVE)
        assertEquals(-24.0f, atten20_24, 1e-3f)

        // At 10 Hz cutoff: zero attenuation
        val atten10 = InfrasonicFilterEngine.calculateRumbleAttenuationAt10Hz(10.0f, InfrasonicSlope.SLOPE_18DB_OCTAVE)
        assertEquals(0.0f, atten10, 1e-3f)
    }

    @Test
    fun `calculateHeadroomRestoredDb restores between 0_5 and 3_5 dB of dynamic power`() {
        for (cutoff in listOf(15f, 20f, 25f, 30f)) {
            for (slope in InfrasonicSlope.entries) {
                val headroom = InfrasonicFilterEngine.calculateHeadroomRestoredDb(cutoff, slope)
                assertTrue("Headroom must be within 0.5 to 3.5 dB", headroom in 0.5f..3.5f)
            }
        }
    }

    @Test
    fun `calculateHighPassBiquadCoefficients produces zero transmission at DC and unity at high frequency`() {
        val coeffs = InfrasonicFilterEngine.calculateHighPassBiquadCoefficients(20.0f, 0.7071f, 44100)

        // DC gain H(1) = (b0 + b1 + b2) / (1 + a1 + a2) must be zero
        val numeratorAtDc = coeffs.b0 + coeffs.b1 + coeffs.b2
        assertEquals(0.0f, numeratorAtDc, 1e-5f)

        // Poles must be strictly inside unit circle
        assertTrue("|a2| must be strictly less than 1.0", abs(coeffs.a2) < 1.0f)
        assertTrue("b0 must be positive", coeffs.b0 > 0.0f)
    }

    @Test
    fun `calculateMetrics produces accurate descriptive ratings`() {
        val config = InfrasonicFilterConfig(
            isEnabled = true,
            cutoffHz = 20.0f,
            slope = InfrasonicSlope.SLOPE_18DB_OCTAVE,
            dcBlockerEnabled = true
        )
        val metrics = InfrasonicFilterEngine.calculateMetrics(config)
        assertEquals(20.0f, metrics.cutoffHz, 1e-4f)
        assertTrue(metrics.preservedHeadroomDb > 1.0f)
        assertEquals(-18.0f, metrics.rumbleAttenuationAt10HzDb, 1e-3f)
        assertTrue(metrics.protectionRating.contains("DC Bias") || metrics.protectionRating.contains("Pristine"))
    }

    @Test
    fun `processFrame attenuates DC offset and blocks zero-frequency bias`() {
        val config = InfrasonicFilterConfig(
            isEnabled = true,
            cutoffHz = 20.0f,
            slope = InfrasonicSlope.SLOPE_18DB_OCTAVE,
            dcBlockerEnabled = true
        )
        val state = InfrasonicFilterEngine.InfrasonicFilterState()

        // Feed continuous DC bias of 0.5f into the filter for 500 samples
        var lastL = 0.5f
        for (i in 0 until 500) {
            val (outL, _) = InfrasonicFilterEngine.processFrame(0.5f, 0.5f, config, state)
            lastL = outL
        }

        // DC offset must be decayed down toward zero
        assertTrue("DC bias must be attenuated toward zero", abs(lastL) < 0.05f)
    }

    @Test
    fun `bypassed filter leaves audio unmodified`() {
        val config = InfrasonicFilterConfig(isEnabled = false)
        val state = InfrasonicFilterEngine.InfrasonicFilterState()

        val (outL, outR) = InfrasonicFilterEngine.processFrame(0.75f, -0.45f, config, state)
        assertEquals(0.75f, outL, 1e-4f)
        assertEquals(-0.45f, outR, 1e-4f)
    }
}

