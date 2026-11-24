package com.nexus.tune.core.domain.lyrics

import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.TrackLyrics

/**
 * Standard LRC lyrics parser supporting RFC / standard time tags,
 * multiple timestamp prefixes, offset tags, and metadata headers.
 */
object LrcParser {

    private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{2,3}))?\]""")
    private val METADATA_TAG_REGEX = Regex("""\[([a-zA-Z]+):(.*)\]""")

    data class ParsedLrc(
        val lines: List<LyricsLine>,
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val offsetMs: Long = 0L
    )

    fun parse(rawText: String, trackId: String = ""): TrackLyrics {
        val parsed = parseDetailed(rawText)
        return TrackLyrics(
            trackId = trackId,
            lines = parsed.lines,
            isSynced = parsed.lines.isNotEmpty()
        )
    }

    fun parseDetailed(rawText: String): ParsedLrc {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var globalOffsetMs = 0L

        val lyricsLines = mutableListOf<LyricsLine>()

        rawText.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            // Check for metadata tag e.g. [ti:Title]
            val metaMatch = METADATA_TAG_REGEX.matchEntire(line)
            if (metaMatch != null && !TIMESTAMP_REGEX.containsMatchIn(line)) {
                val tag = metaMatch.groupValues[1].lowercase()
                val value = metaMatch.groupValues[2].trim()
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                    "offset" -> globalOffsetMs = value.toLongOrNull() ?: 0L
                }
                return@forEach
            }

            // Find all timestamps in this line
            val matches = TIMESTAMP_REGEX.findAll(line).toList()
            if (matches.isNotEmpty()) {
                val text = line.substring(matches.last().range.last + 1).trim()
                for (match in matches) {
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val fractionStr = match.groupValues[3]
                    val millis = when (fractionStr.length) {
                        2 -> (fractionStr.toLongOrNull() ?: 0L) * 10
                        3 -> fractionStr.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val timestampMs = (minutes * 60 * 1000) + (seconds * 1000) + millis + globalOffsetMs
                    lyricsLines.add(LyricsLine(timestampMs.coerceAtLeast(0L), text))
                }
            }
        }

        val sortedLines = lyricsLines.sortedBy { it.startTimeMs }
        return ParsedLrc(
            lines = sortedLines,
            title = title,
            artist = artist,
            album = album,
            offsetMs = globalOffsetMs
        )
    }
}

