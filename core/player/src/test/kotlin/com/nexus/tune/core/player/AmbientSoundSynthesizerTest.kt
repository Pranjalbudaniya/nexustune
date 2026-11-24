package com.nexus.tune.core.player

import com.nexus.tune.core.model.AmbientSoundType
import com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AmbientSoundSynthesizerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var synthesizer: AmbientSoundSynthesizer

    @Before
    fun setUp() {
        synthesizer = AmbientSoundSynthesizer(coroutineScope = testScope)
    }

    @Test
    fun testInitialState() {
        val state = synthesizer.state.value
        assertFalse(state.isPlaying)
        assertEquals(AmbientSoundType.OFF, state.soundType)
        assertEquals(0.5f, state.volume, 0.001f)
    }

    @Test
    fun testStartAndStop() = testScope.runTest {
        synthesizer.start(AmbientSoundType.BROWN_NOISE, 0.7f)
        var state = synthesizer.state.value
        assertTrue(state.isPlaying)
        assertEquals(AmbientSoundType.BROWN_NOISE, state.soundType)
        assertEquals(0.7f, state.volume, 0.001f)

        synthesizer.stop()
        state = synthesizer.state.value
        assertFalse(state.isPlaying)
    }

    @Test
    fun testSetVolumeClamping() {
        synthesizer.setVolume(1.5f)
        assertEquals(1.0f, synthesizer.state.value.volume, 0.001f)

        synthesizer.setVolume(-0.2f)
        assertEquals(0.0f, synthesizer.state.value.volume, 0.001f)

        synthesizer.setVolume(0.42f)
        assertEquals(0.42f, synthesizer.state.value.volume, 0.001f)
    }

    @Test
    fun testSetSoundType() {
        synthesizer.setSoundType(AmbientSoundType.PINK_NOISE)
        assertEquals(AmbientSoundType.PINK_NOISE, synthesizer.state.value.soundType)

        synthesizer.setSoundType(AmbientSoundType.BINAURAL_THETA)
        assertEquals(AmbientSoundType.BINAURAL_THETA, synthesizer.state.value.soundType)
    }

    @Test
    fun testTogglePlay() {
        assertFalse(synthesizer.state.value.isPlaying)
        synthesizer.togglePlay(AmbientSoundType.WHITE_NOISE)
        assertTrue(synthesizer.state.value.isPlaying)
        assertEquals(AmbientSoundType.WHITE_NOISE, synthesizer.state.value.soundType)

        synthesizer.togglePlay()
        assertFalse(synthesizer.state.value.isPlaying)
    }
}

