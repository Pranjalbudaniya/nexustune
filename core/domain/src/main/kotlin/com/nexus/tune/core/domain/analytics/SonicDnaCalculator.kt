package com.nexus.tune.core.domain.analytics

import com.nexus.tune.core.model.GenreShare
import com.nexus.tune.core.model.ListeningStats
import com.nexus.tune.core.model.ListeningStreak
import com.nexus.tune.core.model.SonicArchetype
import com.nexus.tune.core.model.SonicDnaProfile
import java.util.Locale

object SonicDnaCalculator {

    fun calculateSonicDna(
        stats: ListeningStats,
        streak: ListeningStreak,
        averageTrackDurationSec: Long = 210L
    ): SonicDnaProfile {
        val totalPlays = stats.totalPlays
        val estimatedMins = (totalPlays * averageTrackDurationSec) / 60L
        val dominantGenre = stats.topGenres.firstOrNull()?.genre ?: "Eclectic"

        val archetype = determineArchetype(stats, streak, dominantGenre)
        val (startHex, endHex) = getArchetypeGradients(archetype)

        // Variety score: proportion of unique artists relative to total plays, clamped 10-100%
        val variety = if (totalPlays > 0) {
            ((stats.topArtists.size.toFloat() / totalPlays.coerceAtLeast(1)) * 100).toInt().coerceIn(10, 100)
        } else {
            50
        }

        // Energy score: high daytime/afternoon plays + electronic/hiphop/rock genres
        val daytimePlays = stats.morningPlays + stats.afternoonPlays
        val eveningNightPlays = stats.eveningPlays + stats.nightPlays
        val totalTimePlays = (daytimePlays + eveningNightPlays).coerceAtLeast(1)
        val diurnalEnergyRatio = (daytimePlays.toFloat() / totalTimePlays) * 50f
        val genreEnergyBoost = when (dominantGenre.lowercase(Locale.US)) {
            "electronic", "dance", "hip-hop", "workout", "rock", "pop" -> 40f
            "classical", "ambient", "sleep", "lo-fi" -> 10f
            else -> 25f
        }
        val energyScore = (diurnalEnergyRatio + genreEnergyBoost).toInt().coerceIn(10, 100)

        // Acoustic score: higher for classical, acoustic, jazz, folk, chill
        val acousticScore = when (dominantGenre.lowercase(Locale.US)) {
            "classical", "acoustic", "jazz", "folk", "ambient" -> 85
            "rock", "country", "indie" -> 60
            "pop", "r&b" -> 45
            "electronic", "dance", "hip-hop" -> 20
            else -> 50
        }

        return SonicDnaProfile(
            archetype = archetype,
            dominantGenre = dominantGenre,
            genreDistribution = stats.topGenres,
            peakPeriod = stats.peakPeriod,
            varietyScorePercent = variety,
            energyScorePercent = energyScore,
            acousticScorePercent = acousticScore,
            totalTracksPlayed = totalPlays,
            estimatedMinutes = estimatedMins,
            topArtists = stats.topArtists.take(5),
            streakDays = maxOf(streak.currentStreakDays, streak.longestStreakDays),
            gradientStartHex = startHex,
            gradientEndHex = endHex
        )
    }

    private fun determineArchetype(
        stats: ListeningStats,
        streak: ListeningStreak,
        dominantGenre: String
    ): SonicArchetype {
        val maxStreak = maxOf(streak.currentStreakDays, streak.longestStreakDays)
        val genreLower = dominantGenre.lowercase(Locale.US)

        return when {
            // Unbroken multi-day streak
            maxStreak >= 7 -> SonicArchetype.CHRONO_DISCOVERER
            // Late night dominant
            stats.nightPlays > stats.morningPlays && stats.nightPlays > stats.afternoonPlays && stats.nightPlays >= 5 ->
                SonicArchetype.NOCTURNAL_VOYAGER
            // Early morning dominant
            stats.morningPlays > stats.nightPlays && stats.morningPlays > stats.eveningPlays && stats.morningPlays >= 5 ->
                SonicArchetype.AURORA_PULSE
            // Wide variety of artists across catalog
            stats.topArtists.size >= 10 && stats.totalPlays >= 15 ->
                SonicArchetype.ECLECTIC_EXPLORER
            // Study / Focus / Ambient genres
            genreLower.contains("classical") || genreLower.contains("ambient") || genreLower.contains("lo-fi") || genreLower.contains("instrumental") ->
                SonicArchetype.DEEP_FOCUS_SCHOLAR
            // Kinetic high-tempo genres
            genreLower.contains("electronic") || genreLower.contains("dance") || genreLower.contains("workout") ->
                SonicArchetype.RHYTHMIC_ATHLETE
            // Acoustic / Jazz / Warmth genres
            genreLower.contains("acoustic") || genreLower.contains("jazz") || genreLower.contains("folk") || genreLower.contains("rock") ->
                SonicArchetype.ACOUSTIC_PURIST
            // Default balanced profile
            else -> SonicArchetype.SONIC_NOMAD
        }
    }

    fun getArchetypeGradients(archetype: SonicArchetype): Pair<String, String> {
        return when (archetype) {
            SonicArchetype.NOCTURNAL_VOYAGER -> Pair("#1E1B4B", "#4338CA") // Deep Indigo
            SonicArchetype.AURORA_PULSE -> Pair("#7C2D12", "#EA580C")     // Sunrise Amber
            SonicArchetype.DEEP_FOCUS_SCHOLAR -> Pair("#064E3B", "#059669")// Emerald Pine
            SonicArchetype.ECLECTIC_EXPLORER -> Pair("#1E3A8A", "#0284C7") // Sapphire Azure
            SonicArchetype.RHYTHMIC_ATHLETE -> Pair("#701A75", "#D946EF")  // Electric Magenta
            SonicArchetype.ACOUSTIC_PURIST -> Pair("#78350F", "#D97706")   // Warm Brass Wood
            SonicArchetype.CHRONO_DISCOVERER -> Pair("#831843", "#F43F5E") // Flame Rose
            SonicArchetype.SONIC_NOMAD -> Pair("#4A1D96", "#7C3AED")       // Royal Violet
        }
    }

    fun formatShareableSummary(profile: SonicDnaProfile): String {
        val artists = if (profile.topArtists.isNotEmpty()) profile.topArtists.joinToString(", ") else "Varied Artists"
        return """
            ╔════════════════════════════════════════╗
               NexusTune Sonic DNA Profile
            ╚════════════════════════════════════════╝
            ${profile.archetype.badgeIcon} Archetype: ${profile.archetype.title}
            "${profile.archetype.tagline}"

            📊 Sonic Metrics:
            • Variety Score: ${profile.varietyScorePercent}%
            • Energy Pulse: ${profile.energyScorePercent}%
            • Acoustic Depth: ${profile.acousticScorePercent}%

            🎵 Listening Snapshot:
            • Dominant Genre: ${profile.dominantGenre}
            • Peak Listening: ${profile.peakPeriod}
            • Tracks Played: ${profile.totalTracksPlayed}
            • Time Listened: ${profile.estimatedMinutes} mins
            • Daily Streak: ${profile.streakDays} days
            • Top Artists: $artists

            Generated by NexusTune - Pure 120Hz Sound.
        """.trimIndent()
    }
}
