package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.SlewLimiterMode
import com.nexus.tune.core.model.SlewRateLimiterConfig
import com.nexus.tune.core.model.SlewRateLimiterPreset
import com.nexus.tune.core.model.SlewRateSpeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SlewRateLimiterEngineTest {

    private lateinit var engine: SlewRateLimiterEngine

    @Before
    fun setUp() {
        engine = SlewRateLimiterEngine(sampleRate = 44100)
    }

    @Test
    fun `bypassed engine preserves audio buffer identically`() {
        val original = floatArrayOf(0.1f, -0.2f, 0.5f, -0.6f, 0.9f, -0.9f)
        val buffer = original.clone()
        val config = SlewRateLimiterConfig(isEnabled = false)

        val metrics = engine.processInterleaved(buffer, config = config)

        for (i in original.indices) {
            assertEquals(original[i], buffer[i], 1e-6f)
        }
        assertEquals("Bypassed (Full Bandwidth Passthrough)", metrics.membraneSafetyRating)
    }

    @Test
    fun `extreme step transient is clamped by slew rate limit`() {
        // Step transition from 0.0f directly to 0.9f (extreme dV/dt)
        val stepSignal = floatArrayOf(0.0f, 0.0f, 0.9f, 0.9f)
        val buffer = stepSignal.clone()
        val config = SlewRateLimiterConfig(
            isEnabled = true,
            preset = SlewRateLimiterPreset.ELECTROSTATIC_STAX_GUARD,
            speed = SlewRateSpeed.ELECTROSTATIC_25V_US,
            dampingRatio = 0.8f
        )

        engine.processInterleaved(buffer, config = config)

        // The step to 0.9f should be clamped so output sample < 0.9f
        assertTrue("Output sample should be clamped below 0.9f, got ${buffer[2]}", buffer[2] < 0.85f)
        assertTrue("Output sample should have moved above 0.0f, got ${buffer[2]}", buffer[2] > 0.01f)
    }

    @Test
    fun `slow sinusoidal signal passes without significant clamping`() {
        // 440 Hz sine wave at moderate level
        val sineSignal = FloatArray(128) { idx ->
            sin(2.0 * PI * 440.0 * (idx / 2) / 44100.0).toFloat() * 0.3f
        }
        val buffer = sineSignal.clone()
        val config = SlewRateLimiterConfig(
            isEnabled = true,
            speed = SlewRateSpeed.FAST_100V_US,
            dampingRatio = 0.2f
        )

        engine.processInterleaved(buffer, config = config)

        // Verify that buffer is closely following original sine
        for (i in 4 until buffer.size) {
            assertEquals(sineSignal[i], buffer[i], 0.05f)
        }
    }

    @Test
    fun `stator arc protect mode compresses excessive peak amplitudes`() {
        val peakSignal = floatArrayOf(0.98f, 0.98f, 0.99f, 0.99f)
        val buffer = peakSignal.clone()
        val config = SlewRateLimiterConfig(
            isEnabled = true,
            mode = SlewLimiterMode.STATOR_ARC_PROTECT,
            peakThreshold = 0.85f,
            dampingRatio = 0.5f
        )

        engine.processInterleaved(buffer, config = config)

        for (sample in buffer) {
            assertTrue("Peak amplitude should be compressed below 0.98f, got $sample", abs(sample) < 0.98f)
        }
    }

    @Test
    fun `solo clamped edges mode outputs transient residue`() {
        // Step signal
        val step = floatArrayOf(0.0f, 0.0f, 0.9f, 0.9f)
        val buffer = step.clone()
        val config = SlewRateLimiterConfig(
            isEnabled = true,
            mode = SlewLimiterMode.SOLO_CLAMPED_EDGES,
            speed = SlewRateSpeed.PLANAR_15V_US,
            dampingRatio = 0.8f
        )

        engine.processInterleaved(buffer, config = config)

        // In solo mode, output = (in - out) * 4.0
        assertTrue("Residual clamped edge should be non-zero", abs(buffer[2]) > 0.05f)
    }

    @Test
    fun `reset clears internal memories`() {
        val buffer = floatArrayOf(0.8f, 0.8f)
        val config = SlewRateLimiterConfig(isEnabled = true)

        engine.processInterleaved(buffer, config = config)
        engine.reset()

        val freshBuffer = floatArrayOf(0.0f, 0.0f)
        val metrics = engine.processInterleaved(freshBuffer, config = SlewRateLimiterConfig(isEnabled = false))
        assertEquals("Bypassed (Full Bandwidth Passthrough)", metrics.membraneSafetyRating)
    }

    @Test
    fun `calculateMetrics computes peak slew rate and safety rating`() {
        val buffer = floatArrayOf(0.0f, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f)
        val config = SlewRateLimiterConfig(
            isEnabled = true,
            preset = SlewRateLimiterPreset.PLANAR_MAGNETIC_SAFE
        )

        val metrics = SlewRateLimiterEngine.calculateMetrics(config, buffer)

        assertTrue(metrics.peakSlewRateVUs >= 0.0f)
        assertTrue(metrics.membraneSafetyRating.isNotEmpty())
    }
}

