package com.nexus.tune.core.domain.analytics

import com.nexus.tune.core.model.GenreShare
import com.nexus.tune.core.model.ListeningStats
import com.nexus.tune.core.model.ListeningStreak
import com.nexus.tune.core.model.SonicArchetype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SonicDnaCalculatorTest {

    @Test
    fun `calculateSonicDna classifies Chrono Discoverer for 7-day streak`() {
        val stats = ListeningStats(totalPlays = 30)
        val streak = ListeningStreak(currentStreakDays = 7, longestStreakDays = 7)

        val profile = SonicDnaCalculator.calculateSonicDna(stats, streak)
        assertEquals(SonicArchetype.CHRONO_DISCOVERER, profile.archetype)
        assertEquals(7, profile.streakDays)
    }

    @Test
    fun `calculateSonicDna classifies Nocturnal Voyager when night plays dominate`() {
        val stats = ListeningStats(
            totalPlays = 40,
            morningPlays = 2,
            afternoonPlays = 3,
            eveningPlays = 5,
            nightPlays = 30
        )
        val streak = ListeningStreak(currentStreakDays = 2)

        val profile = SonicDnaCalculator.calculateSonicDna(stats, streak)
        assertEquals(SonicArchetype.NOCTURNAL_VOYAGER, profile.archetype)
    }

    @Test
    fun `calculateSonicDna classifies Aurora Pulse when morning plays dominate`() {
        val stats = ListeningStats(
            totalPlays = 40,
            morningPlays = 30,
            afternoonPlays = 2,
            eveningPlays = 3,
            nightPlays = 5
        )
        val streak = ListeningStreak(currentStreakDays = 1)

        val profile = SonicDnaCalculator.calculateSonicDna(stats, streak)
        assertEquals(SonicArchetype.AURORA_PULSE, profile.archetype)
    }

    @Test
    fun `calculateSonicDna classifies Eclectic Explorer for diverse artists`() {
        val stats = ListeningStats(
            totalPlays = 25,
            morningPlays = 5,
            afternoonPlays = 5,
            eveningPlays = 5,
            nightPlays = 5,
            topArtists = List(12) { "Artist $it" }
        )
        val streak = ListeningStreak(currentStreakDays = 2)

        val profile = SonicDnaCalculator.calculateSonicDna(stats, streak)
        assertEquals(SonicArchetype.ECLECTIC_EXPLORER, profile.archetype)
    }

    @Test
    fun `calculateSonicDna classifies genre-specific archetypes`() {
        // Deep Focus Scholar
        val scholarStats = ListeningStats(
            totalPlays = 10,
            topGenres = listOf(GenreShare("Classical / Lo-Fi", 10, 100))
        )
        val scholarProfile = SonicDnaCalculator.calculateSonicDna(scholarStats, ListeningStreak())
        assertEquals(SonicArchetype.DEEP_FOCUS_SCHOLAR, scholarProfile.archetype)
        assertTrue(scholarProfile.acousticScorePercent >= 80)

        // Rhythmic Athlete
        val athleteStats = ListeningStats(
            totalPlays = 10,
            topGenres = listOf(GenreShare("Electronic / Dance", 10, 100))
        )
        val athleteProfile = SonicDnaCalculator.calculateSonicDna(athleteStats, ListeningStreak())
        assertEquals(SonicArchetype.RHYTHMIC_ATHLETE, athleteProfile.archetype)
        assertTrue(athleteProfile.energyScorePercent >= 50)

        // Acoustic Purist
        val puristStats = ListeningStats(
            totalPlays = 10,
            topGenres = listOf(GenreShare("Acoustic / Folk", 10, 100))
        )
        val puristProfile = SonicDnaCalculator.calculateSonicDna(puristStats, ListeningStreak())
        assertEquals(SonicArchetype.ACOUSTIC_PURIST, puristProfile.archetype)
    }

    @Test
    fun `scores remain within 10 to 100 percent boundary`() {
        val emptyStats = ListeningStats()
        val emptyProfile = SonicDnaCalculator.calculateSonicDna(emptyStats, ListeningStreak())

        assertTrue(emptyProfile.varietyScorePercent in 10..100)
        assertTrue(emptyProfile.energyScorePercent in 10..100)
        assertTrue(emptyProfile.acousticScorePercent in 10..100)
    }

    @Test
    fun `formatShareableSummary contains branding and archetype details`() {
        val stats = ListeningStats(
            totalPlays = 50,
            topArtists = listOf("Daft Punk", "Chopin"),
            topGenres = listOf(GenreShare("Electronic", 50, 100))
        )
        val streak = ListeningStreak(currentStreakDays = 4)
        val profile = SonicDnaCalculator.calculateSonicDna(stats, streak)

        val summary = SonicDnaCalculator.formatShareableSummary(profile)
        assertNotNull(summary)
        assertTrue(summary.contains("NexusTune Sonic DNA Profile"))
        assertTrue(summary.contains(profile.archetype.title))
        assertTrue(summary.contains("Daft Punk"))
        assertTrue(summary.contains("Pure 120Hz Sound"))
    }
}

