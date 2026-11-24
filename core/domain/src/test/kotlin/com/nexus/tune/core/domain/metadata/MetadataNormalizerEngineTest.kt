package com.nexus.tune.core.domain.metadata

import org.junit.Assert.*
import org.junit.Test

class MetadataNormalizerEngineTest {

    @Test
    fun `toTitleCase handles capitalization and minor prepositions correctly`() {
        val result = MetadataNormalizerEngine.toTitleCase("the song of ice and fire")
        assertEquals("The Song of Ice and Fire", result)

        val single = MetadataNormalizerEngine.toTitleCase("hello")
        assertEquals("Hello", single)
    }

    @Test
    fun `cleanMetadata strips bracketed official video and audio tags`() {
        val raw = "Midnight City (Official Music Video)"
        val result = MetadataNormalizerEngine.cleanMetadata(raw, "M83")

        assertEquals("Midnight City", result.cleanedTitle)
        assertEquals("M83", result.cleanedArtist)
        assertTrue(result.isModified)
        assertTrue(result.removedPatterns.any { it.contains("Official Music Video", ignoreCase = true) })
    }

    @Test
    fun `cleanMetadata extracts featured artist from title`() {
        val raw = "Starboy (feat. Daft Punk)"
        val result = MetadataNormalizerEngine.cleanMetadata(raw, "The Weeknd")

        assertEquals("Starboy", result.cleanedTitle)
        assertEquals("The Weeknd", result.cleanedArtist)
        assertEquals(listOf("Daft Punk"), result.featuredArtists)
        assertTrue(result.isModified)
    }

    @Test
    fun `cleanMetadata splits hyphenated title when artist is unknown`() {
        val raw = "Queen - Bohemian Rhapsody [Official Audio]"
        val result = MetadataNormalizerEngine.cleanMetadata(raw, "Unknown Artist")

        assertEquals("Bohemian Rhapsody", result.cleanedTitle)
        assertEquals("Queen", result.cleanedArtist)
        assertTrue(result.isModified)
    }

    @Test
    fun `cleanMetadata cleans album name if album has junk tags`() {
        val rawTitle = "Track One"
        val rawAlbum = "Greatest Hits (Live) [HQ]"
        val result = MetadataNormalizerEngine.cleanMetadata(rawTitle, "Artist", rawAlbum)

        assertEquals("Greatest Hits", result.cleanedAlbum)
        assertTrue(result.isModified)
    }

    @Test
    fun `formatBitrateBadge formats lossless and high quality correctly`() {
        assertEquals("FLAC • Lossless (1411 kbps)", MetadataNormalizerEngine.formatBitrateBadge(1411, "FLAC"))
        assertEquals("AAC • High Quality (256 kbps)", MetadataNormalizerEngine.formatBitrateBadge(256, "AAC"))
        assertEquals("OPUS • Standard (160 kbps)", MetadataNormalizerEngine.formatBitrateBadge(160, "Opus"))
    }
}

