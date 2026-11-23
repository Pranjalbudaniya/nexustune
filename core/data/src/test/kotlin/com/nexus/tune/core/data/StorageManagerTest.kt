package com.nexus.tune.core.data

import com.nexus.tune.core.data.storage.StorageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var cacheDir: File
    private lateinit var downloadDir: File
    private lateinit var storageManager: StorageManager

    @Before
    fun setup() {
        cacheDir = tempFolder.newFolder("cache")
        downloadDir = tempFolder.newFolder("downloads")
        storageManager = StorageManager(cacheDir, downloadDir)
    }

    @Test
    fun `getStorageUsage calculates correct total bytes`() {
        val f1 = File(cacheDir, "file1.bin").apply { writeBytes(ByteArray(1024)) }
        val f2 = File(downloadDir, "track.mp3").apply { writeBytes(ByteArray(2048)) }

        val usage = storageManager.getStorageUsage()
        assertEquals(1024L, usage.cacheBytes)
        assertEquals(2048L, usage.downloadBytes)
        assertEquals(3072L, usage.totalBytes)
    }

    @Test
    fun `clearCache removes all files from cacheDir`() {
        val f1 = File(cacheDir, "temp1.bin").apply { writeBytes(ByteArray(1024)) }
        val f2 = File(cacheDir, "temp2.bin").apply { writeBytes(ByteArray(2048)) }

        val freed = storageManager.clearCache()
        assertEquals(3072L, freed)
        assertEquals(0, cacheDir.listFiles()?.size ?: 0)
    }

    @Test
    fun `pruneCache removes oldest files when over budget`() {
        // Create 3 files of 1MB each with different timestamps
        val oneMb = 1024 * 1024
        val f1 = File(cacheDir, "oldest.bin").apply {
            writeBytes(ByteArray(oneMb))
            setLastModified(1000L)
        }
        val f2 = File(cacheDir, "middle.bin").apply {
            writeBytes(ByteArray(oneMb))
            setLastModified(5000L)
        }
        val f3 = File(cacheDir, "newest.bin").apply {
            writeBytes(ByteArray(oneMb))
            setLastModified(10000L)
        }

        // Total cache = 3MB, target budget = 1MB
        val freed = storageManager.pruneCache(budgetMb = 1)
        assertTrue(freed >= 2 * oneMb)
        assertTrue(!f1.exists())
        assertTrue(!f2.exists())
        assertTrue(f3.exists())
    }
}
