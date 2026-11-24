package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.MonoBassAlignConfig
import com.nexus.tune.core.model.MonoBassAlignPreset
import com.nexus.tune.core.model.MonoBassCrossover
import com.nexus.tune.core.model.MonoBassFilterSlope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class MonoBassAlignEngineTest {

    private lateinit var engine: MonoBassAlignEngine
    private val sampleRate = 48000

    @Before
    fun setUp() {
        engine = MonoBassAlignEngine(sampleRate)
    }

    @Test
    fun `bypassed configuration preserves audio bit-perfect`() {
        val config = MonoBassAlignConfig(isEnabled = false)
        val original = FloatArray(512) { i -> sin(2.0 * PI * 1000.0 * i / sampleRate).toFloat() * 0.4f }
        val buffer = original.clone()

        val metrics = engine.process(buffer, config)

        for (i in buffer.indices) {
            assertEquals(original[i], buffer[i], 0.00001f)
        }
        assertEquals(0.0f, metrics.restoredEnergyDb, 0.001f)
    }

    @Test
    fun `measureSubBassCorrelation correctly measures in-phase and out-of-phase`() {
        // Pure in-phase stereo
        val inPhase = FloatArray(1024) { i -> sin(2.0 * PI * 60.0 * (i / 2) / sampleRate).toFloat() * 0.5f }
        val corrInPhase = engine.measureSubBassCorrelation(inPhase)
        assertEquals(1.0f, corrInPhase, 0.01f)

        // Pure out-of-phase stereo (L = +s, R = -s)
        val outOfPhase = FloatArray(1024) { i ->
            val s = sin(2.0 * PI * 60.0 * (i / 2) / sampleRate).toFloat() * 0.5f
            if (i % 2 == 0) s else -s
        }
        val corrOutOfPhase = engine.measureSubBassCorrelation(outOfPhase)
        assertEquals(-1.0f, corrOutOfPhase, 0.01f)
    }

    @Test
    fun `out-of-phase sub-bass below crossover is filtered out`() {
        val config = MonoBassAlignConfig(
            isEnabled = true,
            preset = MonoBassAlignPreset.SUBWOOFER_MONO_LOCK_80HZ,
            crossover = MonoBassCrossover.HZ_80,
            slope = MonoBassFilterSlope.SLOPE_24DB_OCT,
            subPunchGainDb = 0.0f
        )

        // Pure out-of-phase 40 Hz sub-bass (L = +s, R = -s)
        // In this case, Mid = (L+R) = 0, Side = (L-R) = 2s
        // Because 40 Hz is well below 80 Hz crossover, the Side channel is high-passed out!
        // Output should be greatly attenuated towards 0.
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            val s = sin(2.0 * PI * 40.0 * (i / 2) / sampleRate).toFloat() * 0.4f
            if (i % 2 == 0) s else -s
        }

        engine.reset()
        engine.process(buffer, config)

        val tailStart = numSamples / 2
        var sumSquares = 0.0
        for (i in tailStart until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / (numSamples - tailStart))

        assertTrue("Out-of-phase sub-bass below crossover should be filtered: rmsOut=$rmsOut", rmsOut < 0.10)
    }

    @Test
    fun `high frequency stereo separation above crossover is preserved`() {
        val config = MonoBassAlignConfig(
            isEnabled = true,
            preset = MonoBassAlignPreset.SUBWOOFER_MONO_LOCK_80HZ,
            crossover = MonoBassCrossover.HZ_80,
            slope = MonoBassFilterSlope.SLOPE_24DB_OCT,
            subPunchGainDb = 0.0f
        )

        // Hard-panned 1000 Hz tone on Left channel only (Right = 0)
        // Highpass on Side channel at 80 Hz leaves 1000 Hz Side 100% intact!
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            if (i % 2 == 0) sin(2.0 * PI * 1000.0 * (i / 2) / sampleRate).toFloat() * 0.4f else 0.0f
        }

        engine.reset()
        engine.process(buffer, config)

        val tailStart = numSamples / 2
        var leftEnergy = 0.0
        var rightEnergy = 0.0
        for (i in tailStart until numSamples step 2) {
            leftEnergy += buffer[i] * buffer[i]
            rightEnergy += buffer[i + 1] * buffer[i + 1]
        }

        // Left channel should have vastly more energy than right channel (hard pan preserved)
        assertTrue("Hard-panned 1000 Hz Left channel should dominate: L=$leftEnergy, R=$rightEnergy", leftEnergy > rightEnergy * 8.0)
    }

    @Test
    fun `hardware eq compensation reflects crossover frequency`() {
        val disabled = MonoBassAlignConfig(isEnabled = false)
        assertEquals(0, engine.calculateHardwareEqCompensation(disabled))

        val lock150 = MonoBassAlignConfig(
            isEnabled = true,
            crossover = MonoBassCrossover.HZ_150
        )
        assertEquals(80, engine.calculateHardwareEqCompensation(lock150))

        val lock60 = MonoBassAlignConfig(
            isEnabled = true,
            crossover = MonoBassCrossover.HZ_60
        )
        assertEquals(20, engine.calculateHardwareEqCompensation(lock60))
    }

    @Test
    fun `calculateMetrics companion helper returns appropriate rating`() {
        val config = MonoBassAlignConfig(
            isEnabled = true,
            preset = MonoBassAlignPreset.SUBWOOFER_MONO_LOCK_80HZ,
            crossover = MonoBassCrossover.HZ_80
        )
        val metrics = MonoBassAlignEngine.calculateMetrics(config)
        assertEquals(80.0f, metrics.crossoverFrequencyHz, 0.1f)
        assertTrue(metrics.coherenceRating.contains("THX Subwoofer"))
    }
}

