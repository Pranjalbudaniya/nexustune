package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.CustomEqProfile
import com.nexus.tune.core.model.EqualizerPreset
import kotlin.math.sqrt

data class CurvePoint(
    val x: Float,
    val y: Float
)

object EqCurveCalculator {

    val STANDARD_PRESET_BANDS: Map<EqualizerPreset, List<Int>> = mapOf(
        EqualizerPreset.FLAT to listOf(0, 0, 0, 0, 0),
        EqualizerPreset.BASS_BOOST to listOf(600, 400, 0, 0, 0),
        EqualizerPreset.TREBLE_BOOST to listOf(0, 0, 0, 400, 600),
        EqualizerPreset.VOCAL to listOf(0, 500, 500, 0, 0),
        EqualizerPreset.ELECTRONIC to listOf(500, 300, 0, 0, 400),
        EqualizerPreset.ROCK to listOf(400, 200, -100, 200, 400),
        EqualizerPreset.CLASSICAL to listOf(300, 200, -200, 200, 400),
        EqualizerPreset.JAZZ to listOf(200, 300, 300, 200, 300),
        EqualizerPreset.HIP_HOP to listOf(700, 500, 0, 200, 400),
        EqualizerPreset.ACOUSTIC to listOf(200, 300, 400, 400, 300)
    )

    /**
     * Maps 5 band millibel values (-1200mB .. +1200mB) into 2D coordinates across (width, height)
     * where y=0 is +1200mB (+12dB) and y=height is -1200mB (-12dB), center y = height / 2.
     */
    fun calculateBandCoordinates(
        bands: List<Int>,
        width: Float,
        height: Float,
        minGainMb: Int = -1200,
        maxGainMb: Int = 1200
    ): List<CurvePoint> {
        val safeBands = if (bands.size == 5) bands else listOf(0, 0, 0, 0, 0)
        val range = (maxGainMb - minGainMb).toFloat().coerceAtLeast(1f)
        val stepX = width / 4.0f

        return safeBands.mapIndexed { index, mb ->
            val clampedMb = mb.coerceIn(minGainMb, maxGainMb)
            // Normalized fraction: 1.0 at maxGainMb, 0.0 at minGainMb
            val fraction = (clampedMb - minGainMb) / range
            val y = height * (1.0f - fraction)
            val x = index * stepX
            CurvePoint(x, y)
        }
    }

    /**
     * Calculates the closest matching standard preset for a given set of custom band gains
     * using Euclidean distance, returning the preset and percentage match (0% .. 100%).
     */
    fun findClosestPreset(bands: List<Int>): Pair<EqualizerPreset, Int> {
        val safeBands = if (bands.size == 5) bands else listOf(0, 0, 0, 0, 0)

        var closestPreset = EqualizerPreset.FLAT
        var minDistance = Double.MAX_VALUE

        // Max possible Euclidean distance across 5 bands between -1200 and +1200: sqrt(5 * 2400^2) ≈ 5366
        val maxPossibleDist = sqrt(5.0 * 2400.0 * 2400.0)

        for ((preset, presetBands) in STANDARD_PRESET_BANDS) {
            var sumSquares = 0.0
            for (i in 0 until 5) {
                val diff = (safeBands[i] - presetBands[i]).toDouble()
                sumSquares += diff * diff
            }
            val distance = sqrt(sumSquares)
            if (distance < minDistance) {
                minDistance = distance
                closestPreset = preset
            }
        }

        val matchPercent = ((1.0 - (minDistance / maxPossibleDist)) * 100.0).toInt().coerceIn(0, 100)
        return closestPreset to matchPercent
    }

    /**
     * Formats 5 band gains as human-readable decibel strings.
     * e.g. "+3dB, +1.5dB, 0dB, +4.5dB, +6dB"
     */
    fun formatBandsSummary(bands: List<Int>): String {
        val safeBands = if (bands.size == 5) bands else listOf(0, 0, 0, 0, 0)
        return safeBands.joinToString(", ") { mb ->
            val db = mb / 100f
            val sign = if (db > 0f) "+" else ""
            if (db % 1f == 0f) {
                "${sign}${db.toInt()}dB"
            } else {
                "${sign}${String.format(java.util.Locale.US, "%.1f", db)}dB"
            }
        }
    }

    /**
     * Serializes a CustomEqProfile into a simple compact string:
     * "id:::name:::b0,b1,b2,b3,b4:::timestamp"
     */
    fun serializeProfile(profile: CustomEqProfile): String {
        val bandsStr = profile.bands.joinToString(",")
        return "${profile.id}:::${profile.name}:::$bandsStr:::${profile.createdAtTimestamp}"
    }

    fun deserializeProfile(raw: String): CustomEqProfile? {
        val parts = raw.split(":::")
        if (parts.size < 4) return null
        val id = parts[0]
        val name = parts[1]
        val bands = parts[2].split(",").mapNotNull { it.trim().toIntOrNull() }
        if (bands.size != 5) return null
        val timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
        return CustomEqProfile(id, name, bands, timestamp)
    }

    fun serializeProfilesList(profiles: List<CustomEqProfile>): String {
        return profiles.joinToString("###") { serializeProfile(it) }
    }

    fun deserializeProfilesList(raw: String): List<CustomEqProfile> {
        if (raw.isBlank()) return emptyList()
        return raw.split("###").mapNotNull { deserializeProfile(it) }
    }
}
