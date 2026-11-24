package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.HumHarmonicDepth
import com.nexus.tune.core.model.HumIsolatorPreset
import com.nexus.tune.core.model.MainsFrequency
import com.nexus.tune.core.model.MainsHumIsolatorConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class MainsHumIsolatorEngineTest {

    private lateinit var engine: MainsHumIsolatorEngine
    private val sampleRate = 48000

    @Before
    fun setUp() {
        engine = MainsHumIsolatorEngine(sampleRate)
    }

    @Test
    fun `bypassed configuration does not alter input buffer`() {
        val config = MainsHumIsolatorConfig(isEnabled = false)
        val original = FloatArray(512) { i -> sin(2.0 * PI * 1000.0 * i / sampleRate).toFloat() * 0.5f }
        val buffer = original.clone()

        val metrics = engine.process(buffer, config)

        for (i in buffer.indices) {
            assertEquals(original[i], buffer[i], 0.00001f)
        }
        assertEquals(0.0f, metrics.attenuationAtF0Db, 0.001f)
        assertEquals(1.0f, metrics.subBassPreservationRatio, 0.001f)
    }

    @Test
    fun `50 Hz mains hum is notched significantly`() {
        val config = MainsHumIsolatorConfig(
            isEnabled = true,
            preset = HumIsolatorPreset.STUDIO_CLEAN_AUTO,
            frequency = MainsFrequency.HZ_50,
            harmonicDepth = HumHarmonicDepth.LOW_HARMONICS,
            notchQ = 25.0f,
            notchDepthDb = -24.0f,
            adaptiveMasking = false,
            dcBlockerEnabled = false
        )

        // Generate pure 50 Hz sine wave
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 50.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        // Warm up and process
        engine.reset()
        engine.process(buffer, config)

        // Compute RMS of tail (after initial filter settle)
        var sumSquares = 0.0
        val startSample = numSamples / 2
        val count = numSamples - startSample
        for (i in startSample until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / count)

        // Original RMS of 0.4 amplitude sine is 0.4 / sqrt(2) ≈ 0.2828
        // -24 dB notch should reduce RMS significantly (by at least 60%)
        assertTrue("50 Hz hum should be attenuated: rmsOut=$rmsOut", rmsOut < 0.12)
    }

    @Test
    fun `adjacent musical bass frequency at 75 Hz is preserved`() {
        val config = MainsHumIsolatorConfig(
            isEnabled = true,
            preset = HumIsolatorPreset.STUDIO_CLEAN_AUTO,
            frequency = MainsFrequency.HZ_50,
            harmonicDepth = HumHarmonicDepth.LOW_HARMONICS,
            notchQ = 30.0f,
            notchDepthDb = -24.0f,
            adaptiveMasking = false,
            dcBlockerEnabled = false
        )

        // Pure 75 Hz musical note (well outside narrow 50 Hz notch with Q=30)
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 75.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        engine.reset()
        engine.process(buffer, config)

        var sumSquares = 0.0
        val startSample = numSamples / 2
        val count = numSamples - startSample
        for (i in startSample until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / count)
        val expectedRms = 0.4 / sqrt(2.0)

        // At 75 Hz, pass-band should remain > 90% of original
        assertTrue(
            "75 Hz music should be preserved: rmsOut=$rmsOut, expected=$expectedRms",
            rmsOut > expectedRms * 0.90
        )
    }

    @Test
    fun `auto detect correctly identifies 60 Hz interference`() {
        // Generate pure 60 Hz hum signal
        val numSamples = 2048
        val buffer60 = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 60.0 * (i / 2) / sampleRate).toFloat() * 0.3f
        }

        val detectedFreq = engine.detectGridFrequency(buffer60)
        assertEquals(60.0f, detectedFreq, 0.1f)
    }

    @Test
    fun `auto detect correctly identifies 50 Hz interference`() {
        val numSamples = 2048
        val buffer50 = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 50.0 * (i / 2) / sampleRate).toFloat() * 0.3f
        }

        val detectedFreq = engine.detectGridFrequency(buffer50)
        assertEquals(50.0f, detectedFreq, 0.1f)
    }

    @Test
    fun `dc blocker removes constant dc bias`() {
        val config = MainsHumIsolatorConfig(
            isEnabled = true,
            preset = HumIsolatorPreset.USB_DAC_GROUND_LIFT,
            frequency = MainsFrequency.HZ_50,
            dcBlockerEnabled = true
        )

        // Signal with +0.25 DC offset
        val numSamples = 9600
        val buffer = FloatArray(numSamples) { 0.25f }

        engine.reset()
        engine.process(buffer, config)

        // Settle tail check
        val tailAverage = buffer.takeLast(1000).map { abs(it) }.average()
        assertTrue("DC offset should be filtered near zero: tailAverage=$tailAverage", tailAverage < 0.05)
    }

    @Test
    fun `hardware eq compensation reflects harmonic depth`() {
        val disabledConfig = MainsHumIsolatorConfig(isEnabled = false)
        assertEquals(0, engine.calculateHardwareEqCompensation(disabledConfig))

        val fullSpectrumConfig = MainsHumIsolatorConfig(
            isEnabled = true,
            preset = HumIsolatorPreset.USB_DAC_GROUND_LIFT,
            harmonicDepth = HumHarmonicDepth.FULL_SPECTRUM
        )
        assertEquals(60, engine.calculateHardwareEqCompensation(fullSpectrumConfig))

        val lowHarmonicsConfig = MainsHumIsolatorConfig(
            isEnabled = true,
            preset = HumIsolatorPreset.STUDIO_CLEAN_AUTO,
            harmonicDepth = HumHarmonicDepth.LOW_HARMONICS
        )
        assertEquals(40, engine.calculateHardwareEqCompensation(lowHarmonicsConfig))
    }
}

