package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ManageQueueUseCaseTest {

    private val queueRepository = mockk<QueueRepository>(relaxed = true)
    private val useCase = ManageQueueUseCase(queueRepository)

    @Test
    fun `playTrackNow prepends and sets queue if track is new`() = runTest {
        val trackA = Track(id = "a", title = "Track A", artist = "Artist")
        val trackB = Track(id = "b", title = "Track B", artist = "Artist")
        val existingQueue = listOf(trackB)

        useCase.playTrackNow(trackA, existingQueue)

        coVerify { queueRepository.setQueue(listOf(trackA, trackB), 0) }
    }

    @Test
    fun `playTrackNow jumps to existing index if track already in queue`() = runTest {
        val trackA = Track(id = "a", title = "Track A", artist = "Artist")
        val trackB = Track(id = "b", title = "Track B", artist = "Artist")
        val existingQueue = listOf(trackA, trackB)

        useCase.playTrackNow(trackB, existingQueue)

        coVerify { queueRepository.setQueue(existingQueue, 1) }
    }

    @Test
    fun `playPlaylist initializes queue at specified index`() = runTest {
        val tracks = listOf(
            Track(id = "1", title = "One", artist = "Artist"),
            Track(id = "2", title = "Two", artist = "Artist"),
            Track(id = "3", title = "Three", artist = "Artist")
        )

        useCase.playPlaylist(tracks, 2)

        coVerify { queueRepository.setQueue(tracks, 2) }
    }

    @Test
    fun `reorder calls queueRepository moveTrack`() = runTest {
        useCase.reorder(0, 3)
        coVerify { queueRepository.moveTrack(0, 3) }
    }

    @Test
    fun `remove calls queueRepository removeTrack`() = runTest {
        useCase.remove(1)
        coVerify { queueRepository.removeTrack(1) }
    }

    @Test
    fun `addTracksNext delegates to queueRepository when tracks non empty`() = runTest {
        val tracks = listOf(Track(id = "x", title = "X", artist = "A"))
        useCase.addTracksNext(tracks)
        coVerify { queueRepository.addTracksNext(tracks) }
    }

    @Test
    fun `removeTracks delegates to queueRepository when set non empty`() = runTest {
        val ids = setOf("x", "y")
        useCase.removeTracks(ids)
        coVerify { queueRepository.removeTracks(ids) }
    }

    @Test
    fun `clearUpcoming delegates to queueRepository`() = runTest {
        useCase.clearUpcoming()
        coVerify { queueRepository.clearUpcoming() }
    }

    @Test
    fun `shuffleQueue shuffles and delegates to queueRepository`() = runTest {
        val tracks = listOf(
            Track(id = "1", title = "One", artist = "Artist A"),
            Track(id = "2", title = "Two", artist = "Artist B"),
            Track(id = "3", title = "Three", artist = "Artist A"),
            Track(id = "4", title = "Four", artist = "Artist B")
        )
        every { queueRepository.getQueue() } returns flowOf(tracks)
        every { queueRepository.getQueueIndex() } returns flowOf(1)

        useCase.shuffleQueue(smartDiversity = true)

        coVerify { queueRepository.setQueue(match { it.size == 4 && it.map { t -> t.id }.toSet() == setOf("1", "2", "3", "4") }, any()) }
    }
}

