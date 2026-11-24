package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.KaraokeConfig
import com.nexus.tune.core.model.KaraokeLyricProgress
import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.VocalCutLevel
import kotlin.math.pow

object KaraokeEngine {

    const val MIN_SEMITONES: Int = -6
    const val MAX_SEMITONES: Int = 6

    fun calculateLinearGain(attenuationDb: Float): Float {
        return (10.0.pow(attenuationDb / 20.0)).toFloat()
    }

    /**
     * Calculates EQ band offsets in millibels (mB) for the 5-band equalizer:
     * Band 0 (60 Hz): Bass foundation / Kick drum (untouched to preserve rhythm section)
     * Band 1 (230 Hz): Vocal chest resonance & low body
     * Band 2 (910 Hz): Core vocal formant & vowel intelligibility (primary notch cut)
     * Band 3 (3600 Hz): Consonant articulation & vocal presence
     * Band 4 (14000 Hz): Air brilliance (slightly boosted to add sparkle to backing instruments)
     */
    fun calculateVocalCutEqOffsets(cutLevel: VocalCutLevel): List<Int> {
        if (cutLevel == VocalCutLevel.OFF) {
            return listOf(0, 0, 0, 0, 0)
        }
        val db = cutLevel.attenuationDb
        val band0 = 0
        val band1 = (db * 20f).toInt().coerceIn(-1500, 1500)
        val band2 = (db * 80f).toInt().coerceIn(-1500, 1500)
        val band3 = (db * 70f).toInt().coerceIn(-1500, 1500)
        val band4 = 150 // +1.5 dB sparkle boost for cymbals/synths

        return listOf(band0, band1, band2, band3, band4)
    }

    fun calculatePitchTranspositionRatio(semitones: Int): Float {
        val clamped = clampSemitones(semitones)
        return (2.0.pow(clamped.toDouble() / 12.0)).toFloat()
    }

    fun clampSemitones(semitones: Int): Int {
        return semitones.coerceIn(MIN_SEMITONES, MAX_SEMITONES)
    }

    fun formatPitchKey(semitones: Int): String {
        return when {
            semitones == 0 -> "Original Key (0 st)"
            semitones > 0 -> "+$semitones st (Higher)"
            else -> "$semitones st (Lower)"
        }
    }

    fun estimateVocalSuppressionDb(config: KaraokeConfig): Float {
        return if (config.isActive) config.cutLevel.attenuationDb else 0.0f
    }

    fun calculateLyricProgress(playbackPositionMs: Long, lines: List<LyricsLine>): KaraokeLyricProgress {
        if (lines.isEmpty()) {
            return KaraokeLyricProgress()
        }

        if (playbackPositionMs < lines.first().startTimeMs) {
            val first = lines.first()
            return KaraokeLyricProgress(
                currentLineIndex = -1,
                currentLine = null,
                nextLine = first,
                lineProgressFraction = 0f,
                timeRemainingInLineMs = (first.startTimeMs - playbackPositionMs).coerceAtLeast(0L),
                isVocalActive = false
            )
        }

        var activeIndex = -1
        for (i in lines.indices) {
            if (lines[i].startTimeMs <= playbackPositionMs) {
                activeIndex = i
            } else {
                break
            }
        }

        if (activeIndex == -1) {
            return KaraokeLyricProgress()
        }

        val current = lines[activeIndex]
        val next = lines.getOrNull(activeIndex + 1)
        val (fraction, remainingMs) = if (next != null) {
            val lineDuration = (next.startTimeMs - current.startTimeMs).coerceAtLeast(1L)
            val elapsed = (playbackPositionMs - current.startTimeMs).coerceAtLeast(0L)
            val frac = (elapsed.toFloat() / lineDuration.toFloat()).coerceIn(0f, 1f)
            val rem = (next.startTimeMs - playbackPositionMs).coerceAtLeast(0L)
            frac to rem
        } else {
            1f to 0L
        }

        val isVocal = current.text.isNotBlank() && current.text.trim() != "♪" && current.text.trim() != "..."

        return KaraokeLyricProgress(
            currentLineIndex = activeIndex,
            currentLine = current,
            nextLine = next,
            lineProgressFraction = fraction,
            timeRemainingInLineMs = remainingMs,
            isVocalActive = isVocal
        )
    }
}
