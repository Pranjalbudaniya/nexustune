package com.nexus.tune.core.data

import com.nexus.tune.core.common.ExtractorError
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.AudioStream
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class StreamExpiryRecoveryTest {

    private val musicRepository = mockk<MusicRepository>()
    private val resolveStreamUseCase = ResolveStreamUseCase(musicRepository)

    @Test
    fun `when stream expires with 403 or 410, re-extraction provides fresh stream`() = runTest {
        val trackId = "track_expired_123"
        val freshStream = AudioStream(
            url = "https://rr---googlevideo.com/videoplayback?expire=999999999&fresh=true",
            format = "Opus",
            bitrateKbps = 160,
            expiresAtTimestampMs = System.currentTimeMillis() + 21600000L
        )

        // Simulate re-resolving the stream after player hits HTTP 403
        coEvery { musicRepository.resolveStreamUrl(trackId) } returns MusicResult.Success(freshStream)

        val result = resolveStreamUseCase(trackId)

        assertTrue(result is MusicResult.Success)
        val audioStream = (result as MusicResult.Success).data
        assertEquals("https://rr---googlevideo.com/videoplayback?expire=999999999&fresh=true", audioStream.url)
        assertEquals(160, audioStream.bitrateKbps)
        coVerify(exactly = 1) { musicRepository.resolveStreamUrl(trackId) }
    }

    @Test
    fun `when re-extraction fails during network outage, returns NetworkError gracefully`() = runTest {
        val trackId = "track_offline_456"

        coEvery { musicRepository.resolveStreamUrl(trackId) } returns MusicResult.Failure(ExtractorError.NetworkError())

        val result = resolveStreamUseCase(trackId)

        assertTrue(result is MusicResult.Failure)
        val failure = result as MusicResult.Failure
        assertTrue(failure.error is ExtractorError.NetworkError)
        assertTrue(failure.error.canRetry)
    }

    @Test
    fun `when extractor is rate limited with 429, returns RateLimitedError with retry flag`() = runTest {
        val trackId = "track_rate_limited_789"

        coEvery { musicRepository.resolveStreamUrl(trackId) } returns MusicResult.Failure(ExtractorError.RateLimitedError(retryAfterSeconds = 30))

        val result = resolveStreamUseCase(trackId)

        assertTrue(result is MusicResult.Failure)
        val error = (result as MusicResult.Failure).error
        assertTrue(error is ExtractorError.RateLimitedError)
        assertEquals(30, (error as ExtractorError.RateLimitedError).retryAfterSeconds)
        assertTrue(error.canRetry)
    }
}
