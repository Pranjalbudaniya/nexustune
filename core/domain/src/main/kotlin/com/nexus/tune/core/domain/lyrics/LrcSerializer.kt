package com.nexus.tune.core.domain.lyrics

import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.TrackLyrics
import java.util.Locale

/**
 * Serializes [TrackLyrics] or a list of [LyricsLine] into standard .lrc file text format.
 */
object LrcSerializer {

    fun serialize(
        lyrics: TrackLyrics,
        title: String? = null,
        artist: String? = null,
        album: String? = null
    ): String {
        return serialize(lyrics.lines, title, artist, album)
    }

    fun serialize(
        lines: List<LyricsLine>,
        title: String? = null,
        artist: String? = null,
        album: String? = null
    ): String {
        val sb = StringBuilder()
        if (!title.isNullOrBlank()) sb.append("[ti:$title]\n")
        if (!artist.isNullOrBlank()) sb.append("[ar:$artist]\n")
        if (!album.isNullOrBlank()) sb.append("[al:$album]\n")

        lines.sortedBy { it.startTimeMs }.forEach { line ->
            val totalSec = line.startTimeMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            val hundredths = (line.startTimeMs % 1000) / 10
            val timestamp = String.format(Locale.US, "[%02d:%02d.%02d]", min, sec, hundredths)
            sb.append(timestamp).append(" ").append(line.text).append("\n")
        }

        return sb.toString().trimEnd()
    }
}

