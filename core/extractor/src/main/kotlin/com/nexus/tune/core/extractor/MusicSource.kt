package com.nexus.tune.core.extractor

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.model.*

interface MusicSource {
    suspend fun search(query: String, filter: String? = null): MusicResult<SearchResult>
    suspend fun getSearchSuggestions(query: String): List<String>
    suspend fun getHomeFeed(): MusicResult<List<Playlist>>
    suspend fun getTrackDetails(trackId: String): MusicResult<Track>
    suspend fun resolveStreamUrl(trackId: String): MusicResult<AudioStream>
    suspend fun getLyrics(trackId: String, title: String, artist: String, durationSeconds: Long): MusicResult<TrackLyrics>
}

