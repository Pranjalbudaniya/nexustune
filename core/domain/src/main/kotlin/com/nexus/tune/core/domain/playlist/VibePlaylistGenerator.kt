package com.nexus.tune.core.domain.playlist

import com.nexus.tune.core.model.EnergyCurve
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.VibeMixRequest
import com.nexus.tune.core.model.VibeMixResult
import com.nexus.tune.core.model.VibeTheme
import java.util.Locale

object VibePlaylistGenerator {

    private val themeKeywords = mapOf(
        VibeTheme.LATE_NIGHT_CHILL to listOf(
            "chill", "lofi", "lo-fi", "ambient", "night", "sleep", "slow",
            "quiet", "calm", "mellow", "dream", "rain", "twilight", "moon"
        ),
        VibeTheme.HIGH_OCTANE_ENERGY to listOf(
            "energy", "workout", "pump", "run", "fast", "rock", "metal",
            "bass", "trap", "power", "anthem", "drop", "beat", "heavy"
        ),
        VibeTheme.DEEP_FOCUS_STUDY to listOf(
            "focus", "study", "piano", "classical", "acoustic", "instrumental",
            "binaural", "coding", "flow", "reading", "ambient", "meditation"
        ),
        VibeTheme.SUNSET_ACOUSTIC to listOf(
            "acoustic", "sunset", "folk", "unplugged", "jazz", "soul",
            "guitar", "warm", "breeze", "indie", "coffee", "golden"
        ),
        VibeTheme.CLUB_EUPHORIA to listOf(
            "club", "dance", "electronic", "edm", "house", "techno",
            "remix", "party", "bass", "synth", "rave", "disco", "groove"
        ),
        VibeTheme.TIME_MACHINE_DEEP_CUTS to listOf(
            "classic", "retro", "vintage", "old", "timeless", "gold",
            "memories", "nostalgia", "anthem", "original", "legend"
        ),
        VibeTheme.DYNAMIC_ECLECTIC to emptyList()
    )

    fun generateVibeMix(
        request: VibeMixRequest,
        candidateTracks: List<Track>
    ): VibeMixResult {
        if (candidateTracks.isEmpty()) {
            return VibeMixResult(
                title = "${request.theme.icon} ${request.theme.displayName}",
                theme = request.theme,
                tracks = emptyList(),
                totalDurationSeconds = 0L,
                formattedDuration = "0 min",
                curve = request.energyCurve
            )
        }

        val targetSeconds = request.targetDurationMinutes * 60L
        val scoredTracks = candidateTracks.map { track ->
            val score = calculateThemeMatchScore(track, request.theme)
            val energy = estimateTrackEnergy(track, request.theme)
            Triple(track, score, energy)
        }

        // Sort by theme relevance score descending, with diversity penalty for identical artists
        val sortedCandidates = scoredTracks.sortedByDescending { it.second }
        val selected = mutableListOf<Triple<Track, Int, Float>>()
        var accumulatedDuration = 0L
        val seenArtists = mutableSetOf<String>()

        // First pass: pick best matching unique artists
        for (item in sortedCandidates) {
            val track = item.first
            if (track.artist !in seenArtists || seenArtists.size >= 10) {
                selected.add(item)
                seenArtists.add(track.artist)
                accumulatedDuration += track.durationSeconds
                if (accumulatedDuration >= targetSeconds) break
            }
        }

        // Second pass: fill if still below target duration
        if (accumulatedDuration < targetSeconds) {
            for (item in sortedCandidates) {
                if (item !in selected) {
                    selected.add(item)
                    accumulatedDuration += item.first.durationSeconds
                    if (accumulatedDuration >= targetSeconds) break
                }
            }
        }

        // Order tracks according to the requested Energy Curve
        val orderedTracks = applyEnergyCurve(selected, request.energyCurve)
        val totalSecs = orderedTracks.sumOf { it.durationSeconds }
        val mins = totalSecs / 60
        val formatted = if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "$mins min"

        return VibeMixResult(
            title = "${request.theme.icon} ${request.theme.displayName} (${request.targetDurationMinutes}m)",
            theme = request.theme,
            tracks = orderedTracks,
            totalDurationSeconds = totalSecs,
            formattedDuration = formatted,
            curve = request.energyCurve
        )
    }

    private fun calculateThemeMatchScore(track: Track, theme: VibeTheme): Int {
        if (theme == VibeTheme.DYNAMIC_ECLECTIC) return 50
        val keywords = themeKeywords[theme] ?: return 50
        val text = "${track.title} ${track.artist} ${track.album ?: ""}".lowercase(Locale.ROOT)
        var matches = 0
        for (kw in keywords) {
            if (text.contains(kw)) matches += 20
        }
        return matches
    }

    private fun estimateTrackEnergy(track: Track, theme: VibeTheme): Float {
        val text = "${track.title} ${track.artist} ${track.album ?: ""}".lowercase(Locale.ROOT)
        var energy = 0.5f

        val highKeywords = listOf("fast", "run", "heavy", "drop", "party", "energy", "workout", "dance", "rock")
        val lowKeywords = listOf("chill", "slow", "quiet", "ambient", "sleep", "piano", "lofi", "acoustic")

        for (kw in highKeywords) {
            if (text.contains(kw)) energy += 0.15f
        }
        for (kw in lowKeywords) {
            if (text.contains(kw)) energy -= 0.15f
        }

        if (track.durationSeconds in 120..220) energy += 0.05f // snappy pop/dance length
        if (track.durationSeconds > 300) energy -= 0.05f       // extended mellow ballad/ambient

        return energy.coerceIn(0.1f, 1.0f)
    }

    private fun applyEnergyCurve(
        items: List<Triple<Track, Int, Float>>,
        curve: EnergyCurve
    ): List<Track> {
        if (items.size <= 2) return items.map { it.first }

        return when (curve) {
            EnergyCurve.RAMP_UP -> {
                // Starts mellow and builds to peak
                items.sortedBy { it.third }.map { it.first }
            }
            EnergyCurve.PEAK_SUSTAINED -> {
                // Highest energy first
                items.sortedByDescending { it.third }.map { it.first }
            }
            EnergyCurve.GENTLE_COOLDOWN -> {
                // Descending energy
                items.sortedByDescending { it.third }.map { it.first }
            }
            EnergyCurve.WAVE_DYNAMIC -> {
                // Interleave high and low energy items
                val sorted = items.sortedBy { it.third }
                val result = mutableListOf<Track>()
                var left = 0
                var right = sorted.size - 1
                while (left <= right) {
                    if (left == right) {
                        result.add(sorted[left].first)
                        break
                    }
                    result.add(sorted[right].first)
                    result.add(sorted[left].first)
                    left++
                    right--
                }
                result
            }
        }
    }
}

