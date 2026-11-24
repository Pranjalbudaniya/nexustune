package com.nexus.tune.core.domain.playlist

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test

class SmartPlaylistEvaluatorTest {

    private val track1 = Track(
        id = "t1",
        title = "Midnight Starlight",
        artist = "Solar Drift",
        album = "Cosmic Waves",
        durationSeconds = 150L, // 2.5 min
        isLiked = true,
        isDownloaded = false
    )

    private val track2 = Track(
        id = "t2",
        title = "Electric Symphony",
        artist = "Neon Echo",
        album = "Cyber Pulse",
        durationSeconds = 360L, // 6 min
        isLiked = true,
        isDownloaded = true
    )

    private val track3 = Track(
        id = "t3",
        title = "Acoustic Breeze",
        artist = "Cedar Wood",
        album = "Forest Melodies",
        durationSeconds = 210L, // 3.5 min
        isLiked = false,
        isDownloaded = true
    )

    private val track4 = Track(
        id = "t4",
        title = "Hyper Speed",
        artist = "Solar Drift",
        album = "Speed of Sound",
        durationSeconds = 120L, // 2 min
        isLiked = false,
        isDownloaded = false
    )

    private val sampleTracks = listOf(track1, track2, track3, track4)
    private val playCounts = mapOf(
        "t1" to 12,
        "t2" to 6,
        "t3" to 2,
        "t4" to 0
    )

    @Test
    fun `evaluate Heavy Rotation returns tracks with playCount greater than 4 ordered by play count desc`() {
        val heavyRotation = SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS.first { it.id == "builtin_heavy_rotation" }
        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, heavyRotation)

        assertEquals(2, result.size)
        assertEquals("t1", result[0].id) // 12 plays
        assertEquals("t2", result[1].id) // 6 plays
    }

    @Test
    fun `evaluate Liked Gems returns only liked tracks ordered by title asc`() {
        val likedGems = SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS.first { it.id == "builtin_liked_gems" }
        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, likedGems)

        assertEquals(2, result.size)
        assertTrue(result.all { it.isLiked })
        assertEquals("Electric Symphony", result[0].title)
        assertEquals("Midnight Starlight", result[1].title)
    }

    @Test
    fun `evaluate Quick Jams returns tracks shorter than 180 seconds ordered by duration asc`() {
        val quickJams = SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS.first { it.id == "builtin_quick_jams" }
        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, quickJams)

        assertEquals(2, result.size)
        assertEquals("t4", result[0].id) // 120s
        assertEquals("t1", result[1].id) // 150s
    }

    @Test
    fun `evaluate Deep Listening returns tracks longer than 300 seconds`() {
        val deepListening = SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS.first { it.id == "builtin_deep_listening" }
        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, deepListening)

        assertEquals(1, result.size)
        assertEquals("t2", result[0].id) // 360s
    }

    @Test
    fun `evaluate Offline Stash returns only downloaded tracks`() {
        val offlineStash = SmartPlaylistEvaluator.BUILT_IN_PLAYLISTS.first { it.id == "builtin_offline_stash" }
        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, offlineStash)

        assertEquals(2, result.size)
        assertTrue(result.all { it.isDownloaded })
    }

    @Test
    fun `evaluate custom definition with ANY match type matches either artist or duration`() {
        val customDef = SmartPlaylistDefinition(
            id = "custom_test",
            title = "Test Any",
            matchType = SmartRuleMatch.ANY,
            rules = listOf(
                SmartPlaylistRule(field = SmartRuleField.ARTIST, operator = SmartRuleOperator.EQUALS, value = "Neon Echo"),
                SmartPlaylistRule(field = SmartRuleField.DURATION_SECONDS, operator = SmartRuleOperator.LESS_THAN, value = "130")
            ),
            sortBy = SmartRuleSort.DEFAULT,
            limit = 10
        )

        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, customDef)

        // track2 matches Neon Echo, track4 matches duration < 130
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "t2" })
        assertTrue(result.any { it.id == "t4" })
    }

    @Test
    fun `evaluate custom definition with string CONTAINS and NOT_CONTAINS`() {
        val customDef = SmartPlaylistDefinition(
            id = "custom_contains",
            title = "Solar without Starlight",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(field = SmartRuleField.ARTIST, operator = SmartRuleOperator.CONTAINS, value = "Solar"),
                SmartPlaylistRule(field = SmartRuleField.TITLE, operator = SmartRuleOperator.NOT_CONTAINS, value = "Starlight")
            ),
            sortBy = SmartRuleSort.DEFAULT,
            limit = 10
        )

        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, customDef)
        assertEquals(1, result.size)
        assertEquals("t4", result[0].id)
    }

    @Test
    fun `evaluate respects limit`() {
        val customDef = SmartPlaylistDefinition(
            id = "custom_limit",
            title = "Limit Test",
            matchType = SmartRuleMatch.ALL,
            rules = emptyList(),
            sortBy = SmartRuleSort.DEFAULT,
            limit = 2
        )

        val result = SmartPlaylistEvaluator.evaluate(sampleTracks, playCounts, customDef)
        assertEquals(2, result.size)
    }

    @Test
    fun `serialize and deserialize roundtrip preserves all attributes and rules`() {
        val customPlaylist = SmartPlaylistDefinition(
            id = "p_custom_1",
            title = "Vocal Trance Beats",
            icon = "🔮",
            description = "High tempo vocal tracks",
            matchType = SmartRuleMatch.ALL,
            rules = listOf(
                SmartPlaylistRule(id = "r1", field = SmartRuleField.ARTIST, operator = SmartRuleOperator.STARTS_WITH, value = "Neo"),
                SmartPlaylistRule(id = "r2", field = SmartRuleField.PLAY_COUNT, operator = SmartRuleOperator.GREATER_THAN, value = "3")
            ),
            sortBy = SmartRuleSort.PLAY_COUNT_DESC,
            limit = 25,
            isBuiltIn = false
        )

        val json = SmartPlaylistEvaluator.serializePlaylists(listOf(customPlaylist))
        assertNotNull(json)
        assertTrue(json.contains("Vocal Trance Beats"))

        val restored = SmartPlaylistEvaluator.deserializePlaylists(json)
        assertEquals(1, restored.size)
        val r = restored[0]
        assertEquals("p_custom_1", r.id)
        assertEquals("Vocal Trance Beats", r.title)
        assertEquals("🔮", r.icon)
        assertEquals("High tempo vocal tracks", r.description)
        assertEquals(SmartRuleMatch.ALL, r.matchType)
        assertEquals(SmartRuleSort.PLAY_COUNT_DESC, r.sortBy)
        assertEquals(25, r.limit)
        assertFalse(r.isBuiltIn)
        assertEquals(2, r.rules.size)
        assertEquals(SmartRuleField.ARTIST, r.rules[0].field)
        assertEquals(SmartRuleOperator.STARTS_WITH, r.rules[0].operator)
        assertEquals("Neo", r.rules[0].value)
        assertEquals(SmartRuleField.PLAY_COUNT, r.rules[1].field)
        assertEquals(SmartRuleOperator.GREATER_THAN, r.rules[1].operator)
        assertEquals("3", r.rules[1].value)
    }
}

