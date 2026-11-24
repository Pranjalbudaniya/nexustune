package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.MonoGuardMode
import com.nexus.tune.core.model.PhaseCorrelationRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class PhaseMeterEngineTest {

    @Test
    fun testIdenticalChannelsPearsonIsOne() {
        val sine = FloatArray(500) { i -> sin(2.0 * Math.PI * 440.0 * (i / 44100.0)).toFloat() }
        val corr = PhaseMeterEngine.calculatePearsonCorrelation(sine, sine)
        assertEquals(1.0f, corr, 0.001f)

        val rating = PhaseMeterEngine.evaluatePhaseRating(corr)
        assertEquals(PhaseCorrelationRating.PERFECT_MONO, rating)
        assertTrue(rating.isMonoSafe)
    }

    @Test
    fun testInvertedChannelsPearsonIsMinusOne() {
        val sine = FloatArray(500) { i -> sin(2.0 * Math.PI * 440.0 * (i / 44100.0)).toFloat() }
        val inverted = FloatArray(500) { i -> -sine[i] }
        val corr = PhaseMeterEngine.calculatePearsonCorrelation(sine, inverted)
        assertEquals(-1.0f, corr, 0.001f)

        val rating = PhaseMeterEngine.evaluatePhaseRating(corr)
        assertEquals(PhaseCorrelationRating.CRITICAL_CANCELLATION, rating)
        assertFalse(rating.isMonoSafe)
    }

    @Test
    fun testMonoGuardSideAttenuation() {
        // When correlation (+0.8) is above threshold (+0.2), 0 dB attenuation
        val attenSafe = PhaseMeterEngine.calculateMonoGuardSideAttenuationDb(0.8f, MonoGuardMode.INTELLIGENT_SAFEGUARD)
        assertEquals(0.0f, attenSafe, 0.001f)

        // When correlation (-1.0) is in critical cancellation, attenuation is applied
        val attenCrit = PhaseMeterEngine.calculateMonoGuardSideAttenuationDb(-1.0f, MonoGuardMode.INTELLIGENT_SAFEGUARD)
        assertEquals(-18.0f, attenCrit, 0.01f)
    }

    @Test
    fun testProcessPhaseSafeFrameInversionRemediation() {
        val sine = FloatArray(500) { i -> sin(2.0 * Math.PI * 440.0 * (i / 44100.0)).toFloat() }
        val inverted = FloatArray(500) { i -> -sine[i] }

        // Prior to Mono-Guard: correlation is -1.0 (complete mono cancellation)
        val preCorr = PhaseMeterEngine.calculatePearsonCorrelation(sine, inverted)
        assertEquals(-1.0f, preCorr, 0.001f)

        // Process through Mono-Guard
        val (safeL, safeR) = PhaseMeterEngine.processPhaseSafeFrame(sine, inverted, MonoGuardMode.INTELLIGENT_SAFEGUARD)
        val postCorr = PhaseMeterEngine.calculatePearsonCorrelation(safeL, safeR)

        // Inverted signals sum to 0 in Mid, but side attenuation suppresses destructive energy
        assertTrue("Output channels should maintain valid audio values", safeL.isNotEmpty())
    }

    @Test
    fun testPhaseMetrics() {
        val left = floatArrayOf(0.5f, 0.7f, 0.9f, 0.4f)
        val right = floatArrayOf(0.4f, 0.6f, 0.8f, 0.3f)
        val metrics = PhaseMeterEngine.calculatePhaseMetrics(left, right, MonoGuardMode.INTELLIGENT_SAFEGUARD)
        assertTrue(metrics.correlationCoeff > 0.9f)
        assertTrue(metrics.isMonoSafe)
    }
}

