package com.nexus.tune.core.domain.mood

import com.nexus.tune.core.model.MoodCategory
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import java.util.Locale

object MoodRadioEngine {

    private val moodKeywords = mapOf(
        MoodCategory.FOCUS to listOf(
            "focus", "study", "ambient", "lofi", "lo-fi", "piano", "classical",
            "instrumental", "binaural", "synthwave", "meditation", "reading", "coding"
        ),
        MoodCategory.WORKOUT to listOf(
            "workout", "pump", "gym", "bass", "trap", "hardstyle", "rock",
            "metal", "edm", "remix", "power", "run", "beast", "heavy", "drop"
        ),
        MoodCategory.CHILL to listOf(
            "chill", "relax", "acoustic", "sunset", "mellow", "breeze", "soul",
            "indie", "unplugged", "jazz", "cozy", "quiet", "coffee"
        ),
        MoodCategory.ENERGIZE to listOf(
            "energy", "party", "dance", "electronic", "upbeat", "pop", "anthem",
            "hype", "club", "house", "electro", "fast", "groove"
        ),
        MoodCategory.COMMUTE to listOf(
            "commute", "drive", "road", "radio", "hits", "classic", "favorites",
            "sing", "morning", "trip", "highway"
        ),
        MoodCategory.SLEEP to listOf(
            "sleep", "night", "lullaby", "dream", "bedtime", "calm", "gentle",
            "delta", "theta", "whisper", "peace", "rest", "dark"
        )
    )

    fun matchesMood(track: Track, mood: MoodCategory): Boolean {
        if (mood == MoodCategory.ALL) return true
        val keywords = moodKeywords[mood] ?: return true
        val combinedText = "${track.title} ${track.artist} ${track.album ?: ""}".lowercase(Locale.ROOT)
        return keywords.any { combinedText.contains(it) }
    }

    fun matchesMood(playlist: Playlist, mood: MoodCategory): Boolean {
        if (mood == MoodCategory.ALL) return true
        val keywords = moodKeywords[mood] ?: return true
        val titleText = "${playlist.title} ${playlist.author ?: ""}".lowercase(Locale.ROOT)
        if (keywords.any { titleText.contains(it) }) return true
        return playlist.tracks.any { matchesMood(it, mood) }
    }

    fun filterPlaylistsByMood(playlists: List<Playlist>, mood: MoodCategory): List<Playlist> {
        if (mood == MoodCategory.ALL) return playlists
        return playlists.mapNotNull { pl ->
            if (matchesMood(pl, mood)) {
                val matchingTracks = pl.tracks.filter { matchesMood(it, mood) }
                if (matchingTracks.isNotEmpty()) {
                    pl.copy(tracks = matchingTracks, trackCount = matchingTracks.size)
                } else {
                    pl
                }
            } else {
                null
            }
        }
    }

    fun createMoodRadio(pool: List<Track>, mood: MoodCategory, count: Int = 25): List<Track> {
        if (pool.isEmpty()) return emptyList()
        if (mood == MoodCategory.ALL) return pool.shuffled().take(count)

        val matching = pool.filter { matchesMood(it, mood) }
        return if (matching.size < count) {
            val remaining = pool.filterNot { matchesMood(it, mood) }.shuffled()
            (matching + remaining).take(count)
        } else {
            matching.shuffled().take(count)
        }
    }
}
