package com.nexus.tune.core.data

import com.nexus.tune.core.data.repository.MusicRepositoryImpl
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.dao.TrackBookmarkDao
import com.nexus.tune.core.database.entity.TrackBookmarkEntity
import com.nexus.tune.core.extractor.MusicSource
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackBookmarkRepositoryTest {

    private val database = mockk<NexusTuneDatabase>(relaxed = true)
    private val bookmarkDao = mockk<TrackBookmarkDao>(relaxed = true)
    private val musicSource = mockk<MusicSource>(relaxed = true)
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setup() {
        every { database.trackBookmarkDao() } returns bookmarkDao
        repository = MusicRepositoryImpl(musicSource, database)
    }

    @Test
    fun `addBookmark inserts into dao and returns TrackBookmark`() = runTest {
        coEvery { bookmarkDao.insert(any()) } returns 42L

        val bookmark = repository.addBookmark("trk_101", "Epic Drop", 75000L)

        assertEquals(42L, bookmark.id)
        assertEquals("trk_101", bookmark.trackId)
        assertEquals("Epic Drop", bookmark.title)
        assertEquals(75000L, bookmark.positionMs)
        assertEquals("01:15", bookmark.formattedPosition)

        coVerify {
            bookmarkDao.insert(match {
                it.trackId == "trk_101" && it.title == "Epic Drop" && it.positionMs == 75000L
            })
        }
    }

    @Test
    fun `getBookmarksForTrack streams mapped bookmarks from dao`() = runTest {
        val entities = listOf(
            TrackBookmarkEntity(id = 1L, trackId = "trk_101", title = "Intro Solo", positionMs = 30000L),
            TrackBookmarkEntity(id = 2L, trackId = "trk_101", title = "Outro", positionMs = 210000L)
        )
        every { bookmarkDao.getBookmarksForTrack("trk_101") } returns flowOf(entities)

        val bookmarks = repository.getBookmarksForTrack("trk_101").first()

        assertEquals(2, bookmarks.size)
        assertEquals("Intro Solo", bookmarks[0].title)
        assertEquals("00:30", bookmarks[0].formattedPosition)
        assertEquals("Outro", bookmarks[1].title)
        assertEquals("03:30", bookmarks[1].formattedPosition)
    }

    @Test
    fun `deleteBookmark invokes dao delete`() = runTest {
        repository.deleteBookmark(42L)
        coVerify { bookmarkDao.delete(42L) }
    }
}

