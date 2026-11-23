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
        addTracksNext(listOf(track))
    }

    override suspend fun addTracksNext(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        val currentEntities = database.queueDao().getQueueSnapshot()
        val currIdx = _currentIndex.value
        val currentTracks = currentEntities.map { it.toTrack() }
        val newTracks = currentTracks.toMutableList()
        val insertIndex = (currIdx + 1).coerceIn(0, newTracks.size)
        newTracks.addAll(insertIndex, tracks)
        setQueue(newTracks, currIdx)
    }

    override suspend fun moveTrack(fromIndex: Int, toIndex: Int) {
        // Reordering
    }

    override suspend fun removeTrack(index: Int) {
        val currentEntities = database.queueDao().getQueueSnapshot()
        val target = currentEntities.getOrNull(index) ?: return
        removeTracks(setOf(target.trackId))
    }

    override suspend fun removeTracks(trackIds: Set<String>) {
        if (trackIds.isEmpty()) return
        val currentEntities = database.queueDao().getQueueSnapshot()
        val currIdx = _currentIndex.value
        val currentPlayingId = currentEntities.getOrNull(currIdx)?.trackId
        val remainingEntities = currentEntities.filterNot { it.trackId in trackIds }
        val newTracks = remainingEntities.map { it.toTrack() }
        val newIndex = if (currentPlayingId != null) {
            newTracks.indexOfFirst { it.id == currentPlayingId }.coerceAtLeast(0)
        } else 0
        setQueue(newTracks, newIndex)
    }

    override suspend fun clearQueue() {
        database.queueDao().clearQueue()
        _currentIndex.value = 0
    }

    override suspend fun clearUpcoming() {
        val currentEntities = database.queueDao().getQueueSnapshot()
        val currIdx = _currentIndex.value
        val remaining = currentEntities.take(currIdx + 1).map { it.toTrack() }
        setQueue(remaining, currIdx)
    }

    private fun QueueEntity.toTrack() = Track(
        id = trackId,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        artworkUrl = artworkUrl,
        streamUrl = streamUrl
    )

    override suspend fun savePosition(trackId: String, positionMs: Long) {
        savedPositions[trackId] = positionMs
    }

    override suspend fun getSavedPosition(trackId: String): Long {
        return savedPositions[trackId] ?: 0L
    }
}
