package com.nexus.tune.core.data

import com.nexus.tune.core.data.importer.NewPipeBackupImporter
import com.nexus.tune.core.database.dao.PlaylistDao
import com.nexus.tune.core.database.dao.TrackDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NewPipeBackupImporterTest {

    private val playlistDao = mockk<PlaylistDao>(relaxed = true)
    private val trackDao = mockk<TrackDao>(relaxed = true)
    private lateinit var importer: NewPipeBackupImporter

    @Before
    fun setUp() {
        importer = NewPipeBackupImporter(playlistDao, trackDao)
    }

    @Test
    fun `importFromJson parses NewPipe format and saves playlists and tracks`() = runTest {
        val sampleJson = """
            {
                "format_version": 1,
                "playlists": [
                    {
                        "name": "Rock Classics",
                        "stream_list": [
                            {
                                "url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                                "title": "Never Gonna Give You Up",
                                "uploader": "Rick Astley",
                                "duration": 213
                            },
                            {
                                "url": "https://www.youtube.com/watch?v=fJ9rUzIMcZQ",
                                "title": "Bohemian Rhapsody",
                                "uploader": "Queen",
                                "duration": 354
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val result = importer.importFromJson(sampleJson)

        assertEquals(1, result.playlistsImported)
        assertEquals(2, result.tracksImported)

        coVerify(exactly = 1) { playlistDao.insert(match { it.title == "Rock Classics" && it.trackCount == 2 }) }
        coVerify(exactly = 2) { trackDao.insertOrUpdate(any()) }
    }

    @Test
    fun `importFromJson handles invalid json gracefully`() = runTest {
        val invalidJson = "{ invalid json }"
        val result = importer.importFromJson(invalidJson)

        assertEquals(0, result.playlistsImported)
        assertEquals(0, result.tracksImported)
    }
}

