package com.nexus.tune.core.data

import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessDeathQueueRestorationTest {

    private val queueRepository = mockk<QueueRepository>(relaxed = true)
    private val manageQueueUseCase = ManageQueueUseCase(queueRepository)

    @Test
    fun `queue and playback index survive and are restored after process death`() = runTest {
        val savedQueue = listOf(
            Track(id = "track_1", title = "Track 1", artist = "Artist A"),
            Track(id = "track_2", title = "Track 2", artist = "Artist B"),
            Track(id = "track_3", title = "Track 3", artist = "Artist C")
        )
        val savedIndex = 1

        every { queueRepository.getQueue() } returns flowOf(savedQueue)
        every { queueRepository.getQueueIndex() } returns flowOf(savedIndex)

        // Emulate restoring after process resurrection
        val restoredTracks = mutableListOf<Track>()
        manageQueueUseCase.getQueue().collect { restoredTracks.addAll(it) }

        var restoredIndex = -1
        manageQueueUseCase.getQueueIndex().collect { restoredIndex = it }

        assertEquals(3, restoredTracks.size)
        assertEquals("track_2", restoredTracks[restoredIndex].id)
        assertEquals("Track 2", restoredTracks[restoredIndex].title)
    }
}

