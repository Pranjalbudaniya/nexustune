package com.nexus.tune.core.domain

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.queue.AutoPlayQueueManager
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.GetAutoPlayRecommendationsUseCase
import com.nexus.tune.core.model.SearchResult
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutoPlayQueueManagerTest {

    private val musicRepository = mockk<MusicRepository>()
    private val queueRepository = mockk<QueueRepository>(relaxed = true)
    private lateinit var getRecommendationsUseCase: GetAutoPlayRecommendationsUseCase

    @Before
    fun setUp() {
        getRecommendationsUseCase = GetAutoPlayRecommendationsUseCase(musicRepository)
    }

    @Test
    fun `getAutoPlayRecommendations filters out existing tracks`() = runTest {
        val currentTrack = Track(id = "curr", title = "Current Song", artist = "Daft Punk")
        val queue = listOf(
            currentTrack,
            Track(id = "q1", title = "One More Time", artist = "Daft Punk")
        )

        val searchTracks = listOf(
            Track(id = "curr", title = "Current Song", artist = "Daft Punk"),
            Track(id = "q1", title = "One More Time", artist = "Daft Punk"),
            Track(id = "rec1", title = "Harder Better Faster", artist = "Daft Punk"),
            Track(id = "rec2", title = "Around the World", artist = "Daft Punk")
        )

        coEvery { musicRepository.search("Daft Punk", null) } returns MusicResult.Success(
            SearchResult(tracks = searchTracks)
        )

        val recommendations = getRecommendationsUseCase(currentTrack, queue)
        assertEquals(2, recommendations.size)
        assertEquals(listOf("rec1", "rec2"), recommendations.map { it.id })
    }

    @Test
    fun `checkAndReplenish triggers when queue has 2 or fewer remaining tracks`() = runTest {
        val currentTrack = Track(id = "curr", title = "Current Song", artist = "Daft Punk")
        val queue = listOf(
            Track(id = "p1", title = "Played", artist = "Daft Punk"),
            currentTrack,
            Track(id = "q1", title = "Upcoming 1", artist = "Daft Punk")
        )
        // currentIndex = 1, remaining = 3 - 2 = 1 <= 2 -> triggers

        val recs = listOf(
            Track(id = "rec1", title = "Rec 1", artist = "Daft Punk")
        )
        coEvery { musicRepository.search("Daft Punk", null) } returns MusicResult.Success(
            SearchResult(tracks = recs)
        )

        val manager = AutoPlayQueueManager(getRecommendationsUseCase, queueRepository, this)
        manager.checkAndReplenish(currentTrack, queue, currentIndex = 1)
        advanceUntilIdle()

        coVerify(atLeast = 1) { queueRepository.addTracksNext(match { it.any { t -> t.id == "rec1" } }) }
    }

    @Test
    fun `checkAndReplenish does not trigger when queue has ample remaining tracks`() = runTest {
        val currentTrack = Track(id = "curr", title = "Current Song", artist = "Daft Punk")
        val queue = listOf(
            currentTrack,
            Track(id = "q1", title = "Song 1", artist = "Daft Punk"),
            Track(id = "q2", title = "Song 2", artist = "Daft Punk"),
            Track(id = "q3", title = "Song 3", artist = "Daft Punk"),
            Track(id = "q4", title = "Song 4", artist = "Daft Punk")
        )
        // remaining = 5 - 1 = 4 > 2 -> does not trigger

        val manager = AutoPlayQueueManager(getRecommendationsUseCase, queueRepository, this)
        manager.checkAndReplenish(currentTrack, queue, currentIndex = 0)
        advanceUntilIdle()

        coVerify(exactly = 0) { queueRepository.addTracksNext(any()) }
    }

    @Test
    fun `checkAndReplenish does nothing when disabled`() = runTest {
        val currentTrack = Track(id = "curr", title = "Current Song", artist = "Daft Punk")
        val queue = listOf(currentTrack)

        val manager = AutoPlayQueueManager(getRecommendationsUseCase, queueRepository, this)
        manager.setEnabled(false)
        assertFalse(manager.isAutoPlayEnabled())

        manager.checkAndReplenish(currentTrack, queue, currentIndex = 0)
        advanceUntilIdle()

        coVerify(exactly = 0) { queueRepository.addTracksNext(any()) }
    }
}
