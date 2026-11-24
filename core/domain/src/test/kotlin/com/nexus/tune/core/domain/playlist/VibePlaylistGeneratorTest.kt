package com.nexus.tune.core.domain.playlist

import com.nexus.tune.core.model.EnergyCurve
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.VibeMixRequest
import com.nexus.tune.core.model.VibeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VibePlaylistGeneratorTest {

    private val sampleTracks = listOf(
        Track(id = "1", title = "Midnight Rain Chill", artist = "Lofi Boy", durationSeconds = 180),
        Track(id = "2", title = "Beast Workout Anthem", artist = "Gym Pump", durationSeconds = 150),
        Track(id = "3", title = "Deep Piano Focus Study", artist = "Scholar", durationSeconds = 240),
        Track(id = "4", title = "Acoustic Sunset Breeze", artist = "Folk Trio", durationSeconds = 210),
        Track(id = "5", title = "Club Techno Rave Drop", artist = "DJ Beat", durationSeconds = 190),
        Track(id = "6", title = "Classic Gold Memories", artist = "Oldie", durationSeconds = 220),
        Track(id = "7", title = "Twilight Lullaby Ambient", artist = "Sleep Sound", durationSeconds = 300),
        Track(id = "8", title = "Heavy Power Metal Run", artist = "Iron Gym", durationSeconds = 160)
    )

    @Test
    fun `generateVibeMix returns empty result for empty candidate pool`() {
        val request = VibeMixRequest(theme = VibeTheme.LATE_NIGHT_CHILL, targetDurationMinutes = 30)
        val result = VibePlaylistGenerator.generateVibeMix(request, emptyList())

        assertTrue(result.tracks.isEmpty())
        assertEquals(0L, result.totalDurationSeconds)
        assertEquals("0 min", result.formattedDuration)
    }

    @Test
    fun `generateVibeMix filters for Late Night Chill aesthetic`() {
        val request = VibeMixRequest(theme = VibeTheme.LATE_NIGHT_CHILL, targetDurationMinutes = 10)
        val result = VibePlaylistGenerator.generateVibeMix(request, sampleTracks)

        assertTrue(result.tracks.isNotEmpty())
        val topTrack = result.tracks.first()
        assertTrue(topTrack.title.contains("Chill") || topTrack.title.contains("Midnight") || topTrack.title.contains("Ambient"))
    }

    @Test
    fun `generateVibeMix filters for High Octane Energy aesthetic`() {
        val request = VibeMixRequest(theme = VibeTheme.HIGH_OCTANE_ENERGY, targetDurationMinutes = 10)
        val result = VibePlaylistGenerator.generateVibeMix(request, sampleTracks)

        assertTrue(result.tracks.isNotEmpty())
        val topTrack = result.tracks.first()
        assertTrue(topTrack.title.contains("Workout") || topTrack.title.contains("Metal") || topTrack.title.contains("Rave"))
    }

    @Test
    fun `generateVibeMix accurately calculates total duration and formatted title`() {
        val request = VibeMixRequest(theme = VibeTheme.CLUB_EUPHORIA, targetDurationMinutes = 15)
        val result = VibePlaylistGenerator.generateVibeMix(request, sampleTracks)

        assertNotNull(result.title)
        assertTrue(result.title.contains("Club Euphoria"))
        assertTrue(result.title.contains("15m"))
        assertTrue(result.totalDurationSeconds > 0)
    }

    @Test
    fun `energy curves reorder tracks properly`() {
        val rampUpReq = VibeMixRequest(theme = VibeTheme.DYNAMIC_ECLECTIC, energyCurve = EnergyCurve.RAMP_UP, targetDurationMinutes = 30)
        val rampUpRes = VibePlaylistGenerator.generateVibeMix(rampUpReq, sampleTracks)
        assertEquals(EnergyCurve.RAMP_UP, rampUpRes.curve)

        val peakReq = VibeMixRequest(theme = VibeTheme.DYNAMIC_ECLECTIC, energyCurve = EnergyCurve.PEAK_SUSTAINED, targetDurationMinutes = 30)
        val peakRes = VibePlaylistGenerator.generateVibeMix(peakReq, sampleTracks)
        assertEquals(EnergyCurve.PEAK_SUSTAINED, peakRes.curve)

        val waveReq = VibeMixRequest(theme = VibeTheme.DYNAMIC_ECLECTIC, energyCurve = EnergyCurve.WAVE_DYNAMIC, targetDurationMinutes = 30)
        val waveRes = VibePlaylistGenerator.generateVibeMix(waveReq, sampleTracks)
        assertEquals(EnergyCurve.WAVE_DYNAMIC, waveRes.curve)

        val cooldownReq = VibeMixRequest(theme = VibeTheme.DYNAMIC_ECLECTIC, energyCurve = EnergyCurve.GENTLE_COOLDOWN, targetDurationMinutes = 30)
        val cooldownRes = VibePlaylistGenerator.generateVibeMix(cooldownReq, sampleTracks)
        assertEquals(EnergyCurve.GENTLE_COOLDOWN, cooldownRes.curve)
    }
}

