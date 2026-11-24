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
        verify(atLeast = 1) { playerController.setVolume(1.0f) }
    }

    @Test
    fun `fade-out progressively reduces volume during final window`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)

        sleepTimerManager.startTimer(1, fadeDurationSec = 30) // 60s total, fade starts at 30s
        assertEquals(60L, sleepTimerManager.timerState.value.remainingSeconds)
        assertFalse(sleepTimerManager.timerState.value.isFadingOut)

        // Advance 35 seconds (inside fade window of 30s)
        advanceTimeBy(35_000L)
        val fadingState = sleepTimerManager.timerState.value
        assertTrue(fadingState.remainingSeconds <= 30L)
        assertTrue(fadingState.isFadingOut)
        assertTrue(fadingState.fadeVolumeMultiplier < 1.0f)
        assertTrue(fadingState.fadeVolumeMultiplier > 0.0f)

        verify(atLeast = 1) { playerController.setVolume(match { it < 1.0f }) }

        // Cancel restores volume to 1.0f
        sleepTimerManager.cancelTimer()
        assertFalse(sleepTimerManager.timerState.value.isFadingOut)
        verify(atLeast = 1) { playerController.setVolume(1.0f) }
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

    @Test
    fun `extendTimer adds extra seconds and restores volume when fading`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)

        sleepTimerManager.startTimer(1, fadeDurationSec = 30)
        advanceTimeBy(40_000L) // Remaining is inside fade window
        assertTrue(sleepTimerManager.timerState.value.isFadingOut)
        val remainingBefore = sleepTimerManager.timerState.value.remainingSeconds

        // Extend by 10 minutes (600s)
        sleepTimerManager.extendTimer(10)

        val extendedState = sleepTimerManager.timerState.value
        assertEquals(remainingBefore + 600L, extendedState.remainingSeconds)
        assertFalse(extendedState.isFadingOut)
        assertEquals(1.0f, extendedState.fadeVolumeMultiplier, 0.001f)
        verify(atLeast = 1) { playerController.setVolume(1.0f) }
    }

    @Test
    fun `setShakeToExtend updates configuration in timerState`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)
        assertFalse(sleepTimerManager.timerState.value.shakeToExtendEnabled)

        sleepTimerManager.setShakeToExtend(true, 15)
        val state = sleepTimerManager.timerState.value
        assertTrue(state.shakeToExtendEnabled)
        assertEquals(15, state.extensionMinutes)
    }

    @Test
    fun `setBedtimeHandoff updates handoff configuration`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)
        assertFalse(sleepTimerManager.timerState.value.bedtimeHandoffEnabled)

        sleepTimerManager.setBedtimeHandoff(
            enabled = true,
            soundType = com.nexus.tune.core.model.AmbientSoundType.BINAURAL_DELTA,
            volume = 0.4f,
            durationMinutes = 90
        )

        val state = sleepTimerManager.timerState.value
        assertTrue(state.bedtimeHandoffEnabled)
        assertEquals(com.nexus.tune.core.model.AmbientSoundType.BINAURAL_DELTA, state.handoffSoundType)
        assertEquals(0.4f, state.handoffVolume, 0.001f)
        assertEquals(90, state.handoffDurationMinutes)
    }

    @Test
    fun `setSleepDriftProfile updates drift profile and advances drift progress during session`() = runTest {
        val sleepTimerManager = SleepTimerManager(playerController, this)
        assertEquals(com.nexus.tune.core.model.SleepDriftProfile.OFF, sleepTimerManager.timerState.value.driftProfile)

        sleepTimerManager.setSleepDriftProfile(com.nexus.tune.core.model.SleepDriftProfile.DEEP_DRIFT)
        assertEquals(com.nexus.tune.core.model.SleepDriftProfile.DEEP_DRIFT, sleepTimerManager.timerState.value.driftProfile)

        sleepTimerManager.startTimer(1) // 60 seconds
        advanceTimeBy(30_000L) // 30 seconds elapsed -> 50% drift progress

        val midState = sleepTimerManager.timerState.value
        assertTrue(midState.driftProgress >= 0.45f)

        sleepTimerManager.cancelTimer()
        assertEquals(0f, sleepTimerManager.timerState.value.driftProgress)
    }
}
