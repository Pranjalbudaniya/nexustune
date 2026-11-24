package com.nexus.tune.core.domain.library

import com.nexus.tune.core.model.LibrarySortOrder
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import java.util.Locale

object LibraryFilterEngine {

    fun filterTracks(
        tracks: List<Track>,
        query: String,
        sortOrder: LibrarySortOrder = LibrarySortOrder.RECENT
    ): List<Track> {
        val trimmed = query.trim().lowercase(Locale.ROOT)
        val filtered = if (trimmed.isBlank()) {
            tracks
        } else {
            tracks.filter { track ->
                track.title.lowercase(Locale.ROOT).contains(trimmed) ||
                track.artist.lowercase(Locale.ROOT).contains(trimmed) ||
                (track.album?.lowercase(Locale.ROOT)?.contains(trimmed) == true)
            }
        }

        return when (sortOrder) {
            LibrarySortOrder.RECENT -> filtered
            LibrarySortOrder.TITLE_ASC -> filtered.sortedBy { it.title.lowercase(Locale.ROOT) }
            LibrarySortOrder.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase(Locale.ROOT) }
            LibrarySortOrder.DURATION_DESC -> filtered.sortedByDescending { it.durationSeconds }
        }
    }

    fun filterPlaylists(
        playlists: List<Playlist>,
        query: String
    ): List<Playlist> {
        val trimmed = query.trim().lowercase(Locale.ROOT)
        if (trimmed.isBlank()) return playlists

        return playlists.filter { playlist ->
            playlist.title.lowercase(Locale.ROOT).contains(trimmed) ||
            (playlist.author?.lowercase(Locale.ROOT)?.contains(trimmed) == true)
        }
    }
}
