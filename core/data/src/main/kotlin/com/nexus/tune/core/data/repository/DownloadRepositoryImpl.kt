package com.nexus.tune.core.data.repository

import android.content.Context
import androidx.work.*
import com.nexus.tune.core.data.download.DownloadTrackWorker
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.toModel
import com.nexus.tune.core.domain.repository.DownloadRepository
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class DownloadRepositoryImpl(
    private val context: Context,
    private val database: NexusTuneDatabase
) : DownloadRepository {

    override fun getDownloadedTracks(): Flow<List<Track>> {
        return database.trackDao().getDownloadedTracks().map { list -> list.map { it.toModel() } }
    }

    override suspend fun downloadTrack(track: Track): Boolean {
        val workData = workDataOf(
            "track_id" to track.id,
            "title" to track.title,
            "artist" to track.artist,
            "artwork_url" to track.artworkUrl
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadTrackWorker>()
            .setConstraints(constraints)
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(downloadRequest)
        return true
    }

    override suspend fun deleteDownload(trackId: String) {
        val track = database.trackDao().getTrackById(trackId) ?: return
        track.localFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        database.trackDao().insertOrUpdate(track.copy(isDownloaded = false, localFilePath = null))
    }
}

