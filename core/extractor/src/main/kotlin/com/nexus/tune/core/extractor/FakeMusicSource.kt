package com.nexus.tune.core.extractor

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.model.*

class FakeMusicSource : MusicSource {
    private val tracks = mutableListOf(
        Track(
            id = "fake_1",
            title = "Midnight Odyssey",
            artist = "Synthetix",
            album = "Neon Horizon",
            durationSeconds = 214,
            artworkUrl = "https://picsum.photos/seed/fake1/400/400",
            streamUrl = "https://example.com/audio/fake_1.opus"
        ),
        Track(
            id = "fake_2",
            title = "Starlight Velocity",
            artist = "Nova Drift",
            album = "Cosmic Dreams",
            durationSeconds = 188,
            artworkUrl = "https://picsum.photos/seed/fake2/400/400",
            streamUrl = "https://example.com/audio/fake_2.opus"
        ),
        Track(
            id = "fake_3",
            title = "Cybernetic Pulse",
            artist = "Vector Void",
            album = "Subsystem",
            durationSeconds = 245,
            artworkUrl = "https://picsum.photos/seed/fake3/400/400",
            streamUrl = "https://example.com/audio/fake_3.opus"
        ),
        Track(
            id = "fake_4",
            title = "Echoes in the Rain",
            artist = "Solaris",
            album = "Ambient Realm",
            durationSeconds = 310,
            artworkUrl = "https://picsum.photos/seed/fake4/400/400",
            streamUrl = "https://example.com/audio/fake_4.opus"
        ),
        Track(
            id = "fake_5",
            title = "Hyperdrive Overload",
            artist = "Synthetix",
            album = "Neon Horizon",
            durationSeconds = 195,
            artworkUrl = "https://picsum.photos/seed/fake5/400/400",
            streamUrl = "https://example.com/audio/fake_5.opus"
        )
    )

    private val playlists = listOf(
        Playlist(
            id = "pl_quick_picks",
            title = "Quick Picks",
            author = "NexusTune",
            artworkUrl = "https://picsum.photos/seed/quickpicks/400/400",
            trackCount = 5,
            tracks = tracks
        ),
        Playlist(
            id = "pl_cyber_synth",
            title = "Synthwave Essentials",
            author = "NexusTune",
            artworkUrl = "https://picsum.photos/seed/synth/400/400",
            trackCount = 3,
            tracks = tracks.take(3)
        )
    )

    override suspend fun search(query: String, filter: String?): MusicResult<SearchResult> {
        val q = query.lowercase()
        val matched = tracks.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }
        return MusicResult.Success(SearchResult(tracks = matched))
    }

    override suspend fun getSearchSuggestions(query: String): List<String> {
        val q = query.lowercase()
        return tracks.map { it.title }.filter { it.lowercase().contains(q) }
    }

    override suspend fun getHomeFeed(): MusicResult<List<Playlist>> {
        return MusicResult.Success(playlists)
    }

    override suspend fun getTrackDetails(trackId: String): MusicResult<Track> {
        val track = tracks.find { it.id == trackId } ?: tracks.first()
        return MusicResult.Success(track)
    }

    override suspend fun resolveStreamUrl(trackId: String): MusicResult<AudioStream> {
        val track = tracks.find { it.id == trackId } ?: tracks.first()
        return MusicResult.Success(
            AudioStream(
                url = track.streamUrl ?: "https://example.com/audio/$trackId.opus",
                format = "Opus",
                bitrateKbps = 160,
                expiresAtTimestampMs = System.currentTimeMillis() + 10_800_000 // 3 hours
            )
        )
    }

    override suspend fun getLyrics(
        trackId: String,
        title: String,
        artist: String,
        durationSeconds: Long
    ): MusicResult<TrackLyrics> {
        val lines = listOf(
            LyricsLine(0L, "[Music]"),
            LyricsLine(12_000L, "Stepping out into neon lights"),
            LyricsLine(24_000L, "Chasing dreams across the nights"),
            LyricsLine(36_000L, "Signal reaches through the haze"),
            LyricsLine(48_000L, "Lost inside a cyber maze"),
            LyricsLine(60_000L, "NexusTune in endless play")
        )
        return MusicResult.Success(TrackLyrics(trackId, lines, true))
    }
}

