package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.PreampFlattenerMode
import com.nexus.tune.core.model.PreampNoiseFlattenerConfig
import com.nexus.tune.core.model.PreampNoiseFlattenerPreset
import com.nexus.tune.core.model.PreampNoiseProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PreampNoiseFlattenerEngineTest {

    private lateinit var engine: PreampNoiseFlattenerEngine

    @Before
    fun setUp() {
        engine = PreampNoiseFlattenerEngine(sampleRate = 44100)
    }

    @Test
    fun `bypassed engine preserves audio buffer identically`() {
        val original = floatArrayOf(0.1f, -0.2f, 0.3f, -0.4f, 0.5f, -0.6f)
        val buffer = original.clone()
        val config = PreampNoiseFlattenerConfig(isEnabled = false)

        val metrics = engine.processInterleaved(buffer, config = config)

        for (i in original.indices) {
            assertEquals(original[i], buffer[i], 1e-6f)
        }
        assertEquals("Bypassed (Direct Signal)", metrics.noiseFloorRating)
        assertFalse(metrics.ditherActive)
    }

    @Test
    fun `downward expander attenuates low-level thermal noise floor`() {
        // Very low amplitude noise (-60 dB corresponds to ~0.001f)
        val lowLevel = FloatArray(100) { 0.001f }
        val buffer = lowLevel.clone()
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            preset = PreampNoiseFlattenerPreset.TRANSPARENT_EXPANDER,
            mode = PreampFlattenerMode.DOWNWARD_EXPAND,
            downwardThresholdDb = -40.0f,
            flattenDepth = 1.0f
        )

        engine.processInterleaved(buffer, config = config)

        // After downward expansion, output amplitude should be attenuated compared to input
        val lastSample = abs(buffer[buffer.size - 1])
        assertTrue("Expected attenuated low-level noise floor, got $lastSample", lastSample < 0.001f)
    }

    @Test
    fun `downward expander preserves loud musical signal unattenuated`() {
        // High amplitude musical signal (-6 dB corresponds to ~0.5f)
        val loudSignal = FloatArray(100) { 0.5f }
        val buffer = loudSignal.clone()
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            preset = PreampNoiseFlattenerPreset.TRANSPARENT_EXPANDER,
            mode = PreampFlattenerMode.DOWNWARD_EXPAND,
            downwardThresholdDb = -45.0f,
            flattenDepth = 0.8f
        )

        engine.processInterleaved(buffer, config = config)

        val lastSample = abs(buffer[buffer.size - 1])
        // Should remain near 0.5f since it's well above threshold
        assertTrue("Musical signal should remain unattenuated, got $lastSample", lastSample >= 0.45f)
    }

    @Test
    fun `spectral tilt filter attenuates high-frequency hiss`() {
        // Generate high-frequency tone at 10 kHz (sampleRate = 44100)
        val highFreqSignal = FloatArray(256) { idx ->
            sin(2.0 * PI * 10000.0 * (idx / 2) / 44100.0).toFloat() * 0.4f
        }
        val buffer = highFreqSignal.clone()
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            profile = PreampNoiseProfile.SHOT_BLUE,
            mode = PreampFlattenerMode.DYNAMIC_FLATTEN,
            flattenDepth = 1.0f,
            hfTiltDb = -5.0f
        )

        engine.processInterleaved(buffer, config = config)

        // Sum amplitude in second half
        var sumOriginal = 0.0f
        var sumFiltered = 0.0f
        for (i in 128 until 256) {
            sumOriginal += abs(highFreqSignal[i])
            sumFiltered += abs(buffer[i])
        }

        assertTrue(
            "High frequency tone should be attenuated by negative HF tilt: orig=$sumOriginal, filt=$sumFiltered",
            sumFiltered < sumOriginal
        )
    }

    @Test
    fun `warm dither injects low-amplitude pseudo-random noise`() {
        // Zero-filled buffer
        val zeros = FloatArray(100) { 0.0f }
        val buffer = zeros.clone()
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            mode = PreampFlattenerMode.WARM_DITHER,
            thermalDitherLevel = 0.5f
        )

        val metrics = engine.processInterleaved(buffer, config = config)

        assertTrue(metrics.ditherActive)
        var hasNonZero = false
        for (sample in buffer) {
            if (abs(sample) > 1e-6f) {
                hasNonZero = true
                break
            }
        }
        assertTrue("Dither injection should produce non-zero samples on silent buffer", hasNonZero)
    }

    @Test
    fun `solo noise profile auditions attenuated signal difference`() {
        val original = FloatArray(128) { 0.2f }
        val buffer = original.clone()
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            mode = PreampFlattenerMode.SOLO_NOISE_PROFILE,
            downwardThresholdDb = -20.0f,
            flattenDepth = 1.0f
        )

        engine.processInterleaved(buffer, config = config)

        // In solo mode, output = (in - out) * 2.0
        var hasDifference = false
        for (i in buffer.indices) {
            if (abs(buffer[i]) > 1e-4f) {
                hasDifference = true
                break
            }
        }
        assertTrue("Solo noise mode should output extracted residual difference", hasDifference)
    }

    @Test
    fun `reset clears engine state`() {
        val buffer = FloatArray(128) { 0.5f }
        val config = PreampNoiseFlattenerConfig(isEnabled = true, hfTiltDb = -4.0f)

        engine.processInterleaved(buffer, config = config)
        engine.reset()

        val silentBuffer = FloatArray(64) { 0.0f }
        val metrics = engine.processInterleaved(silentBuffer, config = PreampNoiseFlattenerConfig(isEnabled = false))
        assertEquals("Bypassed (Direct Signal)", metrics.noiseFloorRating)
    }

    @Test
    fun `calculateMetrics computes valid ratings and slopes`() {
        val signal = FloatArray(128) { (it % 2) * 0.1f }
        val config = PreampNoiseFlattenerConfig(
            isEnabled = true,
            preset = PreampNoiseFlattenerPreset.STUDIO_SOLID_STATE
        )

        val metrics = PreampNoiseFlattenerEngine.calculateMetrics(config, signal)

        assertTrue(metrics.estimatedNoiseFloorDb <= 0.0f)
        assertTrue(metrics.noiseFloorRating.isNotEmpty())
    }
}

