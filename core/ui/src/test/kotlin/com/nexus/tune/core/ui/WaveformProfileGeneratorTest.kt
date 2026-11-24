package com.nexus.tune.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveformProfileGeneratorTest {

    @Test
    fun `testGenerateProfileSizeAndBounds`() {
        val profile = WaveformProfileGenerator.generateProfile("track_123", 48)
        assertEquals(48, profile.size)
        for (amplitude in profile) {
            assertTrue("Amplitude $amplitude should be >= 0.18f", amplitude >= 0.18f)
            assertTrue("Amplitude $amplitude should be <= 1.0f", amplitude <= 1.0f)
        }
    }

    @Test
    fun `testGenerateProfileDeterminism`() {
        val profile1 = WaveformProfileGenerator.generateProfile("song_abc", 32)
        val profile2 = WaveformProfileGenerator.generateProfile("song_abc", 32)
        assertEquals(profile1, profile2)

        val profileDifferentTrack = WaveformProfileGenerator.generateProfile("song_xyz", 32)
        assertTrue(profile1 != profileDifferentTrack)
    }
}
