package com.nexus.tune.core.domain.analytics

import com.nexus.tune.core.model.GenreShare

object ListeningHabitAnalyzer {

    private val GENRE_KEYWORDS = mapOf(
        "Acoustic" to listOf("acoustic", "unplugged", "folk", "fingerstyle", "ballad"),
        "Rock" to listOf("rock", "metal", "guitar", "punk", "grunge", "alternative"),
        "Electronic" to listOf("edm", "dance", "electronic", "synth", "house", "techno", "trance", "dubstep"),
        "Hip-Hop" to listOf("hip hop", "hip-hop", "rap", "trap", "beats", "freestyle", "drill"),
        "Jazz" to listOf("jazz", "blues", "swing", "soul", "saxophone"),
        "Classical" to listOf("classical", "orchestra", "piano", "symphony", "concerto", "instrumental"),
        "Pop" to listOf("pop", "dance-pop", "hit", "radio", "remix")
    )

    fun determinePeakPeriod(morning: Int, afternoon: Int, evening: Int, night: Int): String {
        val maxPlays = maxOf(morning, afternoon, evening, night)
        if (maxPlays == 0) return "All Day"
        return when (maxPlays) {
            morning -> "Morning (6 AM - 12 PM)"
            afternoon -> "Afternoon (12 PM - 6 PM)"
            evening -> "Evening (6 PM - 12 AM)"
            else -> "Late Night (12 AM - 6 AM)"
        }
    }

    fun analyzeGenres(tracks: List<Triple<String, String, String?>>): List<GenreShare> {
        if (tracks.isEmpty()) return emptyList()

        val counts = mutableMapOf<String, Int>()

        for ((title, artist, album) in tracks) {
            val text = "$title $artist ${album.orEmpty()}".lowercase()
            var matched = false
            for ((genre, keywords) in GENRE_KEYWORDS) {
                if (keywords.any { text.contains(it) }) {
                    counts[genre] = (counts[genre] ?: 0) + 1
                    matched = true
                    break
                }
            }
            if (!matched) {
                counts["Pop"] = (counts["Pop"] ?: 0) + 1
            }
        }

        val totalMatches = counts.values.sum()
        if (totalMatches == 0) return emptyList()

        return counts.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { (genre, count) ->
                val pct = ((count.toFloat() / totalMatches) * 100).toInt()
                GenreShare(genre = genre, playCount = count, percentage = pct)
            }
    }
}

