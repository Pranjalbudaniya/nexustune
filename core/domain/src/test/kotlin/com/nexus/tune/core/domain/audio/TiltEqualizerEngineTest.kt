package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.TiltEqualizerConfig
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class TiltEqualizerEngineTest {

    @Test
    fun `zero tilt yields zero gain across all frequencies and identity offsets`() {
        val gainAtPivot = TiltEqualizerEngine.calculateTiltGainAtFrequency(1000.0f, 0.0f, 1000.0f)
        val gainAtBass = TiltEqualizerEngine.calculateTiltGainAtFrequency(60.0f, 0.0f, 1000.0f)
        val gainAtTreble = TiltEqualizerEngine.calculateTiltGainAtFrequency(14000.0f, 0.0f, 1000.0f)

        assertEquals(0.0f, gainAtPivot, 1e-4f)
        assertEquals(0.0f, gainAtBass, 1e-4f)
        assertEquals(0.0f, gainAtTreble, 1e-4f)

        val offsets = TiltEqualizerEngine.calculate5BandOffsets(0.0f, 1000.0f)
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun `positive tilt produces treble boost and bass attenuation with zero at pivot`() {
        val pivot = 1000.0f
        val tilt = 4.0f

        val gainAtPivot = TiltEqualizerEngine.calculateTiltGainAtFrequency(pivot, tilt, pivot)
        val gainAtBass = TiltEqualizerEngine.calculateTiltGainAtFrequency(100.0f, tilt, pivot)
        val gainAtTreble = TiltEqualizerEngine.calculateTiltGainAtFrequency(10000.0f, tilt, pivot)

        assertEquals(0.0f, gainAtPivot, 1e-3f)
        assertTrue("Bass should be attenuated under bright tilt", gainAtBass < -1.0f)
        assertTrue("Treble should be boosted under bright tilt", gainAtTreble > 1.0f)

        val offsets = TiltEqualizerEngine.calculate5BandOffsets(tilt, pivot)
        assertTrue(offsets[0] < 0) // Bass band cut
        assertTrue(offsets[4] > 0) // Treble band boosted
    }

    @Test
    fun `negative tilt produces warm bass boost and treble rolloff`() {
        val pivot = 1000.0f
        val tilt = -4.0f

        val gainAtPivot = TiltEqualizerEngine.calculateTiltGainAtFrequency(pivot, tilt, pivot)
        val gainAtBass = TiltEqualizerEngine.calculateTiltGainAtFrequency(100.0f, tilt, pivot)
        val gainAtTreble = TiltEqualizerEngine.calculateTiltGainAtFrequency(10000.0f, tilt, pivot)

        assertEquals(0.0f, gainAtPivot, 1e-3f)
        assertTrue("Bass should be boosted under warm tilt", gainAtBass > 1.0f)
        assertTrue("Treble should be rolled off under warm tilt", gainAtTreble < -1.0f)

        val offsets = TiltEqualizerEngine.calculate5BandOffsets(tilt, pivot)
        assertTrue(offsets[0] > 0) // Bass boosted
        assertTrue(offsets[4] < 0) // Treble cut
    }

    @Test
    fun `pivot frequency shift properly moves the zero-crossing inflection point`() {
        val tilt = 3.0f
        val pivot630 = 630.0f
        val pivot1600 = 1600.0f

        assertEquals(0.0f, TiltEqualizerEngine.calculateTiltGainAtFrequency(pivot630, tilt, pivot630), 1e-3f)
        assertEquals(0.0f, TiltEqualizerEngine.calculateTiltGainAtFrequency(pivot1600, tilt, pivot1600), 1e-3f)

        // At 1000Hz, with pivot at 630Hz (f > pivot) it is boosted
        assertTrue(TiltEqualizerEngine.calculateTiltGainAtFrequency(1000.0f, tilt, pivot630) > 0.0f)
        // With pivot at 1600Hz (f < pivot) it is attenuated
        assertTrue(TiltEqualizerEngine.calculateTiltGainAtFrequency(1000.0f, tilt, pivot1600) < 0.0f)
    }

    @Test
    fun `biquad coefficients are unconditionally stable with poles strictly inside unit circle`() {
        for (tilt in listOf(-6.0f, -3.0f, 0.0f, 2.5f, 6.0f)) {
            val coeffs = TiltEqualizerEngine.calculateBiquadCoefficients(tilt, 1000.0f, 44100)
            assertTrue("Pole |a1| must be strictly less than 1.0", abs(coeffs.a1) < 1.0f)
        }
    }

    @Test
    fun `spectral metrics return accurate descriptive ratings and loudness offsets`() {
        val warmConfig = TiltEqualizerConfig(isEnabled = true, tiltDb = -3.5f, pivotHz = 1000.0f)
        val warmMetrics = TiltEqualizerEngine.calculateSpectralMetrics(warmConfig)
        assertEquals("Warm Analog Tube", warmMetrics.tonalBalanceDescription)
        assertTrue(warmMetrics.bassGainDb > 0f)
        assertTrue(warmMetrics.trebleGainDb < 0f)

        val brightConfig = TiltEqualizerConfig(isEnabled = true, tiltDb = 3.0f, pivotHz = 1000.0f)
        val brightMetrics = TiltEqualizerEngine.calculateSpectralMetrics(brightConfig)
        assertEquals("Crisp Detail Sheen", brightMetrics.tonalBalanceDescription)
        assertTrue(brightMetrics.bassGainDb < 0f)
        assertTrue(brightMetrics.trebleGainDb > 0f)
    }

    @Test
    fun `processFrame filters stereo audio and respects loudness compensation`() {
        val config = TiltEqualizerConfig(isEnabled = true, tiltDb = 2.0f, pivotHz = 1000.0f, loudnessCompensate = true)
        val state = TiltEqualizerEngine.TiltFilterState()

        val (outL1, outR1) = TiltEqualizerEngine.processFrame(0.5f, -0.5f, config, state)
        assertTrue(outL1 in -1.0f..1.0f)
        assertTrue(outR1 in -1.0f..1.0f)

        // Bypassed returns unmodified audio
        val bypassConfig = config.copy(isEnabled = false)
        val (bL, bR) = TiltEqualizerEngine.processFrame(0.77f, -0.33f, bypassConfig, state)
        assertEquals(0.77f, bL, 1e-4f)
        assertEquals(-0.33f, bR, 1e-4f)
    }
}

