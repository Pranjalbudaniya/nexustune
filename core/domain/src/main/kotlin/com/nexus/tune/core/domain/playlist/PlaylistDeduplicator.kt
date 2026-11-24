package com.nexus.tune.core.domain.playlist

import com.nexus.tune.core.model.Track

data class DuplicateReport(
    val duplicateCount: Int,
    val duplicateTracks: List<Track>,
    val deduplicatedTracks: List<Track>
)

object PlaylistDeduplicator {

    private val NOISE_PATTERNS = listOf(
        "\\(official audio\\)",
        "\\[official audio\\]",
        "\\(official music video\\)",
        "\\[official music video\\]",
        "\\(official video\\)",
        "\\[official video\\]",
        "\\(lyric video\\)",
        "\\[lyric video\\]",
        "\\(lyrics\\)",
        "\\[lyrics\\]",
        "\\(audio\\)",
        "\\[audio\\]",
        "\\(hd\\)",
        "\\[hd\\]",
        "\\(hq\\)",
        "\\[hq\\]"
    ).map { Regex(it, RegexOption.IGNORE_CASE) }

    fun normalizeTitle(rawTitle: String): String {
        var clean = rawTitle
        for (pattern in NOISE_PATTERNS) {
            clean = pattern.replace(clean, "")
        }
        return clean.trim().lowercase()
    }

    fun deduplicate(tracks: List<Track>): DuplicateReport {
        val seenIds = mutableSetOf<String>()
        val seenNormalizedKeys = mutableSetOf<String>()
        val uniqueList = mutableListOf<Track>()
        val duplicates = mutableListOf<Track>()

        for (track in tracks) {
            val normalizedTitle = normalizeTitle(track.title)
            val normalizedArtist = track.artist.trim().lowercase()
            val semanticKey = "$normalizedArtist::$normalizedTitle"

            if (seenIds.contains(track.id) || seenNormalizedKeys.contains(semanticKey)) {
                duplicates.add(track)
            } else {
                seenIds.add(track.id)
                seenNormalizedKeys.add(semanticKey)
                uniqueList.add(track)
            }
        }

        return DuplicateReport(
            duplicateCount = duplicates.size,
            duplicateTracks = duplicates,
            deduplicatedTracks = uniqueList
        )
    }
}
