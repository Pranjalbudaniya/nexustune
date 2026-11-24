package com.nexus.tune.core.domain.metadata

import com.nexus.tune.core.model.CleanMetadataResult
import java.util.Locale

object MetadataNormalizerEngine {

    private val JUNK_PATTERNS = listOf(
        Regex("""(?i)\(official\s*(music)?\s*video\)"""),
        Regex("""(?i)\[official\s*(music)?\s*video\]"""),
        Regex("""(?i)\(official\s*audio\)"""),
        Regex("""(?i)\[official\s*audio\]"""),
        Regex("""(?i)\(lyric\s*video\)"""),
        Regex("""(?i)\[lyric\s*video\]"""),
        Regex("""(?i)\(lyrics\)"""),
        Regex("""(?i)\[lyrics\]"""),
        Regex("""(?i)\(audio\)"""),
        Regex("""(?i)\[audio\]"""),
        Regex("""(?i)\(visualizer\)"""),
        Regex("""(?i)\[visualizer\]"""),
        Regex("""(?i)\(performance\s*video\)"""),
        Regex("""(?i)\[performance\s*video\]"""),
        Regex("""(?i)\(4k\s*(60fps)?\)"""),
        Regex("""(?i)\[4k\s*(60fps)?\]"""),
        Regex("""(?i)\[hd\]"""),
        Regex("""(?i)\[hq\]"""),
        Regex("""(?i)\(hq\)"""),
        Regex("""(?i)\(live\)"""),
        Regex("""(?i)\[live\]"""),
        Regex("""(?i)\(official\s*hd\s*video\)"""),
        Regex("""(?i)\[official\s*hd\s*video\]""")
    )

    private val FEAT_PAREN_REGEX = Regex("""(?i)\((?:feat\.?|ft\.?|featuring)\s+([^)]+)\)""")
    private val FEAT_BRACKET_REGEX = Regex("""(?i)\[(?:feat\.?|ft\.?|featuring)\s+([^\]]+)\]""")

    private val MINOR_WORDS = setOf(
        "a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "from", "by", "in", "of", "with", "vs", "v"
    )

    fun toTitleCase(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val words = trimmed.split(Regex("""\s+"""))
        return words.mapIndexed { index, word ->
            val lower = word.lowercase(Locale.ROOT)
            if (index > 0 && index < words.size - 1 && lower in MINOR_WORDS) {
                lower
            } else {
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        }.joinToString(" ")
    }

    fun cleanMetadata(
        rawTitle: String,
        rawArtist: String,
        rawAlbum: String? = null
    ): CleanMetadataResult {
        var currentTitle = rawTitle.trim()
        var currentArtist = rawArtist.trim()
        val removedTags = mutableListOf<String>()
        val featured = mutableListOf<String>()

        // 1. Check for Hyphenated Artist - Title if artist is blank or generic
        if (currentTitle.contains(" - ") && (currentArtist.isBlank() || currentArtist.equals("Unknown Artist", ignoreCase = true) || currentArtist.equals("Trending", ignoreCase = true))) {
            val parts = currentTitle.split(" - ", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                currentArtist = parts[0].trim()
                currentTitle = parts[1].trim()
                removedTags.add("Split Artist - Title")
            }
        }

        // 2. Extract Featured Artists from Title
        FEAT_PAREN_REGEX.find(currentTitle)?.let { match ->
            val featName = match.groupValues[1].trim()
            if (featName.isNotEmpty()) {
                featured.add(featName)
                currentTitle = currentTitle.replace(match.value, "").trim()
                removedTags.add("feat. $featName")
            }
        }
        FEAT_BRACKET_REGEX.find(currentTitle)?.let { match ->
            val featName = match.groupValues[1].trim()
            if (featName.isNotEmpty()) {
                featured.add(featName)
                currentTitle = currentTitle.replace(match.value, "").trim()
                removedTags.add("feat. $featName")
            }
        }

        // 3. Strip Junk Suffixes
        for (pattern in JUNK_PATTERNS) {
            pattern.find(currentTitle)?.let { match ->
                removedTags.add(match.value.trim())
                currentTitle = currentTitle.replace(match.value, "").trim()
            }
        }

        // 4. Clean trailing and dangling symbols
        currentTitle = currentTitle.replace(Regex("""[-–—\s]+$"""), "").trim()
        currentTitle = currentTitle.replace(Regex("""^[-–—\s]+"""), "").trim()
        currentArtist = currentArtist.replace(Regex("""[-–—\s]+$"""), "").trim()

        // 5. Smart Title Casing
        val cleanedTitle = toTitleCase(currentTitle)
        val cleanedArtist = toTitleCase(currentArtist)

        // 6. Clean Album if provided
        var cleanedAlbum: String? = null
        if (!rawAlbum.isNullOrBlank()) {
            var albumTemp = rawAlbum.trim()
            for (pattern in JUNK_PATTERNS) {
                pattern.find(albumTemp)?.let { match ->
                    albumTemp = albumTemp.replace(match.value, "").trim()
                }
            }
            cleanedAlbum = toTitleCase(albumTemp.replace(Regex("""[-–—\s]+$"""), ""))
        }

        val modified = cleanedTitle != rawTitle || cleanedArtist != rawArtist || (rawAlbum != null && cleanedAlbum != rawAlbum)

        return CleanMetadataResult(
            originalTitle = rawTitle,
            cleanedTitle = cleanedTitle,
            originalArtist = rawArtist,
            cleanedArtist = cleanedArtist,
            originalAlbum = rawAlbum,
            cleanedAlbum = cleanedAlbum,
            featuredArtists = featured,
            removedPatterns = removedTags,
            isModified = modified
        )
    }

    fun formatBitrateBadge(bitrateKbps: Int, format: String): String {
        val cleanFormat = format.uppercase(Locale.ROOT)
        return when {
            bitrateKbps >= 1000 -> "$cleanFormat • Lossless (${bitrateKbps} kbps)"
            bitrateKbps >= 256 -> "$cleanFormat • High Quality (${bitrateKbps} kbps)"
            bitrateKbps >= 160 -> "$cleanFormat • Standard (${bitrateKbps} kbps)"
            else -> "$cleanFormat • Compact (${bitrateKbps} kbps)"
        }
    }
}

