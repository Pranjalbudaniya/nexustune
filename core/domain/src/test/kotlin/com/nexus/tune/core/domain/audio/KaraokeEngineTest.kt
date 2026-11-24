package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.KaraokeConfig
import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.VocalCutLevel
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class KaraokeEngineTest {

    @Test
    fun `calculateLinearGain converts dB to amplitude correctly`() {
        val gain0 = KaraokeEngine.calculateLinearGain(0f)
        assertEquals(1.0f, gain0, 0.001f)

        val gainMinus6 = KaraokeEngine.calculateLinearGain(-6f)
        assertEquals(0.501f, gainMinus6, 0.01f)

        val gainMinus20 = KaraokeEngine.calculateLinearGain(-20f)
        assertEquals(0.1f, gainMinus20, 0.01f)
    }

    @Test
    fun `calculateVocalCutEqOffsets returns zeros when OFF`() {
        val offsets = KaraokeEngine.calculateVocalCutEqOffsets(VocalCutLevel.OFF)
        assertEquals(listOf(0, 0, 0, 0, 0), offsets)
    }

    @Test
    fun `calculateVocalCutEqOffsets leaves 60Hz bass untouched and notches vocal core`() {
        val offsets = KaraokeEngine.calculateVocalCutEqOffsets(VocalCutLevel.MODERATE)
        assertEquals(5, offsets.size)
        assertEquals(0, offsets[0]) // Sub-bass untouched
        assertTrue("230Hz should be negative", offsets[1] < 0)
        assertTrue("910Hz core vocal should be deeply notched", offsets[2] < offsets[1])
        assertTrue("3600Hz presence should be notched", offsets[3] < 0)
        assertEquals("14000Hz air should be boosted", 150, offsets[4])
    }

    @Test
    fun `calculatePitchTranspositionRatio matches chromatic scale powers`() {
        val ratio0 = KaraokeEngine.calculatePitchTranspositionRatio(0)
        assertEquals(1.0f, ratio0, 0.001f)

        val ratioPlus12Clamped = KaraokeEngine.calculatePitchTranspositionRatio(12)
        // Clamped to MAX_SEMITONES = 6: 2^(6/12) = sqrt(2) ~= 1.4142
        assertEquals(1.4142f, ratioPlus12Clamped, 0.005f)

        val ratioMinus6 = KaraokeEngine.calculatePitchTranspositionRatio(-6)
        // 2^(-6/12) = 1/sqrt(2) ~= 0.7071
        assertEquals(0.7071f, ratioMinus6, 0.005f)
    }

    @Test
    fun `formatPitchKey formats positive, zero, and negative values accurately`() {
        assertEquals("Original Key (0 st)", KaraokeEngine.formatPitchKey(0))
        assertEquals("+2 st (Higher)", KaraokeEngine.formatPitchKey(2))
        assertEquals("-3 st (Lower)", KaraokeEngine.formatPitchKey(-3))
    }

    @Test
    fun `estimateVocalSuppressionDb respects config active status`() {
        val disabledConfig = KaraokeConfig(isEnabled = false, cutLevel = VocalCutLevel.AGGRESSIVE)
        assertEquals(0.0f, KaraokeEngine.estimateVocalSuppressionDb(disabledConfig), 0.001f)

        val activeConfig = KaraokeConfig(isEnabled = true, cutLevel = VocalCutLevel.AGGRESSIVE)
        assertEquals(-18.0f, KaraokeEngine.estimateVocalSuppressionDb(activeConfig), 0.001f)
    }

    @Test
    fun `calculateLyricProgress handles intro, active line, and line completion`() {
        val lines = listOf(
            LyricsLine(1000L, "First line of song"),
            LyricsLine(5000L, "Second verse begins"),
            LyricsLine(10000L, "Outro ♪")
        )

        // 1. Intro before first lyric
        val introProgress = KaraokeEngine.calculateLyricProgress(500L, lines)
        assertEquals(-1, introProgress.currentLineIndex)
        assertNull(introProgress.currentLine)
        assertEquals(lines[0], introProgress.nextLine)
        assertEquals(500L, introProgress.timeRemainingInLineMs)
        assertFalse(introProgress.isVocalActive)

        // 2. Midway through first line (at 3000ms: duration is 4000ms, elapsed 2000ms -> 50%)
        val midProgress = KaraokeEngine.calculateLyricProgress(3000L, lines)
        assertEquals(0, midProgress.currentLineIndex)
        assertEquals(lines[0], midProgress.currentLine)
        assertEquals(lines[1], midProgress.nextLine)
        assertEquals(0.5f, midProgress.lineProgressFraction, 0.01f)
        assertEquals(2000L, midProgress.timeRemainingInLineMs)
        assertTrue(midProgress.isVocalActive)

        // 3. Last line
        val lastProgress = KaraokeEngine.calculateLyricProgress(12000L, lines)
        assertEquals(2, lastProgress.currentLineIndex)
        assertEquals(lines[2], lastProgress.currentLine)
        assertNull(lastProgress.nextLine)
        assertEquals(1.0f, lastProgress.lineProgressFraction, 0.01f)
        assertEquals(0L, lastProgress.timeRemainingInLineMs)
        // "Outro ♪" contains text so isVocalActive is true
    }

    @Test
    fun `calculateLyricProgress handles empty lines gracefully`() {
        val progress = KaraokeEngine.calculateLyricProgress(1000L, emptyList())
        assertEquals(-1, progress.currentLineIndex)
        assertNull(progress.currentLine)
        assertNull(progress.nextLine)
    }
}
