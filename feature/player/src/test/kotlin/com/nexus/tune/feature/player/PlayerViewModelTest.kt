package com.nexus.tune.feature.player

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.model.LyricsLine
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.TrackLyrics
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    fun `loadLyrics restores saved lyrics offset from repository`() = runTest {
        val sampleTrack = Track(id = "t1", title = "Star", artist = "Moon", durationSeconds = 180)
        coEvery { musicRepository.getLyricsOffset("t1") } returns 250L

        viewModel.loadLyrics(sampleTrack)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(250L, viewModel.lyricsOffsetMs.value)
    }

    @Test
    fun `adjustLyricsOffset persists new offset to repository`() = runTest {
        val sampleTrack = Track(id = "t1", title = "Star", artist = "Moon", durationSeconds = 180)
        viewModel.loadLyrics(sampleTrack)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.adjustLyricsOffset(100L)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.saveLyricsOffset("t1", 100L) }
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

    @Test
    fun `visualizer mode and data update correctly`() {
        assertEquals(com.nexus.tune.core.model.VisualizerMode.SPECTRUM_BARS, viewModel.visualizerMode.value)
        viewModel.setVisualizerMode(com.nexus.tune.core.model.VisualizerMode.GLOW_PULSE)
        assertEquals(com.nexus.tune.core.model.VisualizerMode.GLOW_PULSE, viewModel.visualizerMode.value)

        val testData = com.nexus.tune.core.model.VisualizerData(amplitude = 0.85f)
        viewModel.updateVisualizerData(testData)
        assertEquals(0.85f, viewModel.visualizerData.value.amplitude, 0.001f)
    }

    @Test
    fun `abLoopManager methods delegate and update abLoopState`() {
        val mockAbLoop = mockk<com.nexus.tune.core.player.ABLoopManager>(relaxed = true)
        val loopFlow = MutableStateFlow(com.nexus.tune.core.model.ABLoopState(isEnabled = true, startMs = 1000L, endMs = 5000L, loopCount = 2))
        every { mockAbLoop.loopState } returns loopFlow

        val vm = PlayerViewModel(
            musicRepository = mockk(relaxed = true),
            manageQueueUseCase = mockk(relaxed = true),
            abLoopManager = mockAbLoop
        )

        assertEquals(1000L, vm.abLoopState.value.startMs)
        assertEquals(5000L, vm.abLoopState.value.endMs)
        assertEquals(2, vm.abLoopState.value.loopCount)

        vm.setABLoopPointA(2000L)
        verify { mockAbLoop.setPointA(2000L) }

        vm.setABLoopPointB(6000L)
        verify { mockAbLoop.setPointB(6000L) }

        vm.toggleABLoop(false)
        verify { mockAbLoop.toggleLoop(false) }

        vm.clearABLoop()
        verify { mockAbLoop.clearLoop() }
    }

    @Test
    fun `pitch and semitones controls update state and delegate to playerController`() {
        val mockPlayerController = mockk<com.nexus.tune.core.player.PlayerController>(relaxed = true)
        val vm = PlayerViewModel(
            musicRepository = mockk(relaxed = true),
            manageQueueUseCase = mockk(relaxed = true),
            playerController = mockPlayerController
        )

        assertEquals(0, vm.playbackPitchSemitones.value)
        assertEquals(1.0f, vm.playbackPitchRatio.value, 0.001f)

        vm.setPlaybackSemitones(2)
        assertEquals(2, vm.playbackPitchSemitones.value)
        assertTrue(vm.playbackPitchRatio.value > 1.12f)
        verify { mockPlayerController.setPlaybackSemitones(2) }

        vm.transposeUp()
        assertEquals(3, vm.playbackPitchSemitones.value)
        verify { mockPlayerController.setPlaybackSemitones(3) }

        vm.transposeDown()
        assertEquals(2, vm.playbackPitchSemitones.value)

        vm.resetPitch()
        assertEquals(0, vm.playbackPitchSemitones.value)
        assertEquals(1.0f, vm.playbackPitchRatio.value, 0.001f)
        verify { mockPlayerController.setPlaybackSemitones(0) }
    }

    @Test
    fun `shuffleQueue delegates to manageQueueUseCase`() = runTest {
        viewModel.shuffleQueue(smartDiversity = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { manageQueueUseCase.shuffleQueue(smartDiversity = true) }
    }

    @Test
    fun `importCustomLrc delegates to musicRepository and updates lyrics`() = runTest {
        val track = Track(id = "trk1", title = "Title", artist = "Artist")
        val lyrics = com.nexus.tune.core.model.TrackLyrics(trackId = "trk1", lines = listOf(LyricsLine(1000L, "Hello")), isSynced = true)
        coEvery { musicRepository.saveCustomLyrics("trk1", "Title", "Artist", any()) } returns MusicResult.Success(lyrics)

        viewModel.importCustomLrc(track, "[00:01.00] Hello")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.saveCustomLyrics("trk1", "Title", "Artist", "[00:01.00] Hello") }
        assertEquals(lyrics, viewModel.lyrics.value)
    }

    @Test
    fun `exportCurrentLyrics delegates to musicRepository`() = runTest {
        val track = Track(id = "trk1", title = "Title", artist = "Artist")
        coEvery { musicRepository.exportLyricsToLrc("trk1") } returns "[00:01.00] Hello"

        val exported = viewModel.exportCurrentLyrics(track)
        assertEquals("[00:01.00] Hello", exported)
        coVerify { musicRepository.exportLyricsToLrc("trk1") }
    }
}

