package com.nexus.tune.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    val likedTracks: StateFlow<List<Track>> = musicRepository.getLikedTracks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val historyTracks: StateFlow<List<Track>> = musicRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun playTrack(track: Track, queue: List<Track>) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, queue)
        }
    }

    fun createPlaylist(title: String, artworkUrl: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            musicRepository.createPlaylist(title.trim(), artworkUrl?.trim()?.ifBlank { null })
        }
    }

    fun updatePlaylistArtwork(playlistId: String, artworkUrl: String?) {
        viewModelScope.launch {
            musicRepository.updatePlaylistArtwork(playlistId, artworkUrl?.trim()?.ifBlank { null })
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    val deduplicationStatus = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    fun deduplicateLikedTracks() {
        viewModelScope.launch {
            val removed = musicRepository.deduplicateLikedTracks()
            deduplicationStatus.value = if (removed > 0) "Removed $removed duplicate tracks" else "No duplicates found"
        }
    }
}

