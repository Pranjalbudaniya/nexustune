package com.nexus.tune.core.domain

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.preload.AudioPreloader
import com.nexus.tune.core.domain.preload.GaplessPreloadManager
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.AudioStream
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GaplessPreloadManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val audioPreloader = mockk<AudioPreloader>(relaxed = true)
    private val queueRepository = mockk<QueueRepository>(relaxed = true)
    private val resolveStreamUseCase = mockk<ResolveStreamUseCase>(relaxed = true)

    private lateinit var preloadManager: GaplessPreloadManager

    private val track1 = Track(id = "track_1", title = "Track 1", artist = "Artist 1")
    private val track2 = Track(id = "track_2", title = "Track 2", artist = "Artist 2")
    private val track3 = Track(id = "track_3", title = "Track 3", artist = "Artist 3")

    @Before
    fun setUp() {
        preloadManager = GaplessPreloadManager(
            audioPreloader = audioPreloader,
            queueRepository = queueRepository,
            resolveStreamUseCase = resolveStreamUseCase,
            coroutineScope = testScope
        )

        every { queueRepository.getQueue() } returns flowOf(listOf(track1, track2, track3))
        every { queueRepository.getQueueIndex() } returns flowOf(0)
    }

    @Test
    fun `checkAndPreload does nothing when disabled`() = runTest {
        preloadManager.checkAndPreload(
            currentTrack = track1,
            positionMs = 178_000L,
            durationMs = 180_000L,
            enabled = false,
            thresholdSeconds = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(preloadManager.preloadedTrackId.value)
        verify(exactly = 0) { audioPreloader.preloadTrack(any(), any()) }
    }

    @Test
    fun `checkAndPreload does nothing when remaining time is greater than threshold`() = runTest {
        preloadManager.checkAndPreload(
            currentTrack = track1,
            positionMs = 100_000L,
            durationMs = 180_000L,
            enabled = true,
            thresholdSeconds = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(preloadManager.preloadedTrackId.value)
        verify(exactly = 0) { audioPreloader.preloadTrack(any(), any()) }
    }

    @Test
    fun `checkAndPreload preloads next track when within threshold`() = runTest {
        coEvery { resolveStreamUseCase("track_2") } returns MusicResult.Success(
            AudioStream(url = "https://audio.stream/track2.opus")
        )

        preloadManager.checkAndPreload(
            currentTrack = track1,
            positionMs = 177_000L,
            durationMs = 180_000L,
            enabled = true,
            thresholdSeconds = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("track_2", preloadManager.preloadedTrackId.value)
        verify { audioPreloader.preloadTrack(track2, "https://audio.stream/track2.opus") }
    }

    @Test
    fun `clear resets state and delegates to preloader`() = runTest {
        coEvery { resolveStreamUseCase("track_2") } returns MusicResult.Success(
            AudioStream(url = "https://audio.stream/track2.opus")
        )

        preloadManager.checkAndPreload(
            currentTrack = track1,
            positionMs = 177_000L,
            durationMs = 180_000L,
            enabled = true,
            thresholdSeconds = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("track_2", preloadManager.preloadedTrackId.value)

        preloadManager.clear()
        assertNull(preloadManager.preloadedTrackId.value)
        verify { audioPreloader.clearPreload() }
    }

    @Test
    fun `onTrackChanged resets preloaded ID when matching track starts`() = runTest {
        coEvery { resolveStreamUseCase("track_2") } returns MusicResult.Success(
            AudioStream(url = "https://audio.stream/track2.opus")
        )

        preloadManager.checkAndPreload(
            currentTrack = track1,
            positionMs = 177_000L,
            durationMs = 180_000L,
            enabled = true,
            thresholdSeconds = 5
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("track_2", preloadManager.preloadedTrackId.value)

        preloadManager.onTrackChanged("track_2")
        assertNull(preloadManager.preloadedTrackId.value)
    }
}

