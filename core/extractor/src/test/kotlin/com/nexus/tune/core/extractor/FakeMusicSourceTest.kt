package com.nexus.tune.core.extractor

import com.nexus.tune.core.common.MusicResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMusicSourceTest {
    private val musicSource = FakeMusicSource()

    @Test
    fun `search returns matching tracks`() = runTest {
        val result = musicSource.search("synth")
        assertTrue(result is MusicResult.Success)
        val data = (result as MusicResult.Success).data
        assertTrue(data.tracks.isNotEmpty())
        assertTrue(data.tracks.any { it.title.contains("Synthetix") || it.artist.contains("Synthetix") })
    }

    @Test
    fun `resolveStreamUrl produces valid stream`() = runTest {
        val result = musicSource.resolveStreamUrl("fake_1")
        assertTrue(result is MusicResult.Success)
        val stream = (result as MusicResult.Success).data
        assertNotNull(stream.url)
        assertEquals("Opus", stream.format)
        assertEquals(160, stream.bitrateKbps)
    }

    @Test
    fun `getHomeFeed returns playlists`() = runTest {
        val result = musicSource.getHomeFeed()
        assertTrue(result is MusicResult.Success)
        val playlists = (result as MusicResult.Success).data
        assertTrue(playlists.isNotEmpty())
        assertEquals("Quick Picks", playlists[0].title)
    }
}

