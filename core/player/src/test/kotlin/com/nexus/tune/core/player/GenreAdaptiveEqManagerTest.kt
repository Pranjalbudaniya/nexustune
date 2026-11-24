package com.nexus.tune.core.player

import com.nexus.tune.core.model.EqualizerPreset
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.player.effects.GenreAdaptiveEqManager
import com.nexus.tune.core.player.effects.NexusAudioEffects
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenreAdaptiveEqManagerTest {

    private val audioEffects = mockk<NexusAudioEffects>(relaxed = true)
    private lateinit var manager: GenreAdaptiveEqManager

    @Before
    fun setup() {
        manager = GenreAdaptiveEqManager(audioEffects)
    }

    @Test
    fun `detectGenrePreset identifies Electronic genre`() {
        val track = Track(id = "1", title = "Midnight Club Remix", artist = "Daft Punk")
        assertEquals(EqualizerPreset.ELECTRONIC, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset identifies Rock genre`() {
        val track = Track(id = "2", title = "Heavy Metal Thunder", artist = "Rock Band")
        assertEquals(EqualizerPreset.ROCK, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset identifies Hip Hop genre`() {
        val track = Track(id = "3", title = "Trap Beat Anthem", artist = "Rap Star")
        assertEquals(EqualizerPreset.HIP_HOP, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset identifies Classical genre`() {
        val track = Track(id = "4", title = "Symphony No. 5 in C Minor", artist = "Orchestra")
        assertEquals(EqualizerPreset.CLASSICAL, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset identifies Jazz genre`() {
        val track = Track(id = "5", title = "Blue Saxophone Blues", artist = "Jazz Quartet")
        assertEquals(EqualizerPreset.JAZZ, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset identifies Acoustic genre`() {
        val track = Track(id = "6", title = "Unplugged Acoustic Guitar", artist = "Folk Singer")
        assertEquals(EqualizerPreset.ACOUSTIC, manager.detectGenrePreset(track))
    }

    @Test
    fun `detectGenrePreset defaults to Flat when no keywords match`() {
        val track = Track(id = "7", title = "Generic Morning Tune", artist = "Unknown")
        assertEquals(EqualizerPreset.FLAT, manager.detectGenrePreset(track))
    }

    @Test
    fun `applyAdaptivePreset invokes audioEffects applyPreset`() {
        val track = Track(id = "8", title = "Dubstep Bass Drop", artist = "Bass Artist")
        val preset = manager.applyAdaptivePreset(track)
        assertEquals(EqualizerPreset.BASS_BOOST, preset)
        verify { audioEffects.applyPreset(EqualizerPreset.BASS_BOOST) }
    }
}

