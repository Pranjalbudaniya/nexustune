package com.nexus.tune.core.data

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.data.repository.MusicRepositoryImpl
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.dao.LyricsDao
import com.nexus.tune.core.database.entity.LyricsEntity
import com.nexus.tune.core.extractor.MusicSource
import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.TrackLyrics
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LyricsCacheTest {

    private val database = mockk<NexusTuneDatabase>(relaxed = true)
    private val lyricsDao = mockk<LyricsDao>(relaxed = true)
    private val musicSource = mockk<MusicSource>(relaxed = true)
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setup() {
        every { database.lyricsDao() } returns lyricsDao
        repository = MusicRepositoryImpl(musicSource, database)
    }

    @Test
    fun `getLyrics returns cached lyrics from room when present`() = runTest {
        val cached = LyricsEntity(
            trackId = "t1",
            title = "Cached Song",
            artist = "Cached Artist",
            plainLyrics = "Line 1\nLine 2",
            syncedLyricsRaw = "1000:First line\n2000:Second line",
            userOffsetMs = 0L
        )
        coEvery { lyricsDao.getLyrics("t1") } returns cached

        val result = repository.getLyrics("t1", "Cached Song", "Cached Artist", 180)
        assertTrue(result is MusicResult.Success)
        val data = (result as MusicResult.Success).data
        assertEquals("t1", data.trackId)
        assertEquals(2, data.lines.size)
        assertEquals("First line", data.lines[0].text)
        assertEquals(1000L, data.lines[0].startTimeMs)

        // Remote music source should NOT be queried
        coVerify(exactly = 0) { musicSource.getLyrics(any(), any(), any(), any()) }
    }

    @Test
    fun `getLyrics fetches from musicSource and saves to room when cache is empty`() = runTest {
        coEvery { lyricsDao.getLyrics("t2") } returns null
        val remoteLyrics = TrackLyrics(
            trackId = "t2",
            lines = listOf(LyricsLine(1500L, "Remote line")),
            isSynced = true
        )
        coEvery { musicSource.getLyrics("t2", "Song 2", "Artist 2", 200) } returns MusicResult.Success(remoteLyrics)

        val result = repository.getLyrics("t2", "Song 2", "Artist 2", 200)
        assertTrue(result is MusicResult.Success)
        val data = (result as MusicResult.Success).data
        assertEquals("t2", data.trackId)
        assertEquals(1, data.lines.size)

        coVerify {
            lyricsDao.insertOrUpdate(match {
                it.trackId == "t2" && it.syncedLyricsRaw == "1500:Remote line"
            })
        }
    }

    @Test
    fun `saveLyricsOffset and getLyricsOffset persist user calibration`() = runTest {
        coEvery { lyricsDao.getLyrics("t3") } returns null
        coEvery { lyricsDao.getOffset("t3") } returns 350L

        repository.saveLyricsOffset("t3", 350L)
        coVerify {
            lyricsDao.insertOrUpdate(match { it.trackId == "t3" && it.userOffsetMs == 350L })
        }

        val offset = repository.getLyricsOffset("t3")
        assertEquals(350L, offset)
    }

    @Test
    fun `clearLyricsCache invokes dao clearAll`() = runTest {
        repository.clearLyricsCache()
        coVerify { lyricsDao.clearAll() }
    }
}
