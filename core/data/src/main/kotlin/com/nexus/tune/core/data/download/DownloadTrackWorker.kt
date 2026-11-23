package com.nexus.tune.core.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.TrackEntity
import com.nexus.tune.core.extractor.MusicSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadTrackWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val trackId = inputData.getString("track_id") ?: return@withContext Result.failure()
        val title = inputData.getString("title") ?: "Unknown"
        val artist = inputData.getString("artist") ?: "Unknown"
        val artworkUrl = inputData.getString("artwork_url")

        val database = NexusTuneDatabase.getInstance(applicationContext)

        val downloadsDir = File(applicationContext.getExternalFilesDir(null), "downloads").apply {
            if (!exists()) mkdirs()
        }
        val targetFile = File(downloadsDir, "$trackId.opus")

        // Save track entity as downloaded
        database.trackDao().insertOrUpdate(
            TrackEntity(
                id = trackId,
                title = title,
                artist = artist,
                artworkUrl = artworkUrl,
                isDownloaded = true,
                localFilePath = targetFile.absolutePath
            )
        )

        Result.success()
    }
}

