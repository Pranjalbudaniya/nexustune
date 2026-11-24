package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.playlist.PlaylistDeduplicator
import com.nexus.tune.core.model.Track
import org.junit.Assert.*
import org.junit.Test

class PlaylistDeduplicatorTest {

    @Test
    fun `normalizeTitle removes noise tags properly`() {
        assertEquals("midnight odyssey", PlaylistDeduplicator.normalizeTitle("Midnight Odyssey (Official Audio)"))
        assertEquals("cybernetic pulse", PlaylistDeduplicator.normalizeTitle("Cybernetic Pulse [Official Music Video]"))
        assertEquals("starlight", PlaylistDeduplicator.normalizeTitle("Starlight (Lyric Video)"))
        assertEquals("echoes in the rain", PlaylistDeduplicator.normalizeTitle("Echoes in the Rain [HQ]"))
        assertEquals("hyperdrive", PlaylistDeduplicator.normalizeTitle("Hyperdrive (HD)"))
    }

    @Test
    fun `deduplicate removes exact ID duplicates`() {
        val tracks = listOf(
            Track("t1", "Midnight Odyssey", "Synthetix"),
            Track("t2", "Starlight Velocity", "Nova Drift"),
            Track("t1", "Midnight Odyssey", "Synthetix")
        )

        val report = PlaylistDeduplicator.deduplicate(tracks)
        assertEquals(1, report.duplicateCount)
        assertEquals(2, report.deduplicatedTracks.size)
        assertEquals("t1", report.deduplicatedTracks[0].id)
        assertEquals("t2", report.deduplicatedTracks[1].id)
    }

    @Test
    fun `deduplicate removes semantic duplicates with different IDs`() {
        val tracks = listOf(
            Track("t1", "Midnight Odyssey", "Synthetix"),
            Track("t2", "Midnight Odyssey (Official Video)", "Synthetix"),
            Track("t3", "Starlight Velocity", "Nova Drift"),
            Track("t4", "Midnight Odyssey [Audio]", "Synthetix")
        )

        val report = PlaylistDeduplicator.deduplicate(tracks)
        assertEquals(2, report.duplicateCount)
        assertEquals(2, report.deduplicatedTracks.size)
        assertEquals("t1", report.deduplicatedTracks[0].id)
        assertEquals("t3", report.deduplicatedTracks[1].id)
    }

    @Test
    fun `deduplicate preserves tracks by different artists with same title`() {
        val tracks = listOf(
            Track("t1", "Breathe", "Artist A"),
            Track("t2", "Breathe", "Artist B")
        )

        val report = PlaylistDeduplicator.deduplicate(tracks)
        assertEquals(0, report.duplicateCount)
        assertEquals(2, report.deduplicatedTracks.size)
    }
}
