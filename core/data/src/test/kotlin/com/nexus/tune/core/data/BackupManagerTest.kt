package com.nexus.tune.core.data

import com.nexus.tune.core.data.backup.BackupManager
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.dao.TrackDao
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

        every { database.trackDao() } returns trackDao
        every { trackDao.getLikedTracks() } returns flowOf(sampleLiked)
        every { settingsDataStore.themeConfig } returns flowOf(ThemeConfig(isDarkMode = true, isAmoled = true))

        val exportedJson = backupManager.exportBackupJson()
        assertTrue(exportedJson.contains("Cyber City"))
        assertTrue(exportedJson.contains("Future Wave"))

        // Now test import
        val importResult = backupManager.importBackupJson(exportedJson)
        assertTrue(importResult)

        coVerify {
            trackDao.insertOrUpdate(match { it.id == "track_123" && it.title == "Cyber City" })
        }
    }
}

