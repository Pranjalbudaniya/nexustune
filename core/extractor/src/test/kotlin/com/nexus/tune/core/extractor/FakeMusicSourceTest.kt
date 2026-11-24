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
    fun `resolveStreamUrl respects quality tiers`() = runTest {
        val lowResult = musicSource.resolveStreamUrl("fake_1", "LOW")
        assertTrue(lowResult is MusicResult.Success)
        assertEquals(64, (lowResult as MusicResult.Success).data.bitrateKbps)

        val medResult = musicSource.resolveStreamUrl("fake_1", "MEDIUM")
        assertTrue(medResult is MusicResult.Success)
        assertEquals(128, (medResult as MusicResult.Success).data.bitrateKbps)

        val highResult = musicSource.resolveStreamUrl("fake_1", "HIGH")
        assertTrue(highResult is MusicResult.Success)
        assertEquals(160, (highResult as MusicResult.Success).data.bitrateKbps)
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

