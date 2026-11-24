package com.nexus.tune.core.domain.lyrics

import org.junit.Assert.*
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parse empty or blank string returns empty lyrics`() {
        val result = LrcParser.parse("")
        assertTrue(result.lines.isEmpty())
        assertFalse(result.isSynced)
    }

    @Test
    fun `parse standard lrc format extracts timestamps and text`() {
        val lrc = """
            [00:01.20] First line
            [00:04.50] Second line
            [01:10.05] Third line
        """.trimIndent()

        val result = LrcParser.parse(lrc, trackId = "track_1")
        assertEquals("track_1", result.trackId)
        assertTrue(result.isSynced)
        assertEquals(3, result.lines.size)

        assertEquals(1200L, result.lines[0].startTimeMs)
        assertEquals("First line", result.lines[0].text)

        assertEquals(4500L, result.lines[1].startTimeMs)
        assertEquals("Second line", result.lines[1].text)

        assertEquals(70050L, result.lines[2].startTimeMs)
        assertEquals("Third line", result.lines[2].text)
    }

    @Test
    fun `parseDetailed extracts metadata tags and applies offset`() {
        val lrc = """
            [ti:My Song]
            [ar:Awesome Artist]
            [al:Great Album]
            [offset:500]
            [00:02.00] Offset line
        """.trimIndent()

        val parsed = LrcParser.parseDetailed(lrc)
        assertEquals("My Song", parsed.title)
        assertEquals("Awesome Artist", parsed.artist)
        assertEquals("Great Album", parsed.album)
        assertEquals(500L, parsed.offsetMs)
        assertEquals(1, parsed.lines.size)
        assertEquals(2500L, parsed.lines[0].startTimeMs)
        assertEquals("Offset line", parsed.lines[0].text)
    }

    @Test
    fun `parse handles multiple timestamps on a single line`() {
        val lrc = """
            [00:10.00][00:20.00] Repeated Chorus
        """.trimIndent()

        val result = LrcParser.parse(lrc)
        assertEquals(2, result.lines.size)
        assertEquals(10000L, result.lines[0].startTimeMs)
        assertEquals("Repeated Chorus", result.lines[0].text)
        assertEquals(20000L, result.lines[1].startTimeMs)
        assertEquals("Repeated Chorus", result.lines[1].text)
    }
}

