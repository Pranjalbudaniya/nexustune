package com.nexus.tune.core.player

import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.SkipSegment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SkipSegmentManagerTest {

    private val playerController = mockk<PlayerController>(relaxed = true)
    private val playbackStateFlow = MutableStateFlow(PlaybackState())

    @Before
    fun setUp() {
        every { playerController.playbackState } returns playbackStateFlow
    }

    @Test
    fun `setEnabled updates enabled state`() {
        val manager = SkipSegmentManager(playerController)
        assertTrue(manager.enabled.value)
        manager.setEnabled(false)
        assertFalse(manager.enabled.value)
    }

    @Test
    fun `setSegments seeks over matching segment`() = runTest {
        val manager = SkipSegmentManager(playerController, this)
        val segment = SkipSegment(category = "intro", startMs = 0L, endMs = 15000L)
        manager.setSegments(listOf(segment))

        // Position inside segment triggers seek to end
        playbackStateFlow.value = PlaybackState(positionMs = 5000L)
        advanceUntilIdle()

        verify { playerController.seekTo(15000L) }
        manager.clearSegments()
    }

    @Test
    fun `disabled skip segment manager does not seek`() = runTest {
        val manager = SkipSegmentManager(playerController, this)
        manager.setEnabled(false)
        val segment = SkipSegment(category = "intro", startMs = 0L, endMs = 15000L)
        manager.setSegments(listOf(segment))

        playbackStateFlow.value = PlaybackState(positionMs = 5000L)
        advanceUntilIdle()

        verify(exactly = 0) { playerController.seekTo(any()) }
        manager.clearSegments()
    }
}

