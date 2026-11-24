package com.nexus.tune.core.domain.queue

import com.nexus.tune.core.model.Track
import kotlin.random.Random

/**
 * Intelligent shuffle engine that spaces out tracks by the same artist
 * to avoid repetitive clustering, while preserving acoustic diversity.
 */
object SmartShuffleEngine {

    /**
     * Shuffles [tracks] ensuring tracks with identical artists are spaced as evenly as possible.
     * If [currentTrack] is provided and present in [tracks], it is pinned at the start (index 0).
     */
    fun shuffle(
        tracks: List<Track>,
        currentTrack: Track? = null,
        smartDiversity: Boolean = true,
        random: Random = Random.Default
    ): List<Track> {
        if (tracks.size <= 2) return tracks

        val pinnedTrack = currentTrack?.let { ct -> tracks.find { it.id == ct.id } }
        val remaining = if (pinnedTrack != null) {
            tracks.filter { it.id != pinnedTrack.id }
        } else {
            tracks
        }

        if (remaining.size <= 1) {
            return listOfNotNull(pinnedTrack) + remaining
        }

        if (!smartDiversity) {
            return listOfNotNull(pinnedTrack) + remaining.shuffled(random)
        }

        // Group tracks by normalized artist name
        val groupedByArtist = remaining.groupBy { it.artist.trim().lowercase() }
            .mapValues { (_, list) -> list.shuffled(random).toMutableList() }
            .toMutableMap()

        val result = mutableListOf<Track>()
        var lastArtist = pinnedTrack?.artist?.trim()?.lowercase()

        val totalToPlace = remaining.size
        for (i in 0 until totalToPlace) {
            // Find candidate artists that are not the lastArtist (if possible)
            val candidates = groupedByArtist.filter { it.value.isNotEmpty() }
            if (candidates.isEmpty()) break

            val preferredCandidates = candidates.filter { it.key != lastArtist }
            val pool = if (preferredCandidates.isNotEmpty()) preferredCandidates else candidates

            // Pick from the candidate with the highest remaining count to maximize spacing,
            // with random tie-breaking among candidates with max count
            val maxCount = pool.values.maxOf { it.size }
            val bestArtists = pool.filter { it.value.size == maxCount }.keys.toList()
            val chosenArtist = bestArtists[random.nextInt(bestArtists.size)]

            val trackList = groupedByArtist[chosenArtist]!!
            val chosenTrack = trackList.removeAt(0)
            result.add(chosenTrack)
            lastArtist = chosenArtist
        }

        return listOfNotNull(pinnedTrack) + result
    }
}

