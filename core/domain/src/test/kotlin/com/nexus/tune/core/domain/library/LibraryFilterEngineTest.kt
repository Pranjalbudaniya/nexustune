package com.nexus.tune.core.domain.library

import com.nexus.tune.core.model.LibrarySortOrder
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterEngineTest {

    private val trackA = Track(id = "1", title = "Alpha Song", artist = "Zeta", album = "Rock Album", durationSeconds = 300)
    private val trackB = Track(id = "2", title = "Beta Melody", artist = "Apex", album = "Acoustic Session", durationSeconds = 120)
    private val trackC = Track(id = "3", title = "Gamma Wave", artist = "Middle", album = "Synth Sound", durationSeconds = 480)

    private val allTracks = listOf(trackA, trackB, trackC)

    @Test
    fun `filterTracks with empty query returns all tracks`() {
        val result = LibraryFilterEngine.filterTracks(allTracks, "")
        assertEquals(3, result.size)
    }

    @Test
    fun `filterTracks matches title, artist, and album case-insensitively`() {
        val byTitle = LibraryFilterEngine.filterTracks(allTracks, "beta")
        assertEquals(1, byTitle.size)
        assertEquals("2", byTitle[0].id)

        val byArtist = LibraryFilterEngine.filterTracks(allTracks, "zeta")
        assertEquals(1, byArtist.size)
        assertEquals("1", byArtist[0].id)

        val byAlbum = LibraryFilterEngine.filterTracks(allTracks, "synth")
        assertEquals(1, byAlbum.size)
        assertEquals("3", byAlbum[0].id)
    }

    @Test
    fun `filterTracks sorts by TITLE_ASC, ARTIST_ASC, and DURATION_DESC`() {
        val byTitle = LibraryFilterEngine.filterTracks(allTracks, "", LibrarySortOrder.TITLE_ASC)
        assertEquals(listOf("1", "2", "3"), byTitle.map { it.id })

        val byArtist = LibraryFilterEngine.filterTracks(allTracks, "", LibrarySortOrder.ARTIST_ASC)
        assertEquals(listOf("2", "3", "1"), byArtist.map { it.id })

        val byDuration = LibraryFilterEngine.filterTracks(allTracks, "", LibrarySortOrder.DURATION_DESC)
        assertEquals(listOf("3", "1", "2"), byDuration.map { it.id })
    }

    @Test
    fun `filterPlaylists filters by title and author`() {
        val p1 = Playlist(id = "p1", title = "Cyberpunk Mix", author = "Neo")
        val p2 = Playlist(id = "p2", title = "Chill Sunset", author = "DJ Breeze")

        val res1 = LibraryFilterEngine.filterPlaylists(listOf(p1, p2), "cyber")
        assertEquals(1, res1.size)
        assertEquals("p1", res1[0].id)

        val res2 = LibraryFilterEngine.filterPlaylists(listOf(p1, p2), "breeze")
        assertEquals(1, res2.size)
        assertEquals("p2", res2[0].id)
    }
}

