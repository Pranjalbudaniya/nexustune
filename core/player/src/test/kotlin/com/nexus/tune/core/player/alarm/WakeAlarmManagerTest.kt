package com.nexus.tune.core.player.alarm

import android.content.Context
import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.WakeAlarmConfig
import com.nexus.tune.core.model.WakeAlarmSource
import com.nexus.tune.core.player.PlayerController
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WakeAlarmManagerTest {

    private val context = mockk<Context>(relaxed = true)
    private val playerController = mockk<PlayerController>(relaxed = true)

    @Before
    fun setUp() {
        every { playerController.playbackState } returns MutableStateFlow(PlaybackState())
    }

    @Test
    fun `calculateNextTriggerMillis schedules for today when target is in the future`() {
        val testDispatcher = StandardTestDispatcher()
        val manager = WakeAlarmManager(context, playerController, coroutineScope = TestScope(testDispatcher))

        // Set base time to 06:00:00
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        // Target: 07:30
        val target = manager.calculateNextTriggerMillis(7, 30, now)
        val targetCal = Calendar.getInstance().apply { timeInMillis = target }

        assertEquals(cal.get(Calendar.DAY_OF_YEAR), targetCal.get(Calendar.DAY_OF_YEAR))
        assertEquals(7, targetCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, targetCal.get(Calendar.MINUTE))
    }

    @Test
    fun `calculateNextTriggerMillis schedules for tomorrow when target is in the past`() {
        val testDispatcher = StandardTestDispatcher()
        val manager = WakeAlarmManager(context, playerController, coroutineScope = TestScope(testDispatcher))

        // Set base time to 08:00:00
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        // Target: 07:00 (already passed today)
        val target = manager.calculateNextTriggerMillis(7, 0, now)
        val targetCal = Calendar.getInstance().apply { timeInMillis = target }

        assertTrue(target > now)
        assertEquals(7, targetCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, targetCal.get(Calendar.MINUTE))
    }

    @Test
    fun `startFadeIn progresses volume gradually to target`() = runTest {
        val testDispatcher = StandardTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val manager = WakeAlarmManager(context, playerController, coroutineScope = testScope)

        manager.startFadeIn(durationSeconds = 2, targetVolume = 0.8f)
        // totalSteps = 4 (every 500ms over 2 seconds)
        assertEquals(0.0f, manager.currentFadedVolume.value, 0.01f)

        testDispatcher.scheduler.advanceTimeBy(1000L)
        assertTrue(manager.currentFadedVolume.value > 0.0f)

        testDispatcher.scheduler.advanceTimeBy(1500L)
        assertEquals(0.8f, manager.currentFadedVolume.value, 0.01f)
        verify { playerController.setVolume(0.8f) }
    }

    @Test
    fun `stopAlarm resets active state and restores normal volume`() {
        val testDispatcher = StandardTestDispatcher()
        val manager = WakeAlarmManager(context, playerController, coroutineScope = TestScope(testDispatcher))

        manager.triggerWakeAlarm(WakeAlarmConfig(isEnabled = true, fadeInSeconds = 10, targetVolume = 0.8f))
        assertTrue(manager.isAlarmActive.value)

        manager.stopAlarm()
        assertFalse(manager.isAlarmActive.value)
        verify { playerController.setVolume(1.0f) }
    }
}
