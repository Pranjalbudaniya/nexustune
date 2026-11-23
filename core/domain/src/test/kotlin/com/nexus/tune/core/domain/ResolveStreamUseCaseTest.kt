package com.nexus.tune.core.domain

import com.nexus.tune.core.common.ExtractorError
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.AudioStream
import io.mockk.coEvery
import io.mockk.mockk
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
}

