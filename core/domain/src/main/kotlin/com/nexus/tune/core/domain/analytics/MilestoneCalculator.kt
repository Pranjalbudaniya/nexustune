package com.nexus.tune.core.domain.analytics

import com.nexus.tune.core.model.*

object MilestoneCalculator {

    /**
     * Calculates consecutive daily listening streaks based on epoch day timestamps (1 epoch day = 86400 seconds).
     */
    fun calculateStreaks(
        playedEpochDays: List<Long>,
        currentEpochDay: Long
    ): ListeningStreak {
        if (playedEpochDays.isEmpty()) {
            return ListeningStreak()
        }

        val sortedDays = playedEpochDays.distinct().sortedDescending()
        val latestPlayedDay = sortedDays.first()

        // If user played today or yesterday, streak is active; otherwise 0
        val isCurrentStreakActive = latestPlayedDay == currentEpochDay || latestPlayedDay == currentEpochDay - 1
        var currentStreak = 0
        if (isCurrentStreakActive) {
            currentStreak = 1
            var prev = latestPlayedDay
            for (i in 1 until sortedDays.size) {
                val day = sortedDays[i]
                if (day == prev - 1) {
                    currentStreak++
                    prev = day
                } else {
                    break
                }
            }
        }

        // Longest streak across all historical days
        val ascendingDays = sortedDays.sorted()
        var longestStreak = 0
        var tempStreak = 0
        var lastDay = -1L
        for (day in ascendingDays) {
            if (lastDay == -1L || day == lastDay + 1) {
                tempStreak++
            } else if (day > lastDay + 1) {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            lastDay = day
        }

        return ListeningStreak(
            currentStreakDays = currentStreak,
            longestStreakDays = maxOf(currentStreak, longestStreak),
            lastActiveDateEpochDay = latestPlayedDay
        )
    }

    /**
     * Evaluates milestone completion and progress percentages from listening statistics and streaks.
     */
    fun evaluateMilestones(
        stats: ListeningStats,
        streak: ListeningStreak,
        isAudiophileActive: Boolean = false
    ): List<ListeningMilestone> {
        return MilestoneType.values().map { type ->
            val (current, target) = when (type) {
                MilestoneType.FIRST_BEAT -> stats.totalPlays to 1
                MilestoneType.NIGHT_OWL -> stats.nightPlays to 50
                MilestoneType.EARLY_BIRD -> stats.morningPlays to 50
                MilestoneType.EXPLORER -> stats.topArtists.size to 50
                MilestoneType.CENTURY_CLUB -> stats.totalPlays to 100
                MilestoneType.STREAK_MASTER -> maxOf(streak.currentStreakDays, streak.longestStreakDays) to 7
                MilestoneType.AUDIOPHILE -> (if (isAudiophileActive) 1 else 0) to 1
            }

            val isUnlocked = current >= target
            val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

            ListeningMilestone(
                type = type,
                title = type.title,
                description = type.description,
                isUnlocked = isUnlocked,
                progressPercent = progress,
                currentCount = current,
                targetCount = target
            )
        }
    }

    /**
     * Generates a structured time-capsule recap snapshot from listening statistics and streak data.
     */
    fun generateRecap(
        stats: ListeningStats,
        streak: ListeningStreak,
        averageDurationSecondsPerTrack: Long = 210L
    ): TimeCapsuleRecap {
        val estimatedTotalMinutes = (stats.totalPlays * averageDurationSecondsPerTrack) / 60L
        return TimeCapsuleRecap(
            periodName = "Personal Music Time-Capsule",
            totalPlays = stats.totalPlays,
            totalMinutesListened = estimatedTotalMinutes,
            topArtists = stats.topArtists.take(5),
            topGenres = stats.topGenres,
            peakPeriod = stats.peakPeriod,
            streakDays = streak.currentStreakDays
        )
    }
}
