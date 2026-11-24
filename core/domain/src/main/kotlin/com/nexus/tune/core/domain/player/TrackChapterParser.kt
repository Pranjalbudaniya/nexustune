package com.nexus.tune.core.domain.player

import com.nexus.tune.core.model.ChapterProgress
import com.nexus.tune.core.model.TrackChapter
import java.util.Locale

object TrackChapterParser {

    private val PREFIX_TIMESTAMP_REGEX = Regex(
        """(?m)^\s*\[?\(?(\d{1,2}:\d{2}(?::\d{2})?)\)?\]?\s*[-–—:]?\s*(.+)$"""
    )
    private val SUFFIX_TIMESTAMP_REGEX = Regex(
        """(?m)^\s*(.+?)\s*[-–—:]?\s*\[?\(?(\d{1,2}:\d{2}(?::\d{2})?)\)?\]?\s*$"""
    )

    /**
     * Parses raw text (such as YouTube stream descriptions, track comments, or cue-sheets)
     * and extracts ordered TrackChapters with bounded start and end times.
     */
    fun parseChapters(rawText: String, totalTrackDurationMs: Long = 0L): List<TrackChapter> {
        if (rawText.isBlank()) return emptyList()

        val rawChapters = mutableListOf<Pair<Long, String>>()

        rawText.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val prefixMatch = PREFIX_TIMESTAMP_REGEX.matchEntire(trimmed)
            if (prefixMatch != null) {
                val timeStr = prefixMatch.groupValues[1]
                val titleStr = prefixMatch.groupValues[2].trim().trim('-', '–', '—', ':').trim()
                val ms = parseTimestampToMs(timeStr)
                if (ms >= 0 && titleStr.isNotBlank()) {
                    rawChapters.add(Pair(ms, titleStr))
                    return@forEach
                }
            }

            val suffixMatch = SUFFIX_TIMESTAMP_REGEX.matchEntire(trimmed)
            if (suffixMatch != null) {
                val titleStr = suffixMatch.groupValues[1].trim().trim('-', '–', '—', ':').trim()
                val timeStr = suffixMatch.groupValues[2]
                val ms = parseTimestampToMs(timeStr)
                if (ms >= 0 && titleStr.isNotBlank()) {
                    rawChapters.add(Pair(ms, titleStr))
                }
            }
        }

        if (rawChapters.size < 2 && rawChapters.none { it.first == 0L }) {
            // Need at least 2 distinct timestamps to form a valid chapter set, or starting at 0:00
            if (rawChapters.isEmpty()) return emptyList()
        }

        val sorted = rawChapters.distinctBy { it.first }.sortedBy { it.first }
        if (sorted.isEmpty()) return emptyList()

        return sorted.mapIndexed { index, (startMs, title) ->
            val nextStartMs = sorted.getOrNull(index + 1)?.first
            val endMs = nextStartMs ?: if (totalTrackDurationMs > startMs) totalTrackDurationMs else null
            val durationMs = if (endMs != null) endMs - startMs else 0L

            TrackChapter(
                index = index + 1,
                title = title,
                startTimeMs = startMs,
                endTimeMs = endMs,
                durationFormatted = if (durationMs > 0L) formatDuration(durationMs) else ""
            )
        }
    }

    /**
     * Resolves the active chapter given a playback position in milliseconds.
     */
    fun resolveActiveChapter(chapters: List<TrackChapter>, positionMs: Long): TrackChapter? {
        if (chapters.isEmpty()) return null
        val clamped = positionMs.coerceAtLeast(0L)

        // Find chapter where start <= pos and (end == null || pos < end)
        for (chapter in chapters) {
            val end = chapter.endTimeMs
            if (clamped >= chapter.startTimeMs && (end == null || clamped < end)) {
                return chapter
            }
        }

        // If beyond last chapter, return the last chapter
        return if (clamped >= chapters.last().startTimeMs) chapters.last() else chapters.firstOrNull()
    }

    /**
     * Calculates real-time progress and countdown time within the active chapter.
     */
    fun calculateChapterProgress(
        chapter: TrackChapter,
        positionMs: Long,
        nextChapter: TrackChapter? = null
    ): ChapterProgress {
        val elapsed = (positionMs - chapter.startTimeMs).coerceAtLeast(0L)
        val end = chapter.endTimeMs

        val (fraction, remaining) = if (end != null && end > chapter.startTimeMs) {
            val total = end - chapter.startTimeMs
            val frac = (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            val rem = (end - positionMs).coerceAtLeast(0L)
            Pair(frac, rem)
        } else {
            Pair(0f, 0L)
        }

        return ChapterProgress(
            currentChapter = chapter,
            nextChapter = nextChapter,
            progressFraction = fraction,
            elapsedInChapterMs = elapsed,
            remainingInChapterMs = remaining,
            elapsedFormatted = formatDuration(elapsed),
            remainingFormatted = formatDuration(remaining)
        )
    }

    /**
     * Computes the seek target timestamp for next or previous chapter navigation.
     */
    fun getSeekTarget(chapters: List<TrackChapter>, positionMs: Long, forward: Boolean): Long? {
        if (chapters.isEmpty()) return null
        val current = resolveActiveChapter(chapters, positionMs) ?: return null

        return if (forward) {
            val nextIndex = current.index // 1-based index means next chapter is at index
            chapters.getOrNull(nextIndex)?.startTimeMs
        } else {
            val elapsedInCurrent = positionMs - current.startTimeMs
            if (elapsedInCurrent > 3000L || current.index <= 1) {
                // Restart current chapter if more than 3 seconds in
                current.startTimeMs
            } else {
                // Seek to previous chapter
                chapters.getOrNull(current.index - 2)?.startTimeMs ?: current.startTimeMs
            }
        }
    }

    fun parseTimestampToMs(timestamp: String): Long {
        val parts = timestamp.split(":").mapNotNull { it.trim().toLongOrNull() }
        return when (parts.size) {
            2 -> (parts[0] * 60 + parts[1]) * 1000L
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L
            else -> -1L
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}

