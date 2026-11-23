package com.nexus.tune.feature.player

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.TrackLyrics
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val musicRepository = mockk<MusicRepository>(relaxed = true)
    private val manageQueueUseCase = mockk<ManageQueueUseCase>(relaxed = true)

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayerViewModel(musicRepository, manageQueueUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadLyrics updates lyrics state successfully`() = runTest {
        val sampleTrack = Track(
            id = "t1",
            title = "Star",
            artist = "Moon",
            durationSeconds = 180
        )
        val sampleLyrics = TrackLyrics(
            trackId = "t1",
            lines = listOf(LyricsLine(0L, "Hello world")),
            isSynced = true
        )

        coEvery {
            musicRepository.getLyrics("t1", "Star", "Moon", 180)
        } returns MusicResult.Success(sampleLyrics)

        viewModel.loadLyrics(sampleTrack)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.lyrics.value)
        assertEquals(1, viewModel.lyrics.value?.lines?.size)
        assertEquals("Hello world", viewModel.lyrics.value?.lines?.first()?.text)
    }

    @Test
    fun `toggleLike delegates to repository`() = runTest {
        val sampleTrack = Track(id = "t1", title = "Star", artist = "Moon")
        viewModel.toggleLike(sampleTrack)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.toggleLike(sampleTrack) }
    }

    @Test
    fun `adjustLyricsOffset increments and resetLyricsOffset clears offset`() {
        assertEquals(0L, viewModel.lyricsOffsetMs.value)
        viewModel.adjustLyricsOffset(150L)
        assertEquals(150L, viewModel.lyricsOffsetMs.value)
        viewModel.adjustLyricsOffset(-50L)
        assertEquals(100L, viewModel.lyricsOffsetMs.value)
        viewModel.resetLyricsOffset()
        assertEquals(0L, viewModel.lyricsOffsetMs.value)
    }

    @Test
    fun `setPlaybackSpeed updates speed state flow`() {
        assertEquals(1.0f, viewModel.playbackSpeed.value)
        viewModel.setPlaybackSpeed(1.5f)
        assertEquals(1.5f, viewModel.playbackSpeed.value)
        viewModel.setPlaybackSpeed(0.75f)
        assertEquals(0.75f, viewModel.playbackSpeed.value)
    }

    @Test
    fun `updateAudioOutputDevice updates state flow`() {
        val device = com.nexus.tune.core.model.AudioOutputDevice(
            type = com.nexus.tune.core.model.AudioDeviceType.USB_DAC,
            name = "AudioQuest Dragonfly",
            isHighRes = true
        )
        viewModel.updateAudioOutputDevice(device)
        assertEquals("AudioQuest Dragonfly", viewModel.audioOutputDevice.value.name)
        assertEquals(true, viewModel.audioOutputDevice.value.isHighRes)
    }
}

