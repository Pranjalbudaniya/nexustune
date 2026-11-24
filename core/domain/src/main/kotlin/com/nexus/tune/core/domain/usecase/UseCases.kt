package com.nexus.tune.core.domain.usecase

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.*
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.Flow

class SearchMusicUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(query: String, filter: String? = null): MusicResult<SearchResult> {
        if (query.isBlank()) return MusicResult.Success(SearchResult())
        return musicRepository.search(query.trim(), filter)
    }

    suspend fun getSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return musicRepository.getSearchSuggestions(query.trim())
    }

    fun getRecentSearches(): Flow<List<String>> = musicRepository.getRecentSearches()
    suspend fun addRecentSearch(query: String) = musicRepository.addRecentSearch(query)
    suspend fun removeRecentSearch(query: String) = musicRepository.removeRecentSearch(query)
    suspend fun clearRecentSearches() = musicRepository.clearRecentSearches()
}

class GetHomeFeedUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(): MusicResult<List<Playlist>> {
        return musicRepository.getHomeFeed()
    }
}

class ResolveStreamUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(trackId: String): MusicResult<AudioStream> {
        return musicRepository.resolveStreamUrl(trackId)
    }
}

class ManageQueueUseCase(private val queueRepository: QueueRepository) {
    fun getQueue(): Flow<List<Track>> = queueRepository.getQueue()
    fun getQueueIndex(): Flow<Int> = queueRepository.getQueueIndex()

    suspend fun playTrackNow(track: Track, currentQueue: List<Track>) {
        val existingIndex = currentQueue.indexOfFirst { it.id == track.id }
        if (existingIndex >= 0) {
            queueRepository.setQueue(currentQueue, existingIndex)
        } else {
            val newQueue = currentQueue.toMutableList().apply { add(0, track) }
            queueRepository.setQueue(newQueue, 0)
        }
    }

    suspend fun playPlaylist(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        queueRepository.setQueue(tracks, startIndex.coerceIn(0, tracks.lastIndex))
    }

    suspend fun reorder(fromIndex: Int, toIndex: Int) {
        queueRepository.moveTrack(fromIndex, toIndex)
    }

    suspend fun remove(index: Int) {
        queueRepository.removeTrack(index)
    }

    suspend fun addTracksNext(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        queueRepository.addTracksNext(tracks)
    }

    suspend fun removeTracks(trackIds: Set<String>) {
        if (trackIds.isEmpty()) return
        queueRepository.removeTracks(trackIds)
    }

    suspend fun clearUpcoming() {
        queueRepository.clearUpcoming()
    }
}

class ManagePlaylistsUseCase(private val musicRepository: MusicRepository) {
    fun getPlaylists(): Flow<List<Playlist>> = musicRepository.getPlaylists()
    suspend fun createPlaylist(title: String, artworkUrl: String? = null): String =
        musicRepository.createPlaylist(title, artworkUrl)
    suspend fun updateArtwork(playlistId: String, artworkUrl: String?) =
        musicRepository.updatePlaylistArtwork(playlistId, artworkUrl)
    suspend fun deletePlaylist(playlistId: String) =
        musicRepository.deletePlaylist(playlistId)
}



class DownloadTrackUseCase(private val downloadRepository: DownloadRepository) {
    fun getDownloadedTracks(): Flow<List<Track>> = downloadRepository.getDownloadedTracks()
    suspend fun download(track: Track): Boolean = downloadRepository.downloadTrack(track)
    suspend fun delete(trackId: String) = downloadRepository.deleteDownload(trackId)
}

class GetListeningStatsUseCase(private val musicRepository: MusicRepository) {
    operator fun invoke(): Flow<ListeningStats> = musicRepository.getListeningStats()
}

class GetAutoPlayRecommendationsUseCase(private val musicRepository: MusicRepository) {
    suspend operator fun invoke(currentTrack: Track, currentQueue: List<Track>): List<Track> {
        val query = currentTrack.artist.ifBlank { currentTrack.title }
        if (query.isBlank()) return emptyList()

        val existingIds = currentQueue.map { it.id }.toSet() + currentTrack.id
        return when (val result = musicRepository.search(query)) {
            is MusicResult.Success -> {
                result.data.tracks.filter { it.id !in existingIds }
            }
            is MusicResult.Failure -> emptyList()
        }
    }
}
