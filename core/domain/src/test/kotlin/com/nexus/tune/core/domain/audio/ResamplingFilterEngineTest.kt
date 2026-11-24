package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.ResamplingFilterMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ResamplingFilterEngineTest {

    @Test
    fun testSincSample() {
        assertEquals(1.0f, ResamplingFilterEngine.calculateSincSample(0.0f), 0.0001f)
        assertEquals(0.0f, ResamplingFilterEngine.calculateSincSample(1.0f), 0.0001f)
        assertEquals(0.0f, ResamplingFilterEngine.calculateSincSample(-1.0f), 0.0001f)
        assertEquals((2.0 / Math.PI).toFloat(), ResamplingFilterEngine.calculateSincSample(0.5f), 0.001f)
    }

    @Test
    fun testGenerateFilterKernelNormalization() {
        val kernel = ResamplingFilterEngine.generateFilterKernel(
            length = 31,
            cutoffRatio = 0.95f,
            mode = ResamplingFilterMode.MINIMUM_PHASE_PUNCH
        )
        assertEquals(31, kernel.size)
        val sum = kernel.sum()
        assertEquals(1.0f, sum, 0.001f)
    }

    @Test
    fun testLinearPhaseSymmetry() {
        val n = 31
        val kernel = ResamplingFilterEngine.generateFilterKernel(
            length = n,
            cutoffRatio = 0.95f,
            mode = ResamplingFilterMode.LINEAR_PHASE_REFERENCE
        )
        for (i in 0 until n / 2) {
            val left = kernel[i]
            val right = kernel[n - 1 - i]
            assertEquals("Symmetry mismatch at index $i", left, right, 0.0001f)
        }
    }

    @Test
    fun testMinimumPhasePreRingingSuppression() {
        val n = 31
        val kernel = ResamplingFilterEngine.generateFilterKernel(
            length = n,
            cutoffRatio = 0.95f,
            mode = ResamplingFilterMode.MINIMUM_PHASE_PUNCH
        )
        // Pre-echo (left side) is suppressed compared to post-echo (right side)
        val leftEnergy = (0 until 10).map { kernel[it] * kernel[it] }.sum()
        val rightEnergy = (21 until 31).map { kernel[it] * kernel[it] }.sum()
        assertTrue("Pre-ringing energy should be significantly smaller than post-ringing", leftEnergy < rightEnergy)
    }

    @Test
    fun testImpulseMetrics() {
        val minPhase = ResamplingFilterEngine.calculateImpulseMetrics(ResamplingFilterMode.MINIMUM_PHASE_PUNCH)
        assertEquals(0, minPhase.preRingingPercent)
        assertEquals(100, minPhase.postRingingPercent)
        assertEquals(110.0f, minPhase.stopbandRejectionDb, 0.01f)

        val linearPhase = ResamplingFilterEngine.calculateImpulseMetrics(ResamplingFilterMode.LINEAR_PHASE_REFERENCE)
        assertEquals(50, linearPhase.preRingingPercent)
        assertEquals(50, linearPhase.postRingingPercent)
        assertEquals(130.0f, linearPhase.stopbandRejectionDb, 0.01f)
    }

    @Test
    fun testComputeKaiserBeta() {
        val beta100 = ResamplingFilterEngine.computeKaiserBeta(100.0f)
        assertEquals(10.06126f, beta100, 0.01f)

        val betaLow = ResamplingFilterEngine.computeKaiserBeta(15.0f)
        assertEquals(0.0f, betaLow, 0.001f)
    }
}

