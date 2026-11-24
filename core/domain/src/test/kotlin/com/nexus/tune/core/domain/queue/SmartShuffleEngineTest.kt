package com.nexus.tune.core.domain.queue

import com.nexus.tune.core.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SmartShuffleEngineTest {

    private fun createTrack(id: String, artist: String): Track {
        return Track(
            id = id,
            title = "Track $id",
            artist = artist,
            durationSeconds = 200
        )
    }

    @Test
    fun `shuffle with empty or tiny list returns identity`() {
        val empty = emptyList<Track>()
        assertEquals(empty, SmartShuffleEngine.shuffle(empty))

        val single = listOf(createTrack("1", "Artist A"))
        assertEquals(single, SmartShuffleEngine.shuffle(single))

        val two = listOf(createTrack("1", "Artist A"), createTrack("2", "Artist B"))
        assertEquals(two, SmartShuffleEngine.shuffle(two))
    }

    @Test
    fun `shuffle preserves all tracks without loss or duplication`() {
        val tracks = listOf(
            createTrack("1", "Artist A"),
            createTrack("2", "Artist A"),
            createTrack("3", "Artist B"),
            createTrack("4", "Artist B"),
            createTrack("5", "Artist C")
        )

        val result = SmartShuffleEngine.shuffle(tracks)
        assertEquals(tracks.size, result.size)
        assertEquals(tracks.map { it.id }.toSet(), result.map { it.id }.toSet())
    }

    @Test
    fun `shuffle spaces out artists so no two identical artists are consecutive when possible`() {
        val tracks = listOf(
            createTrack("1", "Drake"),
            createTrack("2", "Drake"),
            createTrack("3", "Drake"),
            createTrack("4", "Adele"),
            createTrack("5", "Adele"),
            createTrack("6", "Adele")
        )

        val result = SmartShuffleEngine.shuffle(tracks, random = Random(42))
        assertEquals(6, result.size)

        // Verify no two adjacent tracks have identical artists
        for (i in 0 until result.size - 1) {
            val currentArtist = result[i].artist.lowercase()
            val nextArtist = result[i + 1].artist.lowercase()
            assertNotEquals("Adjacent tracks at $i and ${i + 1} have the same artist $currentArtist", currentArtist, nextArtist)
        }
    }

    @Test
    fun `shuffle pins current track to index 0`() {
        val current = createTrack("active", "Current Artist")
        val tracks = listOf(
            createTrack("1", "Artist A"),
            current,
            createTrack("2", "Artist B"),
            createTrack("3", "Artist A")
        )

        val result = SmartShuffleEngine.shuffle(tracks, currentTrack = current, random = Random(123))
        assertEquals("active", result.first().id)
        assertEquals(4, result.size)
    }

    @Test
    fun `shuffle with smartDiversity disabled still shuffles tracks`() {
        val tracks = (1..20).map { createTrack(it.toString(), "Artist ${it % 4}") }
        val result = SmartShuffleEngine.shuffle(tracks, smartDiversity = false, random = Random(999))
        assertEquals(20, result.size)
        assertEquals(tracks.map { it.id }.toSet(), result.map { it.id }.toSet())
    }
}

