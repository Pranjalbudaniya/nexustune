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
    private val musicRepository = mockk<MusicRepository>()
    private val manageQueueUseCase = mockk<ManageQueueUseCase>(relaxed = true)

    private val sampleLiked = listOf(
        Track(id = "l1", title = "Liked Track", artist = "Artist 1", isLiked = true)
    )
    private val sampleHistory = listOf(
        Track(id = "h1", title = "History Track", artist = "Artist 2")
    )

    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { musicRepository.getLikedTracks() } returns flowOf(sampleLiked)
        every { musicRepository.getHistory() } returns flowOf(sampleHistory)

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
}
