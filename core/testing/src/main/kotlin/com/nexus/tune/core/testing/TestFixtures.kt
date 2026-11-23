package com.nexus.tune.core.testing

import com.nexus.tune.core.model.Track

object TestFixtures {
    val sampleTrack1 = Track(
        id = "test_1",
        title = "Neon Dreams",
        artist = "Synth Wave",
        album = "Future Retro",
        durationSeconds = 240,
        artworkUrl = "https://example.com/art1.jpg"
    )

    val sampleTrack2 = Track(
        id = "test_2",
        title = "Cyber Velocity",
        artist = "Laser Grid",
        album = "Subsystem",
        durationSeconds = 180,
        artworkUrl = "https://example.com/art2.jpg"
    )

    val sampleTracks = listOf(sampleTrack1, sampleTrack2)
}

