package com.nexus.tune.core.domain

import com.nexus.tune.core.common.ExtractorError
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.AudioStream
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveStreamUseCaseTest {

    private val repository = mockk<MusicRepository>()
    private val useCase = ResolveStreamUseCase(repository)

    @Test
    fun `successful stream resolution returns AudioStream`() = runTest {
        val stream = AudioStream(
            url = "https://audio.example.com/test.opus",
            format = "Opus",
            bitrateKbps = 160,
            expiresAtTimestampMs = 1700000000000L
        )
        coEvery { repository.resolveStreamUrl("track_1") } returns MusicResult.Success(stream)

        val result = useCase("track_1")
        assertTrue(result is MusicResult.Success)
        val data = (result as MusicResult.Success).data
        assertEquals("Opus", data.format)
        assertEquals(160, data.bitrateKbps)
    }

    @Test
    fun `failed stream resolution propagates ExtractorError`() = runTest {
        coEvery { repository.resolveStreamUrl("expired_track") } returns MusicResult.Failure(
            ExtractorError.RateLimitedError()
        )

        val result = useCase("expired_track")
        assertTrue(result is MusicResult.Failure)
        val error = (result as MusicResult.Failure).error
        assertTrue(error is ExtractorError.RateLimitedError)
    }

    @Test
    fun `when quality is AUTO and network is metered, requests LOW quality stream`() = runTest {
        val settingsRepo = mockk<SettingsRepository>()
        every { settingsRepo.getPreferredQuality() } returns flowOf("AUTO")

        val stream = AudioStream(
            url = "https://audio.example.com/low.opus",
            format = "Opus",
            bitrateKbps = 64,
            expiresAtTimestampMs = 1700000000000L
        )
        coEvery { repository.resolveStreamUrl("track_1", "LOW") } returns MusicResult.Success(stream)

        val meteredUseCase = ResolveStreamUseCase(
            musicRepository = repository,
            settingsRepository = settingsRepo,
            isMeteredConnectionProvider = { true }
        )

        val result = meteredUseCase("track_1")
        assertTrue(result is MusicResult.Success)
        assertEquals(64, (result as MusicResult.Success).data.bitrateKbps)
    }

    @Test
    fun `when quality is AUTO and network is unmetered, requests HIGH quality stream`() = runTest {
        val settingsRepo = mockk<SettingsRepository>()
        every { settingsRepo.getPreferredQuality() } returns flowOf("AUTO")

        val stream = AudioStream(
            url = "https://audio.example.com/high.opus",
            format = "Opus",
            bitrateKbps = 160,
            expiresAtTimestampMs = 1700000000000L
        )
        coEvery { repository.resolveStreamUrl("track_1", "HIGH") } returns MusicResult.Success(stream)

        val unmeteredUseCase = ResolveStreamUseCase(
            musicRepository = repository,
            settingsRepository = settingsRepo,
            isMeteredConnectionProvider = { false }
        )

        val result = unmeteredUseCase("track_1")
        assertTrue(result is MusicResult.Success)
        assertEquals(160, (result as MusicResult.Success).data.bitrateKbps)
    }

    @Test
    fun `when preferred quality is set explicitly, requests that quality regardless of network`() = runTest {
        val settingsRepo = mockk<SettingsRepository>()
        every { settingsRepo.getPreferredQuality() } returns flowOf("MEDIUM")

        val stream = AudioStream(
            url = "https://audio.example.com/med.opus",
            format = "Opus",
            bitrateKbps = 128,
            expiresAtTimestampMs = 1700000000000L
        )
        coEvery { repository.resolveStreamUrl("track_1", "MEDIUM") } returns MusicResult.Success(stream)

        val explicitUseCase = ResolveStreamUseCase(
            musicRepository = repository,
            settingsRepository = settingsRepo,
            isMeteredConnectionProvider = { true } // metered, but user explicitly asked for MEDIUM
        )

        val result = explicitUseCase("track_1")
        assertTrue(result is MusicResult.Success)
        assertEquals(128, (result as MusicResult.Success).data.bitrateKbps)
    }
}

