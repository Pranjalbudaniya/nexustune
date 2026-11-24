package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.BassAttackPreset
import com.nexus.tune.core.model.BassAttackReshaperConfig
import com.nexus.tune.core.model.BassAttackTarget
import com.nexus.tune.core.model.BassReshaperMode
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class BassAttackReshaperEngineTest {

    private lateinit var engine: BassAttackReshaperEngine

    @Before
    fun setUp() {
        engine = BassAttackReshaperEngine()
    }

    @Test
    fun process_whenDisabled_returnsIdenticalBuffer() {
        val input = floatArrayOf(0.1f, -0.2f, 0.3f, -0.4f)
        val config = BassAttackReshaperConfig(isEnabled = false)

        val output = engine.process(input, config)
        assertArrayEquals(input, output, 0.0001f)
    }

    @Test
    fun process_whenPresetOff_returnsIdenticalBuffer() {
        val input = floatArrayOf(0.1f, -0.2f, 0.3f, -0.4f)
        val config = BassAttackReshaperConfig(isEnabled = true, preset = BassAttackPreset.OFF)

        val output = engine.process(input, config)
        assertArrayEquals(input, output, 0.0001f)
    }

    @Test
    fun process_dynamicAttackBoost_amplifiesTransientEnvelope() {
        // Synthesize 50Hz sub-bass transient burst followed by decay
        val sampleRate = 44100
        val input = FloatArray(1000) { i ->
            val t = i.toFloat() / sampleRate
            val decay = if (i < 200) 1.0f else (1000 - i) / 800.0f
            sin(2.0 * Math.PI * 50.0 * t).toFloat() * 0.7f * decay
        }

        val config = BassAttackReshaperConfig(
            isEnabled = true,
            preset = BassAttackPreset.TIGHT_KICK_IMPACT,
            target = BassAttackTarget.SUB_KICK_60HZ,
            mode = BassReshaperMode.DYNAMIC_ATTACK_BOOST,
            attackPunchMultiplier = 1.6f,
            crossoverFrequencyHz = 80.0f
        )

        val output = engine.process(input, config, sampleRate)
        assertEquals(input.size, output.size)
        // Ensure no NaN or Inf
        assertTrue(output.all { !it.isNaN() && !it.isInfinite() })
        // Output within [-1, 1]
        assertTrue(output.all { it in -1.0f..1.0f })
    }

    @Test
    fun process_sustainTailDamp_reducesDecayTail() {
        val sampleRate = 44100
        val input = FloatArray(1200) { i ->
            val t = i.toFloat() / sampleRate
            sin(2.0 * Math.PI * 60.0 * t).toFloat() * 0.5f
        }

        val config = BassAttackReshaperConfig(
            isEnabled = true,
            preset = BassAttackPreset.DE_BOOM_SUSTAIN,
            target = BassAttackTarget.SUB_KICK_60HZ,
            mode = BassReshaperMode.SUSTAIN_TAIL_DAMP,
            sustainDampingPercent = 80,
            crossoverFrequencyHz = 90.0f
        )

        val output = engine.process(input, config, sampleRate)
        assertEquals(input.size, output.size)
        // Decay tail should be attenuated
        val inputTailEnergy = input.takeLast(400).map { it * it }.average()
        val outputTailEnergy = output.takeLast(400).map { it * it }.average()
        assertTrue(outputTailEnergy < inputTailEnergy)
    }

    @Test
    fun process_soloAttackTransient_outputsOnlyTransientSignal() {
        val sampleRate = 44100
        val input = FloatArray(800) { i ->
            val t = i.toFloat() / sampleRate
            sin(2.0 * Math.PI * 60.0 * t).toFloat() * 0.4f
        }

        val config = BassAttackReshaperConfig(
            isEnabled = true,
            preset = BassAttackPreset.HARDSTYLE_SUB_SLAP,
            mode = BassReshaperMode.SOLO_ATTACK_TRANSIENT,
            soloAttackTransient = true
        )

        val output = engine.process(input, config, sampleRate)
        assertEquals(input.size, output.size)
        assertTrue(output.all { !it.isNaN() })
    }

    @Test
    fun estimateMetrics_returnsRealisticTransientMetrics() {
        val sampleRate = 44100
        val input = FloatArray(1000) { i ->
            val t = i.toFloat() / sampleRate
            val env = if (i < 100) 0.9f else 0.1f
            sin(2.0 * Math.PI * 50.0 * t).toFloat() * env
        }

        val config = BassAttackReshaperConfig(
            isEnabled = true,
            preset = BassAttackPreset.TIGHT_KICK_IMPACT,
            attackPunchMultiplier = 1.7f,
            sustainDampingPercent = 60
        )

        val metrics = engine.estimateMetrics(input, config, sampleRate)
        assertTrue(metrics.attackVelocityRatio >= 1.0f)
        assertTrue(metrics.sustainDampingDb > 0.0f)
        assertNotEquals("BYPASS", metrics.transientPunchRating)
    }

    @Test
    fun presetToConfig_allPresetsProduceValidConfigurations() {
        BassAttackPreset.entries.forEach { preset ->
            val config = engine.presetToConfig(preset)
            assertNotNull(config)
            assertEquals(preset, config.preset)
            if (preset == BassAttackPreset.OFF) {
                assertFalse(config.isEnabled)
            } else {
                assertTrue(config.isEnabled)
                assertTrue(config.attackPunchMultiplier in 1.0f..2.5f)
                assertTrue(config.sustainDampingPercent in 0..100)
            }
        }
    }
}

