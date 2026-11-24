package com.nexus.tune.core.domain.playlist

import com.nexus.tune.core.model.*

object SmartPlaylistEvaluator {

    val BUILT_IN_PLAYLISTS: List<SmartPlaylistDefinition> = listOf(
        SmartPlaylistDefinition(
            id = "builtin_heavy_rotation",
            title = "Heavy Rotation",
            icon = "🔥",
            description = "Your most played tracks with 5 or more streams",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(
                    id = "rule_hr_1",
                    field = SmartRuleField.PLAY_COUNT,
                    operator = SmartRuleOperator.GREATER_THAN,
                    value = "4"
                )
            ),
            sortBy = SmartRuleSort.PLAY_COUNT_DESC,
            limit = 50,
            isBuiltIn = true
        ),
        SmartPlaylistDefinition(
            id = "builtin_liked_gems",
            title = "Liked Gems",
            icon = "⭐",
            description = "Favorite tracks marked with a heart",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(
                    id = "rule_lg_1",
                    field = SmartRuleField.IS_LIKED,
                    operator = SmartRuleOperator.EQUALS,
                    value = "true"
                )
            ),
            sortBy = SmartRuleSort.TITLE_ASC,
            limit = 100,
            isBuiltIn = true
        ),
        SmartPlaylistDefinition(
            id = "builtin_quick_jams",
            title = "Quick Jams",
            icon = "⚡",
            description = "Bite-sized tracks under 3 minutes",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(
                    id = "rule_qj_1",
                    field = SmartRuleField.DURATION_SECONDS,
                    operator = SmartRuleOperator.LESS_THAN,
                    value = "180"
                )
            ),
            sortBy = SmartRuleSort.DURATION_ASC,
            limit = 40,
            isBuiltIn = true
        ),
        SmartPlaylistDefinition(
            id = "builtin_deep_listening",
            title = "Deep Listening",
            icon = "🎧",
            description = "Extended tracks exceeding 5 minutes",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(
                    id = "rule_dl_1",
                    field = SmartRuleField.DURATION_SECONDS,
                    operator = SmartRuleOperator.GREATER_THAN,
                    value = "300"
                )
            ),
            sortBy = SmartRuleSort.DURATION_DESC,
            limit = 50,
            isBuiltIn = true
        ),
        SmartPlaylistDefinition(
            id = "builtin_offline_stash",
            title = "Offline Stash",
            icon = "💾",
            description = "Downloaded tracks ready for airplane mode",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(
                    id = "rule_os_1",
                    field = SmartRuleField.IS_DOWNLOADED,
                    operator = SmartRuleOperator.EQUALS,
                    value = "true"
                )
            ),
            sortBy = SmartRuleSort.ARTIST_ASC,
            limit = 100,
            isBuiltIn = true
        )
    )

    fun evaluate(
        tracks: List<Track>,
        playCounts: Map<String, Int> = emptyMap(),
        definition: SmartPlaylistDefinition
    ): List<Track> {
        val filtered = if (definition.rules.isEmpty()) {
            tracks
        } else {
            tracks.filter { track ->
                val playCount = playCounts[track.id] ?: 0
                when (definition.matchType) {
                    SmartRuleMatch.ALL -> definition.rules.all { matchesRule(track, playCount, it) }
                    SmartRuleMatch.ANY -> definition.rules.any { matchesRule(track, playCount, it) }
                }
            }
        }

        val sorted = when (definition.sortBy) {
            SmartRuleSort.DEFAULT -> filtered
            SmartRuleSort.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SmartRuleSort.ARTIST_ASC -> filtered.sortedBy { it.artist.lowercase() }
            SmartRuleSort.DURATION_ASC -> filtered.sortedBy { it.durationSeconds }
            SmartRuleSort.DURATION_DESC -> filtered.sortedByDescending { it.durationSeconds }
            SmartRuleSort.PLAY_COUNT_DESC -> filtered.sortedByDescending { playCounts[it.id] ?: 0 }
        }

        return if (definition.limit > 0) sorted.take(definition.limit) else sorted
    }

    private fun matchesRule(track: Track, playCount: Int, rule: SmartPlaylistRule): Boolean {
        return when (rule.field) {
            SmartRuleField.TITLE -> evaluateString(track.title, rule.operator, rule.value)
            SmartRuleField.ARTIST -> evaluateString(track.artist, rule.operator, rule.value)
            SmartRuleField.ALBUM -> evaluateString(track.album ?: "", rule.operator, rule.value)
            SmartRuleField.DURATION_SECONDS -> {
                val target = rule.value.toLongOrNull() ?: 0L
                evaluateNumeric(track.durationSeconds, rule.operator, target)
            }
            SmartRuleField.PLAY_COUNT -> {
                val target = rule.value.toIntOrNull() ?: 0
                evaluateNumeric(playCount.toLong(), rule.operator, target.toLong())
            }
            SmartRuleField.IS_LIKED -> {
                val target = rule.value.toBooleanStrictOrNull() ?: true
                track.isLiked == target
            }
            SmartRuleField.IS_DOWNLOADED -> {
                val target = rule.value.toBooleanStrictOrNull() ?: true
                track.isDownloaded == target
            }
        }
    }

    private fun evaluateString(actual: String, operator: SmartRuleOperator, value: String): Boolean {
        return when (operator) {
            SmartRuleOperator.EQUALS -> actual.equals(value, ignoreCase = true)
            SmartRuleOperator.CONTAINS -> actual.contains(value, ignoreCase = true)
            SmartRuleOperator.NOT_CONTAINS -> !actual.contains(value, ignoreCase = true)
            SmartRuleOperator.STARTS_WITH -> actual.startsWith(value, ignoreCase = true)
            SmartRuleOperator.GREATER_THAN -> actual.compareTo(value, ignoreCase = true) > 0
            SmartRuleOperator.LESS_THAN -> actual.compareTo(value, ignoreCase = true) < 0
        }
    }

    private fun evaluateNumeric(actual: Long, operator: SmartRuleOperator, target: Long): Boolean {
        return when (operator) {
            SmartRuleOperator.EQUALS -> actual == target
            SmartRuleOperator.GREATER_THAN -> actual > target
            SmartRuleOperator.LESS_THAN -> actual < target
            SmartRuleOperator.CONTAINS, SmartRuleOperator.STARTS_WITH -> actual.toString().contains(target.toString())
            SmartRuleOperator.NOT_CONTAINS -> !actual.toString().contains(target.toString())
        }
    }

    fun serializeRule(rule: SmartPlaylistRule): String {
        return "${rule.id}@@${rule.field.name}@@${rule.operator.name}@@${rule.value}"
    }

    fun deserializeRule(raw: String): SmartPlaylistRule? {
        val parts = raw.split("@@")
        if (parts.size < 4) return null
        val id = parts[0]
        val field = runCatching { SmartRuleField.valueOf(parts[1]) }.getOrNull() ?: return null
        val operator = runCatching { SmartRuleOperator.valueOf(parts[2]) }.getOrNull() ?: return null
        val value = parts[3]
        return SmartPlaylistRule(id, field, operator, value)
    }

    fun serializePlaylist(pl: SmartPlaylistDefinition): String {
        val rulesStr = pl.rules.joinToString(";;") { serializeRule(it) }
        return "${pl.id}:::${pl.title}:::${pl.icon}:::${pl.description}:::${pl.matchType.name}:::${pl.sortBy.name}:::${pl.limit}:::${pl.isBuiltIn}:::$rulesStr"
    }

    fun deserializePlaylist(raw: String): SmartPlaylistDefinition? {
        val parts = raw.split(":::")
        if (parts.size < 8) return null
        val id = parts[0]
        val title = parts[1]
        val icon = parts[2]
        val description = parts[3]
        val matchType = runCatching { SmartRuleMatch.valueOf(parts[4]) }.getOrDefault(SmartRuleMatch.ALL)
        val sortBy = runCatching { SmartRuleSort.valueOf(parts[5]) }.getOrDefault(SmartRuleSort.DEFAULT)
        val limit = parts[6].toIntOrNull() ?: 50
        val isBuiltIn = parts[7].toBooleanStrictOrNull() ?: false
        val rules = if (parts.size >= 9 && parts[8].isNotBlank()) {
            parts[8].split(";;").mapNotNull { deserializeRule(it) }
        } else {
            emptyList()
        }
        return SmartPlaylistDefinition(
            id = id,
            title = title,
            icon = icon,
            description = description,
            matchType = matchType,
            rules = rules,
            sortBy = sortBy,
            limit = limit,
            isBuiltIn = isBuiltIn
        )
    }

    fun serializePlaylists(playlists: List<SmartPlaylistDefinition>): String {
        return playlists.joinToString("###") { serializePlaylist(it) }
    }

    fun deserializePlaylists(raw: String): List<SmartPlaylistDefinition> {
        if (raw.isBlank()) return emptyList()
        return raw.split("###").mapNotNull { deserializePlaylist(it) }
    }
}
