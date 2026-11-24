package com.nexus.tune.core.data

import com.nexus.tune.core.data.backup.BackupManager
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.dao.PlaylistDao
import com.nexus.tune.core.database.dao.TrackDao
import com.nexus.tune.core.database.entity.PlaylistEntity
import com.nexus.tune.core.database.entity.TrackEntity
import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.model.ThemeConfig
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    private val database = mockk<NexusTuneDatabase>()
    private val trackDao = mockk<TrackDao>(relaxed = true)
    private val playlistDao = mockk<PlaylistDao>(relaxed = true)
    private val settingsDataStore = mockk<SettingsDataStore>(relaxed = true)

    private val backupManager = BackupManager(database, settingsDataStore)

    @Test
    fun `export and import round-trip restores backup data`() = runTest {
        val sampleLiked = listOf(
            TrackEntity(
                id = "track_123",
                title = "Cyber City",
                artist = "Future Wave",
                album = "Neon Grid",
                artworkUrl = "https://example.com/art.jpg",
                isLiked = true
            )
        )
        val samplePlaylists = listOf(
            PlaylistEntity(
                id = "p_1",
                title = "Favorites Mix",
                author = "User",
                artworkUrl = null,
                trackCount = 1
            )
        )

        every { database.trackDao() } returns trackDao
        every { database.playlistDao() } returns playlistDao
        every { trackDao.getLikedTracks() } returns flowOf(sampleLiked)
        every { playlistDao.getPlaylists() } returns flowOf(samplePlaylists)
        every { settingsDataStore.themeConfig } returns flowOf(ThemeConfig(isDarkMode = true, isAmoled = true))

        val exportedJson = backupManager.exportBackupJson()
        assertTrue(exportedJson.contains("Cyber City"))
        assertTrue(exportedJson.contains("Favorites Mix"))

        val importResult = backupManager.importBackupJson(exportedJson)
        assertTrue(importResult)

        coVerify {
            trackDao.insertOrUpdate(match { it.id == "track_123" && it.title == "Cyber City" })
            playlistDao.insert(match { it.id == "p_1" && it.title == "Favorites Mix" })
        }
    }

    @Test
    fun `importFromNewPipeJson creates playlists and tracks`() = runTest {
        every { database.trackDao() } returns trackDao
        every { database.playlistDao() } returns playlistDao

        val newPipeJson = """
            {
                "playlists": [
                    {
                        "name": "Synthwave Classics",
                        "streams": [
                            {
                                "url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                                "title": "Never Gonna Give You Up",
                                "uploader": "Rick Astley"
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val result = backupManager.importFromNewPipeJson(newPipeJson)
        assertTrue(result.success)
        assertEquals(1, result.playlistsImported)
        assertEquals(1, result.tracksImported)

        coVerify {
            playlistDao.insert(match { it.title == "Synthwave Classics" })
            trackDao.insertOrUpdate(match { it.id == "dQw4w9WgXcQ" && it.artist == "Rick Astley" })
        }
    }
}
