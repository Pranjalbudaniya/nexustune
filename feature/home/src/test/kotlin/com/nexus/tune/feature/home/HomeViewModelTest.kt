package com.nexus.tune.feature.home

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.usecase.GetHomeFeedUseCase
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.MoodCategory
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getHomeFeedUseCase = mockk<GetHomeFeedUseCase>(relaxed = true)
    private val manageQueueUseCase = mockk<ManageQueueUseCase>(relaxed = true)

    private val track1 = Track(id = "1", title = "Focus Study Piano", artist = "Ambient Art")
    private val track2 = Track(id = "2", title = "Heavy Metal Workout", artist = "Rockers")
    private val playlist = Playlist(id = "p1", title = "Mixed Jams", tracks = listOf(track1, track2))

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getHomeFeedUseCase() } returns MusicResult.Success(listOf(playlist))
        viewModel = HomeViewModel(getHomeFeedUseCase, manageQueueUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load displays full playlist feed`() {
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertEquals(1, success.playlists.size)
        assertEquals(MoodCategory.ALL, success.selectedMood)
    }

    @Test
    fun `selectMood filters playlists according to mood`() {
        viewModel.selectMood(MoodCategory.FOCUS)
        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val success = state as HomeUiState.Success
        assertEquals(MoodCategory.FOCUS, success.selectedMood)
        assertEquals(1, success.playlists.size)
        assertEquals(1, success.playlists[0].tracks.size)
        assertEquals("1", success.playlists[0].tracks[0].id)
    }

    @Test
    fun `startMoodRadio primes queue with mood radio tracks`() = runTest {
        viewModel.startMoodRadio(MoodCategory.FOCUS)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            manageQueueUseCase.playTrackNow(match { it.id == "1" }, any())
        }
    }

    @Test
    fun `playTrack delegates to manageQueueUseCase`() = runTest {
        viewModel.playTrack(track1, listOf(track1, track2))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            manageQueueUseCase.playTrackNow(track1, listOf(track1, track2))
        }
    }
}

