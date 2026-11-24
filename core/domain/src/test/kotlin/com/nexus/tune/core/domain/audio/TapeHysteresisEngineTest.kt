package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class TapeHysteresisEngineTest {

    @Test
    fun `bypassed when disabled returns identical input sample`() {
        val config = TapeHysteresisConfig(isEnabled = false)
        val sample = 0.654f
        val result = TapeHysteresisEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `bypassed when preset is off returns identical input sample`() {
        val config = TapeHysteresisConfig(isEnabled = true, preset = TapePreset.OFF)
        val sample = -0.543f
        val result = TapeHysteresisEngine.processSample(sample, config)
        assertEquals(sample, result, 1e-6f)
    }

    @Test
    fun `head bump filter boosts low frequencies near center resonance`() {
        val config = TapeHysteresisConfig(
            isEnabled = true,
            preset = TapePreset.SUB_BASS_HEAD_BUMP,
            speed = TapeSpeed.IPS_15,
            headBumpGainDb = 4.0f,
            tapeDrive = 0.0f,
            wowFlutterDepth = 0.0f
        )
        val state = TapeHysteresisEngine.TapeState(sampleRate = 48000)

        // Test with 55 Hz sine wave
        val sampleRate = 48000.0
        val freq = 55.0
        var maxIn = 0.0f
        var maxOut = 0.0f

        for (i in 0 until 1000) {
            val sample = (0.5 * sin(2.0 * Math.PI * freq * i / sampleRate)).toFloat()
            val out = TapeHysteresisEngine.processSample(sample, config, state)
            if (i > 500) {
                maxIn = maxOf(maxIn, abs(sample))
                maxOut = maxOf(maxOut, abs(out))
            }
        }

        assertTrue("Head bump resonance must amplify 55 Hz signal (output > input)", maxOut > maxIn)
    }

    @Test
    fun `magnetic hysteresis applies soft progressive compression`() {
        val config = TapeHysteresisConfig(
            isEnabled = true,
            preset = TapePreset.HOT_SATURATED_TAPE,
            tapeDrive = 0.8f,
            headBumpGainDb = 0.0f,
            wowFlutterDepth = 0.0f
        )

        val smallIn = 0.1f
        val largeIn = 0.9f

        val smallOut = TapeHysteresisEngine.processSample(smallIn, config)
        val largeOut = TapeHysteresisEngine.processSample(largeIn, config)

        val gainSmall = smallOut / smallIn
        val gainLarge = largeOut / largeIn

        assertTrue("Tape saturation must compress large amplitudes more than small (soft knee)", gainLarge < gainSmall)
    }

    @Test
    fun `wow and flutter applies continuous micro-delay modulation`() {
        val state = TapeHysteresisEngine.TapeState(sampleRate = 48000)
        // Feed constant DC step
        for (i in 0 until 200) {
            state.processWowFlutter(0.7f, 0.5f)
        }
        val out = state.processWowFlutter(0.7f, 0.5f)
        assertTrue("Wow flutter output must settle around the input value", abs(out - 0.7f) < 0.05f)
    }

    @Test
    fun `processBuffer produces bounded audio samples`() {
        val config = TapeHysteresisConfig(
            isEnabled = true,
            preset = TapePreset.VINTAGE_WARM_15_IPS,
            tapeDrive = 0.9f,
            headBumpGainDb = 3.5f,
            wowFlutterDepth = 0.4f
        )
        val input = floatArrayOf(-1.0f, -0.6f, 0.0f, 0.6f, 1.0f)
        val output = TapeHysteresisEngine.processBuffer(input, config)

        assertEquals(input.size, output.size)
        for (sample in output) {
            assertFalse("Sample must not be NaN", sample.isNaN())
            assertTrue("Sample must stay within safe audio limits [-1.0, 1.0]", sample in -1.0f..1.0f)
        }
    }

    @Test
    fun `hardware tape offset scales with drive and head bump`() {
        val disabled = TapeHysteresisConfig(isEnabled = false)
        assertEquals(0, TapeHysteresisEngine.calculateHardwareTapeOffset(disabled))

        val active = TapeHysteresisConfig(
            isEnabled = true,
            preset = TapePreset.VINTAGE_WARM_15_IPS,
            headBumpGainDb = 3.0f,
            tapeDrive = 0.6f
        )
        val offset = TapeHysteresisEngine.calculateHardwareTapeOffset(active)
        assertTrue("Active tape warmth offset must be > 0 mB", offset > 0)
        assertTrue("Active tape warmth offset must be <= 300 mB", offset <= 300)
    }

    @Test
    fun `metrics calculation reflects tape speed, head bump, and analog warmth`() {
        val bypassed = TapeHysteresisConfig(isEnabled = false)
        val bypassedMetrics = TapeHysteresisEngine.calculateMetrics(bypassed)
        assertEquals(0.0f, bypassedMetrics.saturationCompressionDb, 1e-3f)
        assertEquals(0, bypassedMetrics.analogWarmthScore)
        assertTrue(bypassedMetrics.tapeCharacterRating.contains("Bypassed"))

        val active = TapeHysteresisConfig(
            isEnabled = true,
            preset = TapePreset.VINTAGE_WARM_15_IPS,
            speed = TapeSpeed.IPS_15,
            tapeDrive = 0.5f,
            headBumpGainDb = 2.5f,
            wowFlutterDepth = 0.3f
        )
        val metrics = TapeHysteresisEngine.calculateMetrics(active)
        assertEquals(55.0f, metrics.headBumpFrequencyHz, 1e-3f)
        assertTrue("Compression should be negative dB", metrics.saturationCompressionDb < 0.0f)
        assertTrue("Warmth score should be > 30", metrics.analogWarmthScore > 30)
        assertTrue("Wow flutter PPM should be > 0", metrics.wowFlutterPpm > 0)
    }
}

