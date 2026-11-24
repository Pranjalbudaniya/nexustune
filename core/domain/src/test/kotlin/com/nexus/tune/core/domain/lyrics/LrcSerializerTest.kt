package com.nexus.tune.core.domain.lyrics

import com.nexus.tune.core.model.LyricsLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcSerializerTest {

    @Test
    fun `serialize converts lyrics lines to standard lrc format`() {
        val lines = listOf(
            LyricsLine(1500L, "First line"),
            LyricsLine(65200L, "Second line")
        )

        val output = LrcSerializer.serialize(lines, title = "Song", artist = "Artist")
        assertTrue(output.contains("[ti:Song]"))
        assertTrue(output.contains("[ar:Artist]"))
        assertTrue(output.contains("[00:01.50] First line"))
        assertTrue(output.contains("[01:05.20] Second line"))
    }

    @Test
    fun `roundtrip serialization and parsing preserves lines`() {
        val original = listOf(
            LyricsLine(1200L, "Hello"),
            LyricsLine(3400L, "World")
        )

        val lrcText = LrcSerializer.serialize(original)
        val parsed = LrcParser.parse(lrcText)

        assertEquals(2, parsed.lines.size)
        assertEquals(1200L, parsed.lines[0].startTimeMs)
        assertEquals("Hello", parsed.lines[0].text)
        assertEquals(3400L, parsed.lines[1].startTimeMs)
        assertEquals("World", parsed.lines[1].text)
    }
}

