package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class TransientShaperEngineTest {

    @Test
    fun `bypassed when disabled returns identical input sample`() {
        val config = TransientShaperConfig(isEnabled = false)
        val sample = 0.543f
        val result = TransientShaperEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `bypassed when preset is off returns identical input sample`() {
        val config = TransientShaperConfig(isEnabled = true, preset = TransientPreset.OFF)
        val sample = -0.712f
        val result = TransientShaperEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `attack boost amplifies fast velocity transient impulse`() {
        val config = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.CRISP_DRUM_PUNCH,
            attackBoost = 0.8f,
            sustainBoost = 0.0f,
            autoOutputLimit = false
        )
        val state = TransientShaperEngine.EnvelopeFollowerState()

        // Prime the follower with silence
        for (i in 0 until 100) {
            TransientShaperEngine.processSample(0.0f, config, state)
        }

        // Sudden fast transient impulse
        val impulse = 0.5f
        val output = TransientShaperEngine.processSample(impulse, config, state)

        assertTrue(
            "Attack boost must amplify instantaneous leading transient (output > input)",
            output > impulse
        )
    }

    @Test
    fun `attack reduction softens fast velocity transient impulse`() {
        val config = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.MELLOW_FATIGUE_SOFTENER,
            attackBoost = -0.7f,
            sustainBoost = 0.0f,
            autoOutputLimit = false
        )
        val state = TransientShaperEngine.EnvelopeFollowerState()

        // Prime follower with silence
        for (i in 0 until 100) {
            TransientShaperEngine.processSample(0.0f, config, state)
        }

        val impulse = 0.5f
        val output = TransientShaperEngine.processSample(impulse, config, state)

        assertTrue(
            "Attack reduction must soften leading transient (output < input)",
            output < impulse
        )
    }

    @Test
    fun `sustain boost amplifies steady state sustain after transient settles`() {
        val config = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.BASS_IMPACT_BOOST,
            attackBoost = 0.0f,
            sustainBoost = 0.8f,
            autoOutputLimit = false
        )
        val state = TransientShaperEngine.EnvelopeFollowerState()

        // Settle follower with a high level step
        for (i in 0 until 500) {
            TransientShaperEngine.processSample(0.8f, config, state)
        }

        // Now drop to sustain level
        val sustainSample = 0.3f
        val output = TransientShaperEngine.processSample(sustainSample, config, state)

        assertTrue(
            "Sustain boost should amplify falling body decay",
            output >= sustainSample
        )
    }

    @Test
    fun `band focus applies transient processing cleanly without destabilizing`() {
        val config = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.BASS_IMPACT_BOOST,
            bandFocus = TransientBandFocus.BASS_PUNCH_LOW,
            attackBoost = 0.5f,
            sustainBoost = 0.2f
        )
        val state = TransientShaperEngine.EnvelopeFollowerState()

        val input = floatArrayOf(0.1f, 0.4f, 0.8f, 0.3f, 0.1f)
        for (sample in input) {
            val out = TransientShaperEngine.processSample(sample, config, state)
            assertFalse("Output must never be NaN", out.isNaN())
            assertTrue("Output must stay bounded in [-1.0, 1.0]", out in -1.0f..1.0f)
        }
    }

    @Test
    fun `hardware transient offset scales with attack and preset`() {
        val disabled = TransientShaperConfig(isEnabled = false)
        assertEquals(0, TransientShaperEngine.calculateHardwareTransientOffset(disabled))

        val active = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.CRISP_DRUM_PUNCH,
            attackBoost = 0.6f
        )
        val offset = TransientShaperEngine.calculateHardwareTransientOffset(active)
        assertTrue("Active transient offset must be > 0 mB for positive attack boost", offset > 0)
    }

    @Test
    fun `metrics calculation reflects attack, sustain, and crest factor changes`() {
        val bypassedConfig = TransientShaperConfig(isEnabled = false)
        val bypassedMetrics = TransientShaperEngine.calculateMetrics(bypassedConfig)
        assertEquals(0.0f, bypassedMetrics.attackDeltaDb, 1e-3f)
        assertEquals(0.0f, bypassedMetrics.sustainDeltaDb, 1e-3f)
        assertTrue(bypassedMetrics.punchRating.contains("Bypassed"))

        val punchyConfig = TransientShaperConfig(
            isEnabled = true,
            preset = TransientPreset.CRISP_DRUM_PUNCH,
            attackBoost = 0.5f,
            sustainBoost = -0.2f
        )
        val punchyMetrics = TransientShaperEngine.calculateMetrics(punchyConfig)
        assertTrue("Attack delta should be positive", punchyMetrics.attackDeltaDb > 0.0f)
        assertTrue("Sustain delta should be negative", punchyMetrics.sustainDeltaDb < 0.0f)
        assertTrue("Punch score should be > 50", punchyMetrics.dynamicPunchScore > 50)
        assertTrue("Crest factor should be elevated", punchyMetrics.crestFactorDb > 13.0f)
    }
}

