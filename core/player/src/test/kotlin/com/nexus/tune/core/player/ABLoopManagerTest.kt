package com.nexus.tune.core.player

import com.nexus.tune.core.model.PlaybackState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ABLoopManagerTest {

    private val playerController = mockk<PlayerController>(relaxed = true)
    private val playbackStateFlow = MutableStateFlow(PlaybackState())
    private var manager: ABLoopManager? = null

    @Before
    fun setUp() {
        every { playerController.playbackState } returns playbackStateFlow
    }

    @org.junit.After
    fun tearDown() {
        manager?.release()
        manager = null
    }

    @Test
    fun `testInitialState`() = runTest {
        val mgr = ABLoopManager(playerController, this).also { manager = it }
        val state = mgr.loopState.value
        assertFalse(state.isEnabled)
        assertNull(state.startMs)
        assertNull(state.endMs)
        assertEquals(0, state.loopCount)
        mgr.release()
    }

    @Test
    fun `testSetPointAAndPointB`() = runTest {
        val mgr = ABLoopManager(playerController, this).also { manager = it }

        mgr.setPointA(15_000L)
        var state = mgr.loopState.value
        assertEquals(15_000L, state.startMs)
        assertNull(state.endMs)
        assertFalse(state.isEnabled)

        mgr.setPointB(45_000L)
        state = mgr.loopState.value
        assertEquals(15_000L, state.startMs)
        assertEquals(45_000L, state.endMs)
        assertTrue(state.isEnabled)
        mgr.release()
    }

    @Test
    fun `testSetPointBInvalidIgnored`() = runTest {
        val mgr = ABLoopManager(playerController, this).also { manager = it }

        mgr.setPointA(30_000L)
        mgr.setPointB(20_000L) // Invalid since 20s < 30s
        val state = mgr.loopState.value
        assertEquals(30_000L, state.startMs)
        assertNull(state.endMs)
        assertFalse(state.isEnabled)
        mgr.release()
    }

    @Test
    fun `testPlaybackAutoSeekOnLoopBoundary`() = runTest {
        val mgr = ABLoopManager(playerController, this).also { manager = it }

        mgr.setPointA(10_000L)
        mgr.setPointB(25_000L)

        // Advance playback to 20s (inside loop)
        playbackStateFlow.value = PlaybackState(positionMs = 20_000L)
        advanceUntilIdle()
        verify(exactly = 0) { playerController.seekTo(any()) }

        // Advance playback to 25s (reaches loop boundary)
        playbackStateFlow.value = PlaybackState(positionMs = 25_100L)
        advanceUntilIdle()

        verify(exactly = 1) { playerController.seekTo(10_000L) }
        assertEquals(1, mgr.loopState.value.loopCount)

        // Advance playback again to loop boundary
        playbackStateFlow.value = PlaybackState(positionMs = 25_200L)
        advanceUntilIdle()

        verify(exactly = 2) { playerController.seekTo(10_000L) }
        assertEquals(2, mgr.loopState.value.loopCount)
        mgr.release()
    }

    @Test
    fun `testToggleAndClear`() = runTest {
        val mgr = ABLoopManager(playerController, this).also { manager = it }

        mgr.setPointA(5_000L)
        mgr.setPointB(15_000L)
        assertTrue(mgr.loopState.value.isEnabled)

        mgr.toggleLoop(false)
        assertFalse(mgr.loopState.value.isEnabled)

        mgr.toggleLoop(true)
        assertTrue(mgr.loopState.value.isEnabled)

        mgr.clearLoop()
        assertFalse(mgr.loopState.value.isEnabled)
        assertNull(mgr.loopState.value.startMs)
        assertNull(mgr.loopState.value.endMs)
        mgr.release()
    }
}

