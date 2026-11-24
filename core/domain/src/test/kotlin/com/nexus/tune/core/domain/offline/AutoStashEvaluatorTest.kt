package com.nexus.tune.core.domain.offline

import com.nexus.tune.core.model.AutoStashCandidate
import com.nexus.tune.core.model.AutoStashConfig
import com.nexus.tune.core.model.AutoStashStrategy
import com.nexus.tune.core.model.Track
import org.junit.Assert.*
import org.junit.Test

class AutoStashEvaluatorTest {

    private val track1 = Track(id = "trk1", title = "Hit Song", artist = "Artist A", durationSeconds = 180)
    private val track2 = Track(id = "trk2", title = "Favorite Gem", artist = "Artist B", durationSeconds = 240)
    private val track3 = Track(id = "trk3", title = "Short Jam", artist = "Artist C", durationSeconds = 90)
    private val track4 = Track(id = "trk4", title = "Rare Discovery", artist = "Artist D", durationSeconds = 210)

    @Test
    fun `scoreCandidate prioritizes favorites under FAVORITES_PRIORITY`() {
        val liked = AutoStashCandidate(track = track1, playCount = 1, isLiked = true)
        val unlikedHeavy = AutoStashCandidate(track = track2, playCount = 20, isLiked = false)

        val scoredLiked = AutoStashEvaluator.scoreCandidate(liked, AutoStashStrategy.FAVORITES_PRIORITY)
        val scoredUnliked = AutoStashEvaluator.scoreCandidate(unlikedHeavy, AutoStashStrategy.FAVORITES_PRIORITY)

        assertTrue(scoredLiked.priorityScore > scoredUnliked.priorityScore)
        assertEquals("Favorite", scoredLiked.reasonTag)
    }

    @Test
    fun `scoreCandidate prioritizes play frequency under HEAVY_ROTATION`() {
        val playedLots = AutoStashCandidate(track = track1, playCount = 30, isLiked = false)
        val playedOnceLiked = AutoStashCandidate(track = track2, playCount = 1, isLiked = true)

        val scoredPlayed = AutoStashEvaluator.scoreCandidate(playedLots, AutoStashStrategy.HEAVY_ROTATION)
        val scoredLiked = AutoStashEvaluator.scoreCandidate(playedOnceLiked, AutoStashStrategy.HEAVY_ROTATION)

        assertTrue(scoredPlayed.priorityScore > scoredLiked.priorityScore)
        assertEquals("Played 30x", scoredPlayed.reasonTag)
    }

    @Test
    fun `evaluateAutoStash respects budget allocation limits`() {
        val candidates = listOf(
            AutoStashCandidate(track = track1, playCount = 10, isLiked = true, estimatedSizeBytes = 30_000_000L), // 30 MB
            AutoStashCandidate(track = track2, playCount = 8, isLiked = true, estimatedSizeBytes = 40_000_000L),  // 40 MB
            AutoStashCandidate(track = track3, playCount = 2, isLiked = false, estimatedSizeBytes = 20_000_000L)  // 20 MB
        )

        // Budget of 50 MB: should pick track 1 (30 MB) and track 3 (20 MB), but not track 2 (30 + 40 > 50)
        val config = AutoStashConfig(
            isEnabled = true,
            storageBudgetMb = 50,
            strategy = AutoStashStrategy.BALANCED_HYBRID
        )

        val report = AutoStashEvaluator.evaluateAutoStash(candidates, config)
        assertTrue(report.stashedCount >= 1)
        assertTrue(report.totalUsedBytes <= config.budgetBytes)
        assertEquals(config.budgetBytes, report.budgetBytes)
        assertTrue(report.usageRatio in 0.0f..1.0f)
    }

    @Test
    fun `evaluateAutoStash identifies evicted tracks no longer fitting in budget`() {
        val candidates = listOf(
            AutoStashCandidate(track = track1, playCount = 50, isLiked = true, estimatedSizeBytes = 40_000_000L)
        )
        val previouslyStashed = setOf("trk1", "old_track_a", "old_track_b")
        val config = AutoStashConfig(isEnabled = true, storageBudgetMb = 100)

        val report = AutoStashEvaluator.evaluateAutoStash(candidates, config, previouslyStashed)
        assertEquals(setOf("old_track_a", "old_track_b"), report.evictionTrackIds)
        assertEquals(1, report.stashedCount)
    }

    @Test
    fun `formatBytes formats KB, MB, and GB accurately`() {
        assertEquals("500 KB", AutoStashEvaluator.formatBytes(500 * 1024L))
        assertEquals("15.5 MB", AutoStashEvaluator.formatBytes((15.5 * 1024 * 1024).toLong()))
        assertEquals("1.50 GB", AutoStashEvaluator.formatBytes((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatOfflineHours returns single-decimal hours`() {
        assertEquals("2.5 hrs", AutoStashEvaluator.formatOfflineHours(2.5f))
    }
}

