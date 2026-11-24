package com.nexus.tune.core.domain.offline

import com.nexus.tune.core.model.AutoStashCandidate
import com.nexus.tune.core.model.AutoStashConfig
import com.nexus.tune.core.model.AutoStashReport
import com.nexus.tune.core.model.AutoStashStrategy
import java.util.Locale

object AutoStashEvaluator {

    fun scoreCandidate(candidate: AutoStashCandidate, strategy: AutoStashStrategy): AutoStashCandidate {
        val playScore = (candidate.playCount * 5f).coerceAtMost(50f)
        val likeScore = if (candidate.isLiked) 40f else 0f

        val (finalScore, tag) = when (strategy) {
            AutoStashStrategy.BALANCED_HYBRID -> {
                val score = playScore + likeScore + 10f
                val reason = when {
                    candidate.isLiked && candidate.playCount >= 5 -> "Favorite & Heavy Rotation"
                    candidate.isLiked -> "Liked Gem"
                    candidate.playCount >= 5 -> "Heavy Rotation"
                    else -> "Discovery"
                }
                score to reason
            }
            AutoStashStrategy.FAVORITES_PRIORITY -> {
                val score = (if (candidate.isLiked) 100f else 0f) + (candidate.playCount * 2f)
                val reason = if (candidate.isLiked) "Favorite" else "Library Track"
                score to reason
            }
            AutoStashStrategy.HEAVY_ROTATION -> {
                val score = (candidate.playCount * 10f) + (if (candidate.isLiked) 20f else 0f)
                val reason = "Played ${candidate.playCount}x"
                score to reason
            }
            AutoStashStrategy.OFFLINE_ESSENTIALS -> {
                val durationFactor = (300f - candidate.track.durationSeconds.toFloat()).coerceAtLeast(0f) / 3f
                val score = durationFactor + playScore + likeScore
                val reason = "Compact Track"
                score to reason
            }
        }

        return candidate.copy(
            priorityScore = finalScore,
            reasonTag = tag
        )
    }

    fun evaluateAutoStash(
        candidates: List<AutoStashCandidate>,
        config: AutoStashConfig,
        currentStashedTrackIds: Set<String> = emptySet()
    ): AutoStashReport {
        if (!config.isEnabled || candidates.isEmpty()) {
            return AutoStashReport(
                budgetBytes = config.budgetBytes,
                strategy = config.strategy,
                evictionTrackIds = if (!config.isEnabled) currentStashedTrackIds else emptySet()
            )
        }

        val scored = candidates.map { scoreCandidate(it, config.strategy) }
            .sortedWith(
                compareByDescending<AutoStashCandidate> { it.priorityScore }
                    .thenByDescending { it.playCount }
                    .thenBy { it.track.durationSeconds }
            )

        val selected = mutableListOf<AutoStashCandidate>()
        var accumulatedBytes = 0L
        var accumulatedSeconds = 0L

        for (candidate in scored) {
            if (accumulatedBytes + candidate.estimatedSizeBytes <= config.budgetBytes) {
                selected.add(candidate)
                accumulatedBytes += candidate.estimatedSizeBytes
                accumulatedSeconds += candidate.track.durationSeconds
            }
        }

        val newStashedIds = selected.map { it.track.id }.toSet()
        val evictions = currentStashedTrackIds - newStashedIds
        val hours = accumulatedSeconds.toFloat() / 3600f

        return AutoStashReport(
            stashedCount = selected.size,
            totalUsedBytes = accumulatedBytes,
            budgetBytes = config.budgetBytes,
            estimatedOfflineHours = hours,
            stashedTracks = selected.map { it.track },
            evictionTrackIds = evictions,
            strategy = config.strategy
        )
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
            bytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes.toFloat() / (1024f * 1024f))
            else -> String.format(Locale.US, "%.2f GB", bytes.toFloat() / (1024f * 1024f * 1024f))
        }
    }

    fun formatOfflineHours(hours: Float): String {
        return String.format(Locale.US, "%.1f hrs", hours)
    }
}

