package com.nexus.tune.core.data.repository

import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.QueueEntity
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class QueueRepositoryImpl(
    private val database: NexusTuneDatabase
) : QueueRepository {

    private val _currentIndex = MutableStateFlow(0)
    private val savedPositions = mutableMapOf<String, Long>()

    override fun getQueue(): Flow<List<Track>> {
        return database.queueDao().getQueue().map { entities: List<QueueEntity> ->
            entities.map { entity ->
                Track(
                    id = entity.trackId,
                    title = entity.title,
                    artist = entity.artist,
                    album = entity.album,
                    durationSeconds = entity.durationSeconds,
                    artworkUrl = entity.artworkUrl,
                    streamUrl = entity.streamUrl
                )
            }
        }
    }

    override fun getQueueIndex(): Flow<Int> = _currentIndex.asStateFlow()

    override suspend fun setQueue(tracks: List<Track>, startIndex: Int) {
        database.queueDao().clearQueue()
        val entities = tracks.mapIndexed { index, track ->
            QueueEntity(
                queueOrder = index,
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationSeconds = track.durationSeconds,
                artworkUrl = track.artworkUrl,
                streamUrl = track.streamUrl
            )
        }
        database.queueDao().insertAll(entities)
        _currentIndex.value = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
    }

    override suspend fun addTrack(track: Track) {
        val entity = QueueEntity(
            queueOrder = 9999,
            trackId = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationSeconds = track.durationSeconds,
            artworkUrl = track.artworkUrl,
            streamUrl = track.streamUrl
        )
        database.queueDao().insertAll(listOf(entity))
    }

    override suspend fun addNext(track: Track) {
        addTrack(track)
    }

    override suspend fun moveTrack(fromIndex: Int, toIndex: Int) {
        // Reordering
    }

    override suspend fun removeTrack(index: Int) {
        // Removal
    }

    override suspend fun clearQueue() {
        database.queueDao().clearQueue()
        _currentIndex.value = 0
    }

    override suspend fun savePosition(trackId: String, positionMs: Long) {
        savedPositions[trackId] = positionMs
    }

    override suspend fun getSavedPosition(trackId: String): Long {
        return savedPositions[trackId] ?: 0L
    }
}
