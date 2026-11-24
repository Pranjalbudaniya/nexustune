package com.nexus.tune.core.domain.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackChapterParserTest {

    private val sampleDescription = """
        Live in Concert - Full Album Set
        Tracklist:
        00:00 Intro & Ambient Prelude
        03:45 Seven Cities (Atlantis Mix)
        08:20 Solarcoaster (Midsummer Night)
        1:14:30 4 Strings - Take Me Away
        1:22:15 Outro & Applause
    """.trimIndent()

    @Test
    fun `parseChapters extracts correct timestamps and titles`() {
        val chapters = TrackChapterParser.parseChapters(sampleDescription, 5400000L) // 1h30m total
        assertEquals(5, chapters.size)

        assertEquals(1, chapters[0].index)
        assertEquals("Intro & Ambient Prelude", chapters[0].title)
        assertEquals(0L, chapters[0].startTimeMs)
        assertEquals(225000L, chapters[0].endTimeMs) // 3m45s = 225s = 225000ms
        assertEquals("03:45", chapters[0].durationFormatted)

        assertEquals(2, chapters[1].index)
        assertEquals("Seven Cities (Atlantis Mix)", chapters[1].title)
        assertEquals(225000L, chapters[1].startTimeMs)
        assertEquals(500000L, chapters[1].endTimeMs) // 8m20s = 500s = 500000ms

        assertEquals(4, chapters[3].index)
        assertEquals("4 Strings - Take Me Away", chapters[3].title)
        // 1:14:30 = 74m30s = 4470s = 4470000ms
        assertEquals(4470000L, chapters[3].startTimeMs)
        assertEquals(4935000L, chapters[3].endTimeMs) // 1:22:15 = 4935000ms
    }

    @Test
    fun `parseChapters handles suffix timestamp format`() {
        val raw = """
            Track 1 - 00:00
            Track 2 - 04:30
            Track 3 (09:15)
        """.trimIndent()

        val chapters = TrackChapterParser.parseChapters(raw)
        assertEquals(3, chapters.size)
        assertEquals("Track 1", chapters[0].title)
        assertEquals(0L, chapters[0].startTimeMs)
        assertEquals(270000L, chapters[0].endTimeMs) // 4:30
        assertEquals("Track 2", chapters[1].title)
        assertEquals(270000L, chapters[1].startTimeMs)
        assertEquals("Track 3", chapters[2].title)
        assertEquals(555000L, chapters[2].startTimeMs)
    }

    @Test
    fun `resolveActiveChapter finds appropriate chapter based on position`() {
        val chapters = TrackChapterParser.parseChapters(sampleDescription, 5400000L)

        // At 1 minute (60_000ms) -> Chapter 1
        val ch1 = TrackChapterParser.resolveActiveChapter(chapters, 60000L)
        assertNotNull(ch1)
        assertEquals(1, ch1?.index)

        // At 5 minutes (300_000ms) -> Chapter 2 (starts at 3:45)
        val ch2 = TrackChapterParser.resolveActiveChapter(chapters, 300000L)
        assertNotNull(ch2)
        assertEquals(2, ch2?.index)

        // Beyond last chapter -> Last chapter
        val chLast = TrackChapterParser.resolveActiveChapter(chapters, 6000000L)
        assertNotNull(chLast)
        assertEquals(5, chLast?.index)
    }

    @Test
    fun `calculateChapterProgress evaluates relative progress and countdown`() {
        val chapters = TrackChapterParser.parseChapters(sampleDescription, 5400000L)
        val ch2 = chapters[1] // 03:45 (225_000ms) to 08:20 (500_000ms) -> total 275_000ms

        // Position at 06:02.5 (362_500ms) -> exactly 50% through chapter 2
        val progress = TrackChapterParser.calculateChapterProgress(ch2, 362500L, chapters[2])
        assertEquals(0.5f, progress.progressFraction, 0.01f)
        assertEquals(137500L, progress.elapsedInChapterMs)
        assertEquals(137500L, progress.remainingInChapterMs)
        assertEquals("02:17", progress.elapsedFormatted)
        assertEquals("02:17", progress.remainingFormatted)
        assertEquals("Solarcoaster (Midsummer Night)", progress.nextChapter?.title)
    }

    @Test
    fun `getSeekTarget navigates forwards and backwards correctly`() {
        val chapters = TrackChapterParser.parseChapters(sampleDescription, 5400000L)

        // Forward from Chapter 1 (at 10_000ms) -> jumps to Chapter 2 start (225_000ms)
        val nextTarget = TrackChapterParser.getSeekTarget(chapters, 10000L, forward = true)
        assertEquals(225000L, nextTarget)

        // Backward from Chapter 2 early on (226_000ms, only 1s in) -> jumps to Chapter 1 start (0L)
        val prevTargetEarly = TrackChapterParser.getSeekTarget(chapters, 226000L, forward = false)
        assertEquals(0L, prevTargetEarly)

        // Backward from Chapter 2 later (300_000ms, >3s in) -> restarts Chapter 2 (225_000ms)
        val prevTargetLate = TrackChapterParser.getSeekTarget(chapters, 300000L, forward = false)
        assertEquals(225000L, prevTargetLate)
    }

    @Test
    fun `parseTimestampToMs handles mm_ss and hh_mm_ss`() {
        assertEquals(45000L, TrackChapterParser.parseTimestampToMs("00:45"))
        assertEquals(225000L, TrackChapterParser.parseTimestampToMs("03:45"))
        assertEquals(3665000L, TrackChapterParser.parseTimestampToMs("1:01:05"))
        assertEquals(-1L, TrackChapterParser.parseTimestampToMs("invalid"))
    }
}

