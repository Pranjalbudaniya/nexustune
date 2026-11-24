package com.nexus.tune.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.domain.library.LibraryFilterEngine
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.LibrarySortOrder
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(LibrarySortOrder.RECENT)
    val sortOrder: StateFlow<LibrarySortOrder> = _sortOrder.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setSortOrder(order: LibrarySortOrder) {
        _sortOrder.value = order
    }

    val likedTracks: StateFlow<List<Track>> = musicRepository.getLikedTracks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredLikedTracks: StateFlow<List<Track>> = combine(
        likedTracks,
        searchQuery,
        sortOrder
    ) { tracks, query, order ->
        LibraryFilterEngine.filterTracks(tracks, query, order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyTracks: StateFlow<List<Track>> = musicRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredHistoryTracks: StateFlow<List<Track>> = combine(
        historyTracks,
        searchQuery,
        sortOrder
    ) { tracks, query, order ->
        LibraryFilterEngine.filterTracks(tracks, query, order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredPlaylists: StateFlow<List<Playlist>> = combine(
        playlists,
        searchQuery
    ) { list, query ->
        LibraryFilterEngine.filterPlaylists(list, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playTrack(track: Track, queue: List<Track>) {
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(track, queue)
        }
    }

    fun playAll(queue: List<Track>) {
        if (queue.isEmpty()) return
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(queue.first(), queue)
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

    val deduplicationStatus = MutableStateFlow<String?>(null)

    fun deduplicateLikedTracks() {
        viewModelScope.launch {
            val removed = musicRepository.deduplicateLikedTracks()
            deduplicationStatus.value = if (removed > 0) "Removed $removed duplicate tracks" else "No duplicates found"
        }
    }
}
