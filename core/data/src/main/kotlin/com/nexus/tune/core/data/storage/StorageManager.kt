package com.nexus.tune.core.data.storage

import android.content.Context
import com.nexus.tune.core.model.StorageUsage
import java.io.File

class StorageManager(
    private val cacheDir: File,
    private val downloadDir: File
) {

    constructor(context: Context) : this(
        cacheDir = context.cacheDir,
        downloadDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "downloads")
    )

    fun getStorageUsage(): StorageUsage {
        val cacheBytes = calculateDirectorySize(cacheDir)
        val downloadBytes = calculateDirectorySize(downloadDir)
        return StorageUsage(cacheBytes = cacheBytes, downloadBytes = downloadBytes)
    }

    fun clearCache(): Long {
        var freed = 0L
        cacheDir.listFiles()?.forEach { file ->
            freed += deleteRecursivelyAndCount(file)
        }
        return freed
    }

    fun pruneCache(budgetMb: Int): Long {
        if (budgetMb <= 0) return 0L
        val budgetBytes = budgetMb * 1024L * 1024L
        var currentCacheSize = calculateDirectorySize(cacheDir)
        if (currentCacheSize <= budgetBytes) return 0L

        var freed = 0L
        val allFiles = getAllFiles(cacheDir).sortedBy { it.lastModified() }
        for (file in allFiles) {
            val length = file.length()
            if (file.delete()) {
                freed += length
                currentCacheSize -= length
                if (currentCacheSize <= budgetBytes) break
            }
        }
        return freed
    }

    private fun calculateDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        return size
    }

    private fun getAllFiles(dir: File): List<File> {
        if (!dir.exists()) return emptyList()
        return dir.walkTopDown().filter { it.isFile }.toList()
    }

    private fun deleteRecursivelyAndCount(file: File): Long {
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                size += deleteRecursivelyAndCount(it)
            }
        } else {
            size += file.length()
        }
        file.delete()
        return size
    }
}
