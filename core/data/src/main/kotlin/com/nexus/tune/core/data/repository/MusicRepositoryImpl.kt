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
        val cached = database.lyricsDao().getLyrics(trackId)
        if (cached != null && (!cached.plainLyrics.isNullOrBlank() || !cached.syncedLyricsRaw.isNullOrBlank())) {
            val lines = cached.syncedLyricsRaw?.split("\n")?.mapNotNull { line ->
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val ts = parts[0].toLongOrNull()
                    if (ts != null) LyricsLine(ts, parts[1]) else null
                } else null
            } ?: emptyList()
            return MusicResult.Success(
                TrackLyrics(
                    trackId = trackId,
                    lines = lines,
                    isSynced = lines.isNotEmpty()
                )
            )
        }

        val remoteResult = musicSource.getLyrics(trackId, title, artist, durationSeconds)
        if (remoteResult is MusicResult.Success) {
            val lyrics = remoteResult.data
            val rawSynced = if (lyrics.lines.isNotEmpty()) {
                lyrics.lines.joinToString("\n") { "${it.startTimeMs}:${it.text}" }
            } else null
            val plain = if (lyrics.lines.isNotEmpty()) lyrics.lines.joinToString("\n") { it.text } else null
            database.lyricsDao().insertOrUpdate(
                com.nexus.tune.core.database.entity.LyricsEntity(
                    trackId = trackId,
                    title = title,
                    artist = artist,
                    plainLyrics = plain,
                    syncedLyricsRaw = rawSynced,
                    userOffsetMs = cached?.userOffsetMs ?: 0L
                )
            )
        }
        return remoteResult
    }

    override suspend fun saveLyricsOffset(trackId: String, offsetMs: Long) {
        val existing = database.lyricsDao().getLyrics(trackId)
        if (existing != null) {
            database.lyricsDao().updateOffset(trackId, offsetMs)
        } else {
            database.lyricsDao().insertOrUpdate(
                com.nexus.tune.core.database.entity.LyricsEntity(
                    trackId = trackId,
                    title = "",
                    artist = "",
                    userOffsetMs = offsetMs
                )
            )
        }
    }

    override suspend fun getLyricsOffset(trackId: String): Long {
        return database.lyricsDao().getOffset(trackId) ?: 0L
    }

    override suspend fun clearLyricsCache() {
        database.lyricsDao().clearAll()
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

            var morning = 0
            var afternoon = 0
            var evening = 0
            var night = 0

            val calendar = java.util.Calendar.getInstance()
            list.forEach { item ->
                calendar.timeInMillis = item.playedAtTimestamp
                when (calendar.get(java.util.Calendar.HOUR_OF_DAY)) {
                    in 6..11 -> morning++
                    in 12..17 -> afternoon++
                    in 18..23 -> evening++
                    else -> night++
                }
            }

            val tracksInfo = list.map { Triple(it.title, it.artist, it.album) }
            val peakPeriod = com.nexus.tune.core.domain.analytics.ListeningHabitAnalyzer.determinePeakPeriod(morning, afternoon, evening, night)
            val topGenres = com.nexus.tune.core.domain.analytics.ListeningHabitAnalyzer.analyzeGenres(tracksInfo)

            ListeningStats(
                totalPlays = totalPlays,
                topArtists = topArtists,
                morningPlays = morning,
                afternoonPlays = afternoon,
                eveningPlays = evening,
                nightPlays = night,
                topGenres = topGenres,
                peakPeriod = peakPeriod
            )
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database.playlistDao().getPlaylists().map { list ->
            list.map {
                Playlist(
                    id = it.id,
                    title = it.title,
                    author = it.author,
                    artworkUrl = it.artworkUrl,
                    trackCount = it.trackCount,
                    isUserCreated = it.isUserCreated
                )
            }
        }
    }

    override suspend fun createPlaylist(title: String, artworkUrl: String?): String {
        val id = java.util.UUID.randomUUID().toString()
        val entity = com.nexus.tune.core.database.entity.PlaylistEntity(
            id = id,
            title = title,
            author = "You",
            artworkUrl = artworkUrl,
            trackCount = 0,
            isUserCreated = true
        )
        database.playlistDao().insert(entity)
        return id
    }

    override suspend fun updatePlaylistArtwork(playlistId: String, artworkUrl: String?) {
        database.playlistDao().updateArtwork(playlistId, artworkUrl)
    }

    override suspend fun deletePlaylist(playlistId: String) {
        database.playlistDao().delete(playlistId)
    }
}

