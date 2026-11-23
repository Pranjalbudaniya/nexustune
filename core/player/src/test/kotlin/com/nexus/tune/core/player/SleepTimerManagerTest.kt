package com.nexus.tune.core.player

import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.PlaybackStatus
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerManagerTest {

    private val playerController = mockk<PlayerController>(relaxed = true)
    private val playbackStateFlow = MutableStateFlow(PlaybackState())

    @Before
    fun setUp() {
        every { playerController.playbackState } returns playbackStateFlow
    }

    @Test
    fun `startTimer sets state and triggers play pause when time expires`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)

        sleepTimerManager.startTimer(1) // 1 minute = 60s
        val initialState = sleepTimerManager.timerState.value
        assertTrue(initialState.isActive)
        assertEquals(60L, initialState.remainingSeconds)
        assertFalse(initialState.isEndOfTrack)

        // Advance until idle (all 60s elapse)
        advanceUntilIdle()
        assertFalse(sleepTimerManager.timerState.value.isActive)
        assertEquals(0L, sleepTimerManager.timerState.value.remainingSeconds)

        verify(atLeast = 1) { playerController.togglePlayPause() }
    }

    @Test
    fun `cancelTimer resets active state immediately`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)

        sleepTimerManager.startTimer(15)
        assertTrue(sleepTimerManager.timerState.value.isActive)

        sleepTimerManager.cancelTimer()
        assertFalse(sleepTimerManager.timerState.value.isActive)
        assertEquals(0L, sleepTimerManager.timerState.value.remainingSeconds)
    }

    @Test
    fun `startEndOfTrackTimer pauses when track ends`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)

        sleepTimerManager.startEndOfTrackTimer()
        val state = sleepTimerManager.timerState.value
        assertTrue(state.isActive)
        assertTrue(state.isEndOfTrack)

        // Simulate track ended
        playbackStateFlow.value = PlaybackState(status = PlaybackStatus.ENDED)
        advanceUntilIdle()

        assertFalse(sleepTimerManager.timerState.value.isActive)
        verify(atLeast = 1) { playerController.togglePlayPause() }
    }
}
