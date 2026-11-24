package com.nexus.tune.feature.library

import app.cash.turbine.test
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Track
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val musicRepository = mockk<MusicRepository>(relaxed = true)
    private val manageQueueUseCase = mockk<ManageQueueUseCase>(relaxed = true)

    private val sampleLiked = listOf(
        Track(id = "l1", title = "Liked Track", artist = "Artist 1", isLiked = true)
    )
    private val sampleHistory = listOf(
        Track(id = "h1", title = "History Track", artist = "Artist 2")
    )
    private val samplePlaylists = listOf(
        com.nexus.tune.core.model.Playlist(id = "p1", title = "Synthwave", artworkUrl = "file:///art.jpg")
    )

    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { musicRepository.getLikedTracks() } returns flowOf(sampleLiked)
        every { musicRepository.getHistory() } returns flowOf(sampleHistory)
        every { musicRepository.getPlaylists() } returns flowOf(samplePlaylists)

        viewModel = LibraryViewModel(musicRepository, manageQueueUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `likedTracks emits repository liked list`() = runTest {
        viewModel.likedTracks.test {
            val item = awaitItem()
            val list = if (item.isEmpty()) awaitItem() else item
            assertEquals(1, list.size)
            assertEquals("Liked Track", list.first().title)
        }
    }

    @Test
    fun `historyTracks emits repository history list`() = runTest {
        viewModel.historyTracks.test {
            val item = awaitItem()
            val list = if (item.isEmpty()) awaitItem() else item
            assertEquals(1, list.size)
            assertEquals("History Track", list.first().title)
        }
    }

    @Test
    fun `playTrack delegates to manageQueueUseCase`() = runTest {
        val track = sampleLiked.first()
        viewModel.playTrack(track, sampleLiked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { manageQueueUseCase.playTrackNow(track, sampleLiked) }
    }

    @Test
    fun `playlists emits repository list`() = runTest {
        viewModel.playlists.test {
            val item = awaitItem()
            val list = if (item.isEmpty()) awaitItem() else item
            assertEquals(1, list.size)
            assertEquals("Synthwave", list.first().title)
            assertEquals("file:///art.jpg", list.first().artworkUrl)
        }
    }

    @Test
    fun `createPlaylist delegates to repository`() = runTest {
        viewModel.createPlaylist("Favorites", "file:///fav.jpg")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.createPlaylist("Favorites", "file:///fav.jpg") }
    }

    @Test
    fun `updatePlaylistArtwork delegates to repository`() = runTest {
        viewModel.updatePlaylistArtwork("p1", "file:///new_art.png")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.updatePlaylistArtwork("p1", "file:///new_art.png") }
    }

    @Test
    fun `deletePlaylist delegates to repository`() = runTest {
        viewModel.deletePlaylist("p1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.deletePlaylist("p1") }
    }

    @Test
    fun `setSearchQuery and setSortOrder filter liked tracks reactively`() = runTest {
        viewModel.setSearchQuery("Liked")
        viewModel.setSortOrder(com.nexus.tune.core.model.LibrarySortOrder.TITLE_ASC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredLikedTracks.test {
            val list = awaitItem()
            val final = if (list.isEmpty()) awaitItem() else list
            assertEquals(1, final.size)
            assertEquals("Liked Track", final.first().title)
        }

        viewModel.setSearchQuery("NonExistent")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filteredLikedTracks.test {
            val list = awaitItem()
            assertEquals(0, list.size)
        }
    }

    @Test
    fun `playAll delegates to manageQueueUseCase`() = runTest {
        viewModel.playAll(sampleLiked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { manageQueueUseCase.playTrackNow(sampleLiked.first(), sampleLiked) }
    }
}
