package com.nexus.tune.core.player

import com.nexus.tune.core.model.VisualizerMode
import com.nexus.tune.core.player.visualizer.VisualizerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VisualizerManagerTest {

    @Test
    fun testModeSwitchingAndZeroingWhenOff() = runTest {
        val manager = VisualizerManager.instance

        manager.setMode(VisualizerMode.SPECTRUM_BARS)
        manager.updatePlayback(true)

        manager.setMode(VisualizerMode.OFF)
        val data = manager.visualizerData.value
        assertEquals(0f, data.amplitude, 0.001f)
        assertTrue(data.magnitudes.all { it == 0f })
    }

    @Test
    fun testAudioSessionSafeHandling() = runTest {
        val manager = VisualizerManager.instance

        // Session 0 should be ignored safely
        manager.attachToAudioSession(0)

        // Invalid session or unmocked audio effect should gracefully not crash
        manager.attachToAudioSession(-1)

        manager.releaseNativeVisualizer()
    }

    @Test
    fun testReleaseLifecycle() = runTest {
        val manager = VisualizerManager.instance
        manager.setMode(VisualizerMode.CIRCULAR_WAVE)
        manager.updatePlayback(false)
        manager.release()

        val data = manager.visualizerData.value
        assertEquals(0f, data.amplitude, 0.001f)
    }
}
