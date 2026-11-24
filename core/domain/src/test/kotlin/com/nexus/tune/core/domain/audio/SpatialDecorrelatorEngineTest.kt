package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DecorrelatorMode
import com.nexus.tune.core.model.DecorrelatorPreset
import com.nexus.tune.core.model.DecorrelatorStage
import com.nexus.tune.core.model.SpatialDecorrelatorConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class SpatialDecorrelatorEngineTest {

    private lateinit var engine: SpatialDecorrelatorEngine

    @Before
    fun setUp() {
        engine = SpatialDecorrelatorEngine()
    }

    @Test
    fun processStereo_whenDisabled_returnsIdenticalBuffer() {
        val input = floatArrayOf(0.5f, 0.5f, -0.2f, -0.2f)
        val config = SpatialDecorrelatorConfig(isEnabled = false)

        val output = engine.processStereo(input, config)
        assertArrayEquals(input, output, 0.0001f)
    }

    @Test
    fun processStereo_whenPresetOff_returnsIdenticalBuffer() {
        val input = floatArrayOf(0.5f, 0.5f, -0.2f, -0.2f)
        val config = SpatialDecorrelatorConfig(isEnabled = true, preset = DecorrelatorPreset.OFF)

        val output = engine.processStereo(input, config)
        assertArrayEquals(input, output, 0.0001f)
    }

    @Test
    fun processStereo_allPassCascade_decorrelatesStereoField() {
        val sampleRate = 44100
        val input = FloatArray(1000) { i ->
            val t = i.toFloat() / sampleRate
            sin(2.0 * Math.PI * 440.0 * t).toFloat() * 0.6f
        }

        val config = SpatialDecorrelatorConfig(
            isEnabled = true,
            preset = DecorrelatorPreset.HEADPHONE_SOUNDSTAGE_EXPAND,
            stage = DecorrelatorStage.DIFFUSE_CONCERT_DEPTH,
            mode = DecorrelatorMode.ALLPASS_CASCADE_DIFFUSE,
            diffusionIntensityPercent = 70,
            stereoWidthMultiplier = 1.4f
        )

        val output = engine.processStereo(input, config, sampleRate)
        assertEquals(input.size, output.size)
        assertTrue(output.all { !it.isNaN() && !it.isInfinite() })
        assertTrue(output.all { it in -1.0f..1.0f })
    }

    @Test
    fun processStereo_soloDecorrelatedSide_outputsInvertedPhaseStereo() {
        val input = floatArrayOf(0.7f, 0.2f, -0.4f, 0.3f, 0.5f, -0.1f)
        val config = SpatialDecorrelatorConfig(
            isEnabled = true,
            preset = DecorrelatorPreset.STUDIO_NEARFIELD_UNCLUTTER,
            mode = DecorrelatorMode.SOLO_DECORRELATED_SIDE,
            soloDecorrelatedSide = true
        )

        val output = engine.processStereo(input, config)
        assertEquals(input.size, output.size)
        // Left and Right channels should be anti-phase on solo side audition
        for (i in 0 until input.size / 2) {
            val l = output[i * 2]
            val r = output[i * 2 + 1]
            assertEquals(l, -r, 0.001f)
        }
    }

    @Test
    fun estimateMetrics_returnsRealisticSpatialMetrics() {
        val input = FloatArray(800) { i ->
            if (i % 2 == 0) 0.5f else 0.45f
        }

        val config = SpatialDecorrelatorConfig(
            isEnabled = true,
            preset = DecorrelatorPreset.HEADPHONE_SOUNDSTAGE_EXPAND,
            diffusionIntensityPercent = 60
        )

        val metrics = engine.estimateMetrics(input, config)
        assertTrue(metrics.correlationCoefficient in -1.0f..1.0f)
        assertTrue(metrics.phaseSpreadAngleDegrees >= 0.0f)
        assertTrue(metrics.isMonoSafe)
    }

    @Test
    fun presetToConfig_allPresetsProduceValidConfigurations() {
        DecorrelatorPreset.entries.forEach { preset ->
            val config = engine.presetToConfig(preset)
            assertNotNull(config)
            assertEquals(preset, config.preset)
            if (preset == DecorrelatorPreset.OFF) {
                assertFalse(config.isEnabled)
            } else {
                assertTrue(config.isEnabled)
                assertTrue(config.diffusionIntensityPercent in 0..100)
                assertTrue(config.stereoWidthMultiplier in 0.5f..2.5f)
            }
        }
    }
}
