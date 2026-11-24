package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DeClickerConfig
import com.nexus.tune.core.model.DeClickerMetrics
import com.nexus.tune.core.model.DeClickerMode
import kotlin.math.*

object DeClickerEngine {

    /**
     * Performs standard cubic Hermite spline interpolation between p1 (at t=0) and p2 (at t=1)
     * using surrounding boundary samples p0 and p3 to establish continuous tangents.
     */
    fun interpolateHermite(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val clampedT = t.coerceIn(0.0f, 1.0f)
        val t2 = clampedT * clampedT
        val t3 = t2 * clampedT

        // Tangents at endpoints
        val m1 = (p2 - p0) * 0.5f
        val m2 = (p3 - p1) * 0.5f

        // Hermite basis functions
        val h00 = 2.0f * t3 - 3.0f * t2 + 1.0f
        val h10 = t3 - 2.0f * t2 + clampedT
        val h01 = -2.0f * t3 + 3.0f * t2
        val h11 = t3 - t2

        return (h00 * p1 + h10 * m1 + h01 * p2 + h11 * m2).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Calculates the local RMS amplitude for a buffer region to establish a dynamic noise floor.
     */
    fun calculateLocalRms(buffer: FloatArray, center: Int, halfWindow: Int = 32): Float {
        val start = max(0, center - halfWindow)
        val end = min(buffer.size, center + halfWindow)
        val count = end - start
        if (count <= 0) return 1e-4f

        var sumSq = 0.0
        for (i in start until end) {
            sumSq += buffer[i].toDouble() * buffer[i].toDouble()
        }
        return max(1e-4f, sqrt(sumSq / count.toDouble()).toFloat())
    }

    /**
     * Detects and repairs impulsive micro-clicks within the audio buffer using 2nd-derivative
     * peak detection and cubic Hermite spline interpolation.
     */
    fun processBuffer(buffer: FloatArray, config: DeClickerConfig): DeClickerMetrics {
        if (!config.isEnabled || config.mode == DeClickerMode.OFF || buffer.size < 16) {
            return DeClickerMetrics(
                clicksRepaired = 0,
                peakClickReductionDb = 0.0f,
                currentNoiseFloorRms = calculateLocalRms(buffer, buffer.size / 2),
                statusSummary = "Bypassed"
            )
        }

        val sensitivity = config.sensitivityMultiplier.coerceIn(1.5f, 10.0f)
        val maxClickWidth = config.maxClickWidthSamples.coerceIn(1, 16)

        var clicksRepaired = 0
        var maxPeakDiff = 0.0f
        var noiseFloorSum = 0.0f
        var noiseFloorCount = 0

        var i = 2
        while (i < buffer.size - 3) {
            val rms = calculateLocalRms(buffer, i)
            noiseFloorSum += rms
            noiseFloorCount++

            // Discrete second derivative: d2 = x[i] - 2*x[i-1] + x[i-2]
            val d2 = abs(buffer[i] - 2.0f * buffer[i - 1] + buffer[i - 2])
            val threshold = sensitivity * rms

            if (d2 > threshold) {
                // Determine click boundary
                val clickStart = i
                var clickEnd = i
                while (clickEnd + 1 < buffer.size - 3 &&
                    clickEnd - clickStart + 1 < maxClickWidth &&
                    abs(buffer[clickEnd + 1] - 2.0f * buffer[clickEnd] + buffer[clickEnd - 1]) > (threshold * 0.7f)
                ) {
                    clickEnd++
                }

                val clickLength = clickEnd - clickStart + 1
                if (clickLength in 1..maxClickWidth) {
                    // Valid click anomaly - apply cubic Hermite repair
                    val p0 = buffer[max(0, clickStart - 2)]
                    val p1 = buffer[clickStart - 1]
                    val p2 = buffer[min(buffer.size - 1, clickEnd + 1)]
                    val p3 = buffer[min(buffer.size - 1, clickEnd + 2)]

                    for (k in 0 until clickLength) {
                        val sampleIdx = clickStart + k
                        val t = (k + 1).toFloat() / (clickLength + 1).toFloat()
                        val original = buffer[sampleIdx]
                        val repaired = interpolateHermite(p0, p1, p2, p3, t)
                        val diff = abs(original - repaired)
                        if (diff > maxPeakDiff) {
                            maxPeakDiff = diff
                        }
                        buffer[sampleIdx] = repaired
                    }

                    clicksRepaired++
                    i = clickEnd + 2
                    continue
                }
            }
            i++
        }

        val avgNoiseFloor = if (noiseFloorCount > 0) noiseFloorSum / noiseFloorCount else 1e-4f
        val peakReductionDb = if (maxPeakDiff > 1e-5f) {
            (20.0 * log10(maxPeakDiff.toDouble())).toFloat().coerceIn(-60.0f, 0.0f)
        } else {
            0.0f
        }

        val status = evaluateStatusSummary(clicksRepaired, config.mode)

        return DeClickerMetrics(
            clicksRepaired = clicksRepaired,
            peakClickReductionDb = peakReductionDb,
            currentNoiseFloorRms = avgNoiseFloor,
            statusSummary = status
        )
    }

    fun evaluateStatusSummary(clicksRepaired: Int, mode: DeClickerMode): String {
        return when {
            mode == DeClickerMode.OFF -> "Bypassed"
            clicksRepaired == 0 -> "Clean Signal • No Artifacts"
            clicksRepaired in 1..5 -> "Pristine • $clicksRepaired Micro-Ticks Repaired"
            else -> "Active Guard • $clicksRepaired Clicks Suppressed"
        }
    }
}

