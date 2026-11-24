package com.nexus.tune.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.domain.library.LibraryFilterEngine
import com.nexus.tune.core.domain.playlist.SmartPlaylistEvaluator
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val manageQueueUseCase: ManageQueueUseCase,
    private val settingsRepository: SettingsRepository? = null
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

    val vibeMixPreview = MutableStateFlow<VibeMixResult?>(null)

    fun previewVibeMix(request: VibeMixRequest) {
        val liked = likedTracks.value
        val history = historyTracks.value
        val candidatePool = (liked + history).distinctBy { it.id }
        val result = com.nexus.tune.core.domain.playlist.VibePlaylistGenerator.generateVibeMix(request, candidatePool)
        vibeMixPreview.value = result
    }

    fun saveVibeMixAsPlaylist(result: VibeMixResult, onComplete: ((String) -> Unit)? = null) {
        if (result.tracks.isEmpty()) return
        viewModelScope.launch {
            val playlistId = musicRepository.createPlaylist(result.title, result.tracks.firstOrNull()?.artworkUrl)
            musicRepository.addTracksToPlaylist(playlistId, result.tracks.map { it.id })
            onComplete?.invoke(playlistId)
        }
    }

    fun playVibeMixNow(result: VibeMixResult) {
        if (result.tracks.isEmpty()) return
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(result.tracks.first(), result.tracks)
        }
    }

    val smartPlaylists: StateFlow<List<SmartPlaylistDefinition>> =
        (settingsRepository?.getCustomSmartPlaylists() ?: flowOf(emptyList()))
            .map { custom ->
                SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS + custom
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS)

    val activeSmartPlaylistPreview = MutableStateFlow<Pair<SmartPlaylistDefinition, List<Track>>?>(null)

    fun previewSmartPlaylist(definition: SmartPlaylistDefinition) {
        val allTracks = (likedTracks.value + historyTracks.value).distinctBy { it.id }
        val playCounts = historyTracks.value.groupingBy { it.id }.eachCount()
        val evaluated = SmartPlaylistEvaluator.evaluate(allTracks, playCounts, definition)
        activeSmartPlaylistPreview.value = Pair(definition, evaluated)
    }

    fun dismissSmartPlaylistPreview() {
        activeSmartPlaylistPreview.value = null
    }

    fun playSmartPlaylist(definition: SmartPlaylistDefinition, shuffle: Boolean = false) {
        val allTracks = (likedTracks.value + historyTracks.value).distinctBy { it.id }
        val playCounts = historyTracks.value.groupingBy { it.id }.eachCount()
        val evaluated = SmartPlaylistEvaluator.evaluate(allTracks, playCounts, definition)
        if (evaluated.isEmpty()) return
        val queue = if (shuffle) evaluated.shuffled() else evaluated
        viewModelScope.launch {
            manageQueueUseCase.playTrackNow(queue.first(), queue)
        }
    }

    fun createCustomSmartPlaylist(
        title: String,
        icon: String = "✨",
        description: String = "",
        matchType: SmartRuleMatch = SmartRuleMatch.ALL,
        rules: List<SmartPlaylistRule> = emptyList(),
        sortBy: SmartRuleSort = SmartRuleSort.DEFAULT,
        limit: Int = 50
    ) {
        if (title.isBlank()) return
        val newPlaylist = SmartPlaylistDefinition(
            id = "custom_" + java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            icon = icon.ifBlank { "✨" },
            description = description.trim(),
            matchType = matchType,
            rules = rules,
            sortBy = sortBy,
            limit = limit,
            isBuiltIn = false
        )
        viewModelScope.launch {
            settingsRepository?.saveCustomSmartPlaylist(newPlaylist)
        }
    }

    fun deleteCustomSmartPlaylist(id: String) {
        viewModelScope.launch {
            settingsRepository?.deleteCustomSmartPlaylist(id)
            if (activeSmartPlaylistPreview.value?.first?.id == id) {
                activeSmartPlaylistPreview.value = null
            }
        }
    }

    fun updateTrackMetadata(trackId: String, title: String, artist: String, album: String?) {
        viewModelScope.launch {
            musicRepository.updateTrackMetadata(trackId, title, artist, album)
        }
    }
}
