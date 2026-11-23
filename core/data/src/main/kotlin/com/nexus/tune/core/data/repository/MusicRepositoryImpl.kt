package com.nexus.tune.core.data.repository

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.database.entity.HistoryEntity
import com.nexus.tune.core.database.entity.toEntity
import com.nexus.tune.core.database.entity.toModel
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.extractor.MusicSource
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImpl(
    private val musicSource: MusicSource,
    private val database: NexusTuneDatabase
) : MusicRepository {

    override suspend fun search(query: String, filter: String?): MusicResult<SearchResult> {
        return musicSource.search(query, filter)
    }

    override suspend fun getSearchSuggestions(query: String): List<String> {
        return musicSource.getSearchSuggestions(query)
    }

    override suspend fun getHomeFeed(): MusicResult<List<Playlist>> {
        return musicSource.getHomeFeed()
    }

    override suspend fun getTrackDetails(trackId: String): MusicResult<Track> {
        return musicSource.getTrackDetails(trackId)
    }

    override suspend fun resolveStreamUrl(trackId: String): MusicResult<AudioStream> {
        return musicSource.resolveStreamUrl(trackId)
    }

    override suspend fun getLyrics(
        trackId: String,
        title: String,
        artist: String,
        durationSeconds: Long
    ): MusicResult<TrackLyrics> {
        return musicSource.getLyrics(trackId, title, artist, durationSeconds)
    }

    override fun getLikedTracks(): Flow<List<Track>> {
        return database.trackDao().getLikedTracks().map { list -> list.map { it.toModel() } }
    }

    override suspend fun toggleLike(track: Track): Boolean {
        val existing = database.trackDao().getTrackById(track.id)
        val newLikedState = !(existing?.isLiked ?: track.isLiked)
        database.trackDao().insertOrUpdate(track.toEntity(isLikedOverride = newLikedState))
        return newLikedState
    }

    override fun getHistory(): Flow<List<Track>> {
        return database.historyDao().getHistory().map { list ->
            list.map {
                Track(
                    id = it.trackId,
                    title = it.title,
                    artist = it.artist,
                    album = it.album,
                    durationSeconds = it.durationSeconds,
                    artworkUrl = it.artworkUrl
                )
            }
        }
    }

    override suspend fun addToHistory(track: Track) {
        database.historyDao().insert(
            HistoryEntity(
                trackId = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationSeconds = track.durationSeconds,
                artworkUrl = track.artworkUrl
            )
        )
    }

    override fun getListeningStats(): Flow<ListeningStats> {
        return database.historyDao().getHistory().map { list ->
            val totalPlays = list.size
            val topArtists = list.groupingBy { it.artist }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(5)
                .map { it.key }
            ListeningStats(totalPlays = totalPlays, topArtists = topArtists)
        }
    }
}

