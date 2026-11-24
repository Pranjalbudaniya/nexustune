package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class TruePeakGuardEngineTest {

    @Test
    fun `dBTP and linear conversions are exact inverses`() {
        val linear = 0.5f
        val dbTp = TruePeakGuardEngine.linearToDbTp(linear)
        val inverted = TruePeakGuardEngine.dbTpToLinear(dbTp)
        assertEquals(linear, inverted, 0.001f)
    }

    @Test
    fun `estimateTruePeak detects inter-sample peak excursions higher than sample peaks`() {
        // Synthesizing a classic pathological ISP waveform: two successive near-full-scale samples
        // [0.0, 0.95, 0.95, 0.0, -0.95, -0.95, 0.0] where continuous analog curve between (0.95, 0.95) overshoots 1.0 (>0 dBFS)
        val samples = floatArrayOf(0.0f, 0.95f, 0.95f, 0.0f, -0.95f, -0.95f, 0.0f)
        val samplePeak = TruePeakGuardEngine.estimateTruePeak(samples, oversample4x = false)
        val truePeak = TruePeakGuardEngine.estimateTruePeak(samples, oversample4x = true)

        assertEquals(0.95f, samplePeak, 0.001f)
        assertTrue("True Peak with 4x oversampling should detect interpolation overshoot, was $truePeak", truePeak > samplePeak)
    }

    @Test
    fun `guard attenuation clamps true peak beneath target ceiling`() {
        val estimatedTruePeak = 0.8f // +0.8 dBTP
        val ceiling = TruePeakCeiling.CEILING_MINUS_0_5_DBTP // -0.5 dBTP

        val atten = TruePeakGuardEngine.calculateGuardAttenuationDb(estimatedTruePeak, ceiling, isEnabled = true)
        assertEquals(-1.3f, atten, 0.01f)

        val linearMult = TruePeakGuardEngine.calculateGuardLinearMultiplier(estimatedTruePeak, ceiling, isEnabled = true)
        assertTrue(linearMult < 1.0f)
        assertTrue(linearMult > 0.0f)
    }

    @Test
    fun `calculateMetrics produces correct ratings when active vs bypassed`() {
        val bypassed = TruePeakGuardConfig(isEnabled = false)
        val bypassedMetrics = TruePeakGuardEngine.calculateMetrics(bypassed)
        assertEquals(0.0f, bypassedMetrics.appliedGuardAttenuationDb, 0.001f)
        assertTrue(bypassedMetrics.dacHeadroomRating.contains("Bypassed"))

        val active = TruePeakGuardConfig(
            isEnabled = true,
            mode = TruePeakMode.EBU_R128_STANDARD,
            ceiling = TruePeakCeiling.CEILING_MINUS_1_0_DBTP,
            oversample4xEnabled = true
        )
        val activeMetrics = TruePeakGuardEngine.calculateMetrics(active, nominalSamplePeak = 0.99f, nominalTruePeak = 1.10f)
        assertTrue(activeMetrics.interSampleOvershootDb > 0.0f)
        assertTrue(activeMetrics.appliedGuardAttenuationDb < 0.0f)
        assertTrue(activeMetrics.dacHeadroomRating.contains("ISP Protection") || activeMetrics.dacHeadroomRating.contains("Clamping"))
    }
}

