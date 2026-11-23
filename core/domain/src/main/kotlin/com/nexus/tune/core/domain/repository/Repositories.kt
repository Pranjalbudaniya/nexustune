package com.nexus.tune.core.domain.repository

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun search(query: String, filter: String? = null): MusicResult<SearchResult>
    suspend fun getSearchSuggestions(query: String): List<String>
    suspend fun getHomeFeed(): MusicResult<List<Playlist>>
    suspend fun getTrackDetails(trackId: String): MusicResult<Track>
    suspend fun resolveStreamUrl(trackId: String): MusicResult<AudioStream>
    suspend fun getLyrics(trackId: String, title: String, artist: String, durationSeconds: Long): MusicResult<TrackLyrics>
    fun getLikedTracks(): Flow<List<Track>>
    suspend fun toggleLike(track: Track): Boolean
    fun getHistory(): Flow<List<Track>>
    suspend fun addToHistory(track: Track)
    fun getListeningStats(): Flow<ListeningStats>
}

interface QueueRepository {
    fun getQueue(): Flow<List<Track>>
    fun getQueueIndex(): Flow<Int>
    suspend fun setQueue(tracks: List<Track>, startIndex: Int)
    suspend fun addTrack(track: Track)
    suspend fun addNext(track: Track)
    suspend fun moveTrack(fromIndex: Int, toIndex: Int)
    suspend fun removeTrack(index: Int)
    suspend fun clearQueue()
    suspend fun savePosition(trackId: String, positionMs: Long)
    suspend fun getSavedPosition(trackId: String): Long
}

interface SettingsRepository {
    fun getThemeConfig(): Flow<ThemeConfig>
    suspend fun updateThemeConfig(config: ThemeConfig)
    fun getPreferredContentCountry(): Flow<String>
    suspend fun setPreferredContentCountry(country: String)
    fun getPreferredQuality(): Flow<String> // "HIGH", "MEDIUM", "LOW"
    suspend fun setPreferredQuality(quality: String)
    fun getEqualizerEnabled(): Flow<Boolean>
    suspend fun setEqualizerEnabled(enabled: Boolean)
    fun getEqualizerPreset(): Flow<Int>
    suspend fun setEqualizerPreset(preset: Int)
}



interface DownloadRepository {
    fun getDownloadedTracks(): Flow<List<Track>>
    suspend fun downloadTrack(track: Track): Boolean
    suspend fun deleteDownload(trackId: String)
}
