package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DjFxConfig
import com.nexus.tune.core.model.DjPreset
import java.util.Locale

object DjTransformEngine {

    const val MIN_SPEED = 0.50f
    const val MAX_SPEED = 2.00f
    const val MIN_SEMITONES = -6
    const val MAX_SEMITONES = 6
    const val MIN_SOUNDSTAGE_WIDTH = 0.0f
    const val MAX_SOUNDSTAGE_WIDTH = 2.0f

    fun getPresetConfig(preset: DjPreset): DjFxConfig {
        return when (preset) {
            DjPreset.NORMAL -> DjFxConfig(
                preset = DjPreset.NORMAL,
                speed = 1.0f,
                semitones = 0,
                soundstageWidth = 1.0f,
                loFiFilterEnabled = false,
                bassBoostDb = 0
            )
            DjPreset.NIGHTCORE -> DjFxConfig(
                preset = DjPreset.NIGHTCORE,
                speed = 1.25f,
                semitones = 2,
                soundstageWidth = 1.15f,
                loFiFilterEnabled = false,
                bassBoostDb = 2
            )
            DjPreset.SLOWED_REVERB -> DjFxConfig(
                preset = DjPreset.SLOWED_REVERB,
                speed = 0.85f,
                semitones = -2,
                soundstageWidth = 1.40f,
                loFiFilterEnabled = false,
                bassBoostDb = 4
            )
            DjPreset.VINTAGE_VINYL -> DjFxConfig(
                preset = DjPreset.VINTAGE_VINYL,
                speed = 0.95f,
                semitones = -1,
                soundstageWidth = 0.85f,
                loFiFilterEnabled = true,
                bassBoostDb = 3
            )
            DjPreset.VAPORWAVE -> DjFxConfig(
                preset = DjPreset.VAPORWAVE,
                speed = 0.80f,
                semitones = -3,
                soundstageWidth = 1.50f,
                loFiFilterEnabled = true,
                bassBoostDb = 5
            )
            DjPreset.DEEP_BASS -> DjFxConfig(
                preset = DjPreset.DEEP_BASS,
                speed = 1.0f,
                semitones = 0,
                soundstageWidth = 1.10f,
                loFiFilterEnabled = false,
                bassBoostDb = 8
            )
            DjPreset.CUSTOM -> DjFxConfig(
                preset = DjPreset.CUSTOM
            )
        }
    }

    fun clampSpeed(speed: Float): Float = speed.coerceIn(MIN_SPEED, MAX_SPEED)

    fun clampSemitones(semitones: Int): Int = semitones.coerceIn(MIN_SEMITONES, MAX_SEMITONES)

    fun clampSoundstageWidth(width: Float): Float = width.coerceIn(MIN_SOUNDSTAGE_WIDTH, MAX_SOUNDSTAGE_WIDTH)

    fun calculateEffectiveDuration(originalDurationMs: Long, speed: Float): Long {
        if (originalDurationMs <= 0L) return 0L
        val clamped = clampSpeed(speed)
        return (originalDurationMs / clamped).toLong()
    }

    fun formatSpeed(speed: Float): String {
        return String.format(Locale.US, "%.2fx", clampSpeed(speed))
    }

    fun formatPitch(semitones: Int): String {
        val clamped = clampSemitones(semitones)
        return when {
            clamped > 0 -> "+$clamped st"
            clamped < 0 -> "$clamped st"
            else -> "0 st (Standard)"
        }
    }

    fun formatSoundstage(width: Float): String {
        val pct = (clampSoundstageWidth(width) * 100).toInt()
        return when {
            pct == 0 -> "Mono (0%)"
            pct < 100 -> "Narrow ($pct%)"
            pct == 100 -> "Natural Stereo (100%)"
            pct <= 150 -> "Wide ($pct%)"
            else -> "Super-Wide ($pct%)"
        }
    }

    fun calculateStereoCoefficients(width: Float): Pair<Float, Float> {
        val clamped = clampSoundstageWidth(width)
        val midGain = 1.0f
        val sideGain = clamped
        return Pair(midGain, sideGain)
    }

    fun matchClosestPreset(speed: Float, semitones: Int): DjPreset {
        val targetSpeed = clampSpeed(speed)
        val targetSemi = clampSemitones(semitones)
        val presets = listOf(
            DjPreset.NORMAL,
            DjPreset.NIGHTCORE,
            DjPreset.SLOWED_REVERB,
            DjPreset.VINTAGE_VINYL,
            DjPreset.VAPORWAVE,
            DjPreset.DEEP_BASS
        )
        return presets.minByOrNull { preset ->
            val cfg = getPresetConfig(preset)
            val speedDiff = Math.abs(cfg.speed - targetSpeed) * 10f
            val semiDiff = Math.abs(cfg.semitones - targetSemi).toFloat()
            speedDiff + semiDiff
        } ?: DjPreset.CUSTOM
    }
}
