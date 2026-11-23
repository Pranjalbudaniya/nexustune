package com.nexus.tune.core.data

import com.nexus.tune.core.data.repository.QueueRepositoryImpl
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.dao.QueueDao
import com.nexus.tune.core.database.entity.QueueEntity
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class QueueRepositoryImplTest {

    private val database = mockk<NexusTuneDatabase>(relaxed = true)
    private val queueDao = mockk<QueueDao>(relaxed = true)
    private lateinit var repository: QueueRepositoryImpl

    @Before
    fun setup() {
        every { database.queueDao() } returns queueDao
        repository = QueueRepositoryImpl(database)
    }

    @Test
    fun `addTracksNext inserts tracks right after current index`() = runTest {
        val existing = listOf(
            QueueEntity(0, "t1", "Track 1", "Artist 1", null, 200, null, null),
            QueueEntity(1, "t2", "Track 2", "Artist 2", null, 200, null, null)
        )
        coEvery { queueDao.getQueueSnapshot() } returns existing

        val nextTracks = listOf(Track(id = "next1", title = "Next 1", artist = "Artist"))
        repository.addTracksNext(nextTracks)

        coVerify {
            queueDao.clearQueue()
            queueDao.insertAll(match { list ->
                list.size == 3 && list[1].trackId == "next1"
            })
        }
    }

    @Test
    fun `removeTracks deletes target tracks and updates queue`() = runTest {
        val existing = listOf(
            QueueEntity(0, "t1", "Track 1", "Artist 1", null, 200, null, null),
            QueueEntity(1, "t2", "Track 2", "Artist 2", null, 200, null, null),
            QueueEntity(2, "t3", "Track 3", "Artist 3", null, 200, null, null)
        )
        coEvery { queueDao.getQueueSnapshot() } returns existing

        repository.removeTracks(setOf("t2"))

        coVerify {
            queueDao.clearQueue()
            queueDao.insertAll(match { list ->
                list.size == 2 && list.none { it.trackId == "t2" }
            })
        }
    }

    @Test
    fun `clearUpcoming retains only current and prior tracks`() = runTest {
        val existing = listOf(
            QueueEntity(0, "t1", "Track 1", "Artist 1", null, 200, null, null),
            QueueEntity(1, "t2", "Track 2", "Artist 2", null, 200, null, null),
            QueueEntity(2, "t3", "Track 3", "Artist 3", null, 200, null, null)
        )
        coEvery { queueDao.getQueueSnapshot() } returns existing

        repository.clearUpcoming()

        coVerify {
            queueDao.clearQueue()
            queueDao.insertAll(match { list ->
                list.size == 1 && list[0].trackId == "t1"
            })
        }
    }
}
