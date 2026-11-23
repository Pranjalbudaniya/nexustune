package com.nexus.tune.core.player

import org.junit.Assert.assertEquals
import org.junit.Test

class NexusPlaybackServiceTest {

    @Test
    fun `library root constants are defined correctly for automotive navigation`() {
        assertEquals("root", NexusPlaybackService.ROOT_ID)
        assertEquals("liked_tracks", NexusPlaybackService.LIKED_ID)
        assertEquals("history", NexusPlaybackService.HISTORY_ID)
        assertEquals("playlists", NexusPlaybackService.PLAYLISTS_ID)
    }
}
