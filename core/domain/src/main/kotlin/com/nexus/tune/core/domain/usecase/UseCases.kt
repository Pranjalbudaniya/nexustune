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
}



class DownloadTrackUseCase(private val downloadRepository: DownloadRepository) {
    fun getDownloadedTracks(): Flow<List<Track>> = downloadRepository.getDownloadedTracks()
    suspend fun download(track: Track): Boolean = downloadRepository.downloadTrack(track)
    suspend fun delete(trackId: String) = downloadRepository.deleteDownload(trackId)
}

class GetListeningStatsUseCase(private val musicRepository: MusicRepository) {
    operator fun invoke(): Flow<ListeningStats> = musicRepository.getListeningStats()
}
