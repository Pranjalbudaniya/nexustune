package com.nexus.tune.core.domain.mood

import com.nexus.tune.core.model.MoodCategory
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import org.junit.Assert.*
import org.junit.Test

class MoodRadioEngineTest {

    private val focusTrack = Track(id = "1", title = "Lofi Study Beats", artist = "ChilledCow")
    private val workoutTrack = Track(id = "2", title = "Gym Power Trap", artist = "Heavy Bass")
    private val chillTrack = Track(id = "3", title = "Sunset Acoustic Breeze", artist = "Relaxing Waves")
    private val sleepTrack = Track(id = "4", title = "Deep Delta Sleep Meditation", artist = "Night Dreams")
    private val genericTrack = Track(id = "5", title = "Standard Tune", artist = "Artist")

    @Test
    fun `matchesMood returns true for ALL`() {
        assertTrue(MoodRadioEngine.matchesMood(focusTrack, MoodCategory.ALL))
        assertTrue(MoodRadioEngine.matchesMood(genericTrack, MoodCategory.ALL))
    }

    @Test
    fun `matchesMood identifies relevant mood keywords`() {
        assertTrue(MoodRadioEngine.matchesMood(focusTrack, MoodCategory.FOCUS))
        assertFalse(MoodRadioEngine.matchesMood(workoutTrack, MoodCategory.FOCUS))

        assertTrue(MoodRadioEngine.matchesMood(workoutTrack, MoodCategory.WORKOUT))
        assertFalse(MoodRadioEngine.matchesMood(chillTrack, MoodCategory.WORKOUT))

        assertTrue(MoodRadioEngine.matchesMood(chillTrack, MoodCategory.CHILL))
        assertTrue(MoodRadioEngine.matchesMood(sleepTrack, MoodCategory.SLEEP))
    }

    @Test
    fun `filterPlaylistsByMood filters matching playlists and tracks`() {
        val playlist1 = Playlist(
            id = "p1",
            title = "Focus and Coding",
            tracks = listOf(focusTrack, genericTrack)
        )
        val playlist2 = Playlist(
            id = "p2",
            title = "Beast Mode Gym",
            tracks = listOf(workoutTrack)
        )

        val filteredFocus = MoodRadioEngine.filterPlaylistsByMood(listOf(playlist1, playlist2), MoodCategory.FOCUS)
        assertEquals(1, filteredFocus.size)
        assertEquals("p1", filteredFocus[0].id)
        assertEquals(1, filteredFocus[0].tracks.size)
        assertEquals("1", filteredFocus[0].tracks[0].id)

        val filteredWorkout = MoodRadioEngine.filterPlaylistsByMood(listOf(playlist1, playlist2), MoodCategory.WORKOUT)
        assertEquals(1, filteredWorkout.size)
        assertEquals("p2", filteredWorkout[0].id)
    }

    @Test
    fun `createMoodRadio returns requested count prioritizing matching tracks`() {
        val pool = listOf(focusTrack, workoutTrack, chillTrack, sleepTrack, genericTrack)

        val radio = MoodRadioEngine.createMoodRadio(pool, MoodCategory.FOCUS, count = 3)
        assertEquals(3, radio.size)
        assertTrue(radio.contains(focusTrack))
    }
}

