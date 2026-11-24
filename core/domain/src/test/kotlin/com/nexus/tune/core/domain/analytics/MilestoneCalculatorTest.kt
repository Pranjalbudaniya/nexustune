package com.nexus.tune.core.domain.analytics

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test

class MilestoneCalculatorTest {

    @Test
    fun `calculateStreaks returns zero streak when no days played`() {
        val streak = MilestoneCalculator.calculateStreaks(emptyList(), 20000L)
        assertEquals(0, streak.currentStreakDays)
        assertEquals(0, streak.longestStreakDays)
    }

    @Test
    fun `calculateStreaks calculates active streak correctly for consecutive days`() {
        val today = 20000L
        val playedDays = listOf(today, today - 1, today - 2) // 3-day streak
        val streak = MilestoneCalculator.calculateStreaks(playedDays, today)

        assertEquals(3, streak.currentStreakDays)
        assertEquals(3, streak.longestStreakDays)
        assertEquals(today, streak.lastActiveDateEpochDay)
    }

    @Test
    fun `calculateStreaks detects active streak if played yesterday`() {
        val today = 20000L
        val playedDays = listOf(today - 1, today - 2) // 2-day streak ending yesterday
        val streak = MilestoneCalculator.calculateStreaks(playedDays, today)

        assertEquals(2, streak.currentStreakDays)
        assertEquals(2, streak.longestStreakDays)
    }

    @Test
    fun `calculateStreaks resets current streak if broken but preserves longest streak`() {
        val today = 20000L
        val playedDays = listOf(today - 5, today - 6, today - 7, today - 8) // 4-day streak in past
        val streak = MilestoneCalculator.calculateStreaks(playedDays, today)

        assertEquals(0, streak.currentStreakDays)
        assertEquals(4, streak.longestStreakDays)
    }

    @Test
    fun `evaluateMilestones unlocks FIRST_BEAT and CENTURY_CLUB at correct thresholds`() {
        val stats = ListeningStats(totalPlays = 120, topArtists = List(55) { "Artist $it" })
        val streak = ListeningStreak(currentStreakDays = 7, longestStreakDays = 7)

        val milestones = MilestoneCalculator.evaluateMilestones(stats, streak, isAudiophileActive = true)

        val firstBeat = milestones.find { it.type == MilestoneType.FIRST_BEAT }
        assertNotNull(firstBeat)
        assertTrue(firstBeat!!.isUnlocked)
        assertEquals(1.0f, firstBeat.progressPercent, 0.001f)

        val centuryClub = milestones.find { it.type == MilestoneType.CENTURY_CLUB }
        assertNotNull(centuryClub)
        assertTrue(centuryClub!!.isUnlocked)
        assertEquals(1.0f, centuryClub.progressPercent, 0.001f)

        val explorer = milestones.find { it.type == MilestoneType.EXPLORER }
        assertNotNull(explorer)
        assertTrue(explorer!!.isUnlocked)

        val streakMaster = milestones.find { it.type == MilestoneType.STREAK_MASTER }
        assertNotNull(streakMaster)
        assertTrue(streakMaster!!.isUnlocked)

        val audiophile = milestones.find { it.type == MilestoneType.AUDIOPHILE }
        assertNotNull(audiophile)
        assertTrue(audiophile!!.isUnlocked)
    }

    @Test
    fun `evaluateMilestones calculates partial progress accurately for locked milestones`() {
        val stats = ListeningStats(totalPlays = 50, nightPlays = 25)
        val streak = ListeningStreak(currentStreakDays = 2, longestStreakDays = 3)

        val milestones = MilestoneCalculator.evaluateMilestones(stats, streak, isAudiophileActive = false)

        val nightOwl = milestones.find { it.type == MilestoneType.NIGHT_OWL }
        assertNotNull(nightOwl)
        assertFalse(nightOwl!!.isUnlocked)
        assertEquals(0.5f, nightOwl.progressPercent, 0.001f)

        val streakMaster = milestones.find { it.type == MilestoneType.STREAK_MASTER }
        assertNotNull(streakMaster)
        assertFalse(streakMaster!!.isUnlocked)
        assertTrue(streakMaster.progressPercent > 0.4f)
    }

    @Test
    fun `generateRecap structures listening snapshot accurately`() {
        val stats = ListeningStats(
            totalPlays = 100,
            topArtists = listOf("Artist One", "Artist Two"),
            peakPeriod = "Morning (6 AM - 12 PM)"
        )
        val streak = ListeningStreak(currentStreakDays = 5)

        val recap = MilestoneCalculator.generateRecap(stats, streak, averageDurationSecondsPerTrack = 180L)

        assertEquals(100, recap.totalPlays)
        assertEquals(300L, recap.totalMinutesListened) // 100 * 180 / 60
        assertEquals("Artist One", recap.topArtists.first())
        assertEquals(5, recap.streakDays)
    }
}
