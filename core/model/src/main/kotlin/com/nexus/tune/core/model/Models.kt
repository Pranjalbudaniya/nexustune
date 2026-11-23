package com.nexus.tune.core.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val album: String? = null,
    val albumId: String? = null,
    val durationSeconds: Long = 0L,
    val artworkUrl: String? = null,
    val streamUrl: String? = null,
    val isExplicit: Boolean = false,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String? = null,
    val artworkUrl: String? = null,
    val year: Int? = null,
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val topTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class Playlist(
    val id: String,
    val title: String,
    val author: String? = null,
    val artworkUrl: String? = null,
    val trackCount: Int = 0,
    val isUserCreated: Boolean = false,
    val tracks: List<Track> = emptyList()
)

data class AudioStream(
    val url: String,
    val format: String, // Opus, AAC
    val bitrateKbps: Int,
    val expiresAtTimestampMs: Long
)

enum class PlaybackStatus {
    IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val isRepeatOne: Boolean = false,
    val isRepeatAll: Boolean = false,
    val queueIndex: Int = 0,
    val queue: List<Track> = emptyList(),
    val errorMessage: String? = null
)

enum class ThemeSeedMode {
    DYNAMIC, VIBRANT, EXPRESSIVE, AMOLED, CUSTOM
}

enum class ShapePreset {
    SOFT_ROUNDED, FULL_EXPRESSIVE, GENTLE, SHARP, CUT_CORNER
}

enum class PlayerLayout {
    ART_FORWARD, VINYL_RECORD, SYNCED_LYRICS, MINIMALIST
}

enum class MiniPlayerStyle {
    DOCKED_BAR, FLOATING_PILL, COMPACT_SLIM
}

data class ThemeConfig(
    val seedMode: ThemeSeedMode = ThemeSeedMode.DYNAMIC,
    val customSeedColorHex: String = "#6750A4",
    val isDarkMode: Boolean = true,
    val isAmoled: Boolean = false,
    val shapePreset: ShapePreset = ShapePreset.SOFT_ROUNDED,
    val playerLayout: PlayerLayout = PlayerLayout.ART_FORWARD,
    val miniPlayerStyle: MiniPlayerStyle = MiniPlayerStyle.DOCKED_BAR,
    val refreshRatePreferred: Int = 120 // 60, 90, 120
)

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class LyricsLine(
    val startTimeMs: Long,
    val text: String
)

data class TrackLyrics(
    val trackId: String,
    val lines: List<LyricsLine> = emptyList(),
    val isSynced: Boolean = true
)

enum class EqualizerPreset {
    FLAT, BASS_BOOST, TREBLE_BOOST, VOCAL, ELECTRONIC, ROCK
}

data class ListeningStats(
    val totalPlays: Int = 0,
    val topArtists: List<String> = emptyList()
)

