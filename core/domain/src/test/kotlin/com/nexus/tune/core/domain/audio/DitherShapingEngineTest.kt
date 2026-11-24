package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DitherMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DitherShapingEngineTest {

    @Test
    fun testTpdfRangeAndDistribution() {
        val samples = (0 until 1000).map { DitherShapingEngine.generateTpdfSample() }
        for (sample in samples) {
            assertTrue("TPDF sample out of bounds: $sample", sample >= -1.0f && sample <= 1.0f)
        }
        val mean = samples.average().toFloat()
        assertTrue("TPDF mean should be close to 0.0: $mean", abs(mean) < 0.1f)
    }

    @Test
    fun testRpdfRangeAndDistribution() {
        val samples = (0 until 1000).map { DitherShapingEngine.generateRpdfSample() }
        for (sample in samples) {
            assertTrue("RPDF sample out of bounds: $sample", sample >= -0.5f && sample <= 0.5f)
        }
        val mean = samples.average().toFloat()
        assertTrue("RPDF mean should be close to 0.0: $mean", abs(mean) < 0.1f)
    }

    @Test
    fun testNoiseShapingCoefficients() {
        val lipshitz = DitherShapingEngine.getNoiseShapingCoefficients(DitherMode.PSYCHOACOUSTIC_LIPSHITZ)
        assertEquals(2, lipshitz.size)
        assertEquals(1.62f, lipshitz[0], 0.001f)
        assertEquals(-0.93f, lipshitz[1], 0.001f)

        val highPass = DitherShapingEngine.getNoiseShapingCoefficients(DitherMode.HIGH_PASS_CONTOUR)
        assertEquals(1, highPass.size)
        assertEquals(0.95f, highPass[0], 0.001f)

        val off = DitherShapingEngine.getNoiseShapingCoefficients(DitherMode.OFF)
        assertEquals(0, off.size)

        val tpdf = DitherShapingEngine.getNoiseShapingCoefficients(DitherMode.TRIANGULAR_TPDF)
        assertEquals(0, tpdf.size)
    }

    @Test
    fun testQuantizeOffDirectTruncation() {
        val targetBits = 4 // maxLevel = 7
        val q1 = DitherShapingEngine.quantizeSample(0.5f, targetBits = targetBits, mode = DitherMode.OFF)
        // 0.5 * 7 = 3.5 -> round = 4.0 -> 4/7 = 0.5714f
        assertEquals(4f / 7f, q1, 0.001f)

        val q0 = DitherShapingEngine.quantizeSample(0.0f, targetBits = targetBits, mode = DitherMode.OFF)
        assertEquals(0.0f, q0, 0.001f)
    }

    @Test
    fun testQuantizeWithTpdfBounded() {
        val output = DitherShapingEngine.quantizeSample(
            input = 0.75f,
            targetBits = 16,
            mode = DitherMode.TRIANGULAR_TPDF
        )
        assertTrue(output in -1.0f..1.0f)
    }

    @Test
    fun testApparentDynamicRange() {
        val dr16Base = DitherShapingEngine.calculateApparentDynamicRange(16, DitherMode.OFF)
        assertEquals(98.08f, dr16Base, 0.01f)

        val dr16Lipshitz = DitherShapingEngine.calculateApparentDynamicRange(16, DitherMode.PSYCHOACOUSTIC_LIPSHITZ)
        assertEquals(98.08f + 18.0f, dr16Lipshitz, 0.01f)

        val dr16HighPass = DitherShapingEngine.calculateApparentDynamicRange(16, DitherMode.HIGH_PASS_CONTOUR)
        assertEquals(98.08f + 6.0f, dr16HighPass, 0.01f)
    }

    @Test
    fun testNoiseMetrics() {
        val metrics = DitherShapingEngine.calculateNoiseMetrics(16, DitherMode.PSYCHOACOUSTIC_LIPSHITZ)
        assertEquals(98.08f, metrics.theoreticalDynamicRangeDb, 0.01f)
        assertEquals(116.08f, metrics.effectiveSnrDb, 0.01f)
        assertEquals(18.0f, metrics.noiseShapingGainDb, 0.01f)
    }

    @Test
    fun testProcessBuffer() {
        val input = floatArrayOf(-0.9f, -0.5f, 0.0f, 0.3f, 0.8f, 1.0f)
        val processed = DitherShapingEngine.processBuffer(
            input = input,
            targetBits = 16,
            mode = DitherMode.PSYCHOACOUSTIC_LIPSHITZ
        )
        assertEquals(input.size, processed.size)
        for (sample in processed) {
            assertTrue(sample in -1.0f..1.0f)
        }
    }

    @Test
    fun testLinearityError() {
        val stepSize = 1.0f / 32767.0f
        assertEquals(0.5f * stepSize, DitherShapingEngine.estimateLinearityError(stepSize, DitherMode.OFF), 0.000001f)
        assertEquals(0.25f * stepSize, DitherShapingEngine.estimateLinearityError(stepSize, DitherMode.RECTANGULAR_RPDF), 0.000001f)
        assertEquals(0.0f, DitherShapingEngine.estimateLinearityError(stepSize, DitherMode.TRIANGULAR_TPDF), 0.000001f)
        assertEquals(0.0f, DitherShapingEngine.estimateLinearityError(stepSize, DitherMode.PSYCHOACOUSTIC_LIPSHITZ), 0.000001f)
        assertEquals(0.0f, DitherShapingEngine.estimateLinearityError(stepSize, DitherMode.HIGH_PASS_CONTOUR), 0.000001f)
    }
}

