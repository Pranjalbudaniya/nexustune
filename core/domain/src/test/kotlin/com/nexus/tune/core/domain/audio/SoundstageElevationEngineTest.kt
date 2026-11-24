package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.ElevationAngle
import com.nexus.tune.core.model.SoundstageElevationConfig
import com.nexus.tune.core.model.SoundstageElevationPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class SoundstageElevationEngineTest {

    private lateinit var engine: SoundstageElevationEngine
    private val sampleRate = 48000

    @Before
    fun setUp() {
        engine = SoundstageElevationEngine(sampleRate)
    }

    @Test
    fun `bypassed configuration preserves audio bit-perfect`() {
        val config = SoundstageElevationConfig(isEnabled = false)
        val original = FloatArray(512) { i -> sin(2.0 * PI * 1000.0 * i / sampleRate).toFloat() * 0.4f }
        val buffer = original.clone()

        val metrics = engine.process(buffer, config)

        for (i in buffer.indices) {
            assertEquals(original[i], buffer[i], 0.00001f)
        }
        assertEquals(0.0f, metrics.virtualElevationDegrees, 0.001f)
        assertEquals(0, metrics.spatialHeightScore)
    }

    @Test
    fun `pinna notch attenuates median plane notch frequency at 8500 Hz`() {
        val config = SoundstageElevationConfig(
            isEnabled = true,
            preset = SoundstageElevationPreset.CONCERT_HALL_BALCONY,
            elevationAngle = ElevationAngle.BALCONY_30,
            elevationIntensity = 1.0f,
            ceilingReflectionMs = 4.0f,
            pinnaNotchDepthDb = -6.0f,
            diffuseCeilingSpread = false
        )

        // Generate 8500 Hz tone (notch frequency for BALCONY_30)
        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 8500.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        engine.reset()
        engine.process(buffer, config)

        val tailStart = numSamples / 2
        var sumSquares = 0.0
        for (i in tailStart until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / (numSamples - tailStart))
        val originalRms = 0.4 / sqrt(2.0)

        assertTrue(
            "8500 Hz should be attenuated by pinna notch: rmsOut=$rmsOut, original=$originalRms",
            rmsOut < originalRms * 0.85
        )
    }

    @Test
    fun `midrange vocal frequency at 1000 Hz is preserved`() {
        val config = SoundstageElevationConfig(
            isEnabled = true,
            preset = SoundstageElevationPreset.VOCAL_FOREHEAD_LIFT,
            elevationAngle = ElevationAngle.STAGE_LIFT_15,
            elevationIntensity = 0.5f,
            ceilingReflectionMs = 3.5f,
            pinnaNotchDepthDb = -4.0f
        )

        val numSamples = 4800
        val buffer = FloatArray(numSamples) { i ->
            sin(2.0 * PI * 1000.0 * (i / 2) / sampleRate).toFloat() * 0.4f
        }

        engine.reset()
        engine.process(buffer, config)

        val tailStart = numSamples / 2
        var sumSquares = 0.0
        for (i in tailStart until numSamples) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rmsOut = sqrt(sumSquares / (numSamples - tailStart))
        val expectedRms = 0.4 / sqrt(2.0)

        assertTrue(
            "1000 Hz vocal foundation should remain intact: rmsOut=$rmsOut, expected=$expectedRms",
            rmsOut > expectedRms * 0.85
        )
    }

    @Test
    fun `hardware eq compensation reflects elevation angle`() {
        val disabled = SoundstageElevationConfig(isEnabled = false)
        assertEquals(0, engine.calculateHardwareEqCompensation(disabled))

        val dome = SoundstageElevationConfig(
            isEnabled = true,
            preset = SoundstageElevationPreset.ATMOS_HEIGHT_GLOW,
            elevationAngle = ElevationAngle.CATHEDRAL_DOME_45
        )
        assertEquals(70, engine.calculateHardwareEqCompensation(dome))

        val balcony = SoundstageElevationConfig(
            isEnabled = true,
            preset = SoundstageElevationPreset.CONCERT_HALL_BALCONY,
            elevationAngle = ElevationAngle.BALCONY_30
        )
        assertEquals(50, engine.calculateHardwareEqCompensation(balcony))
    }

    @Test
    fun `calculateMetrics companion helper produces expected score and angle`() {
        val config = SoundstageElevationConfig(
            isEnabled = true,
            preset = SoundstageElevationPreset.CONCERT_HALL_BALCONY,
            elevationAngle = ElevationAngle.BALCONY_30,
            elevationIntensity = 0.8f
        )
        val metrics = SoundstageElevationEngine.calculateMetrics(config)
        assertEquals(24.0f, metrics.virtualElevationDegrees, 0.1f)
        assertEquals(8500.0f, metrics.pinnaNotchFrequencyHz, 0.1f)
        assertTrue(metrics.spatialHeightScore > 45)
    }
}

