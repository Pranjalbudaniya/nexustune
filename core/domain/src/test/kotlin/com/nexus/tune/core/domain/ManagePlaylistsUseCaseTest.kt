package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManagePlaylistsUseCase
import com.nexus.tune.core.model.Playlist
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagePlaylistsUseCaseTest {

    private val musicRepository = mockk<MusicRepository>(relaxed = true)
    private val useCase = ManagePlaylistsUseCase(musicRepository)

    @Test
    fun `getPlaylists returns repository flow`() = runTest {
        val playlists = listOf(
            Playlist(id = "p1", title = "Workout", artworkUrl = "file:///art1.jpg")
        )
        every { musicRepository.getPlaylists() } returns flowOf(playlists)

        val result = useCase.getPlaylists()
        result.collect {
            assertEquals("Workout", it[0].title)
            assertEquals("file:///art1.jpg", it[0].artworkUrl)
        }
    }

    @Test
    fun `createPlaylist delegates to repository`() = runTest {
        coEvery { musicRepository.createPlaylist("Chill", "file:///art.jpg") } returns "p123"

        val id = useCase.createPlaylist("Chill", "file:///art.jpg")
        assertEquals("p123", id)
        coVerify { musicRepository.createPlaylist("Chill", "file:///art.jpg") }
    }

    @Test
    fun `updateArtwork delegates to repository`() = runTest {
        useCase.updateArtwork("p123", "file:///new.jpg")
        coVerify { musicRepository.updatePlaylistArtwork("p123", "file:///new.jpg") }
    }

    @Test
    fun `deletePlaylist delegates to repository`() = runTest {
        useCase.deletePlaylist("p123")
        coVerify { musicRepository.deletePlaylist("p123") }
    }
}
