package com.nexus.tune.core.data

import com.nexus.tune.core.common.ExtractorError
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.DownloadRepository
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.DownloadTrackUseCase
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.model.Track
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class OfflinePlaybackFallbackTest {

    private val downloadRepository = mockk<DownloadRepository>()
    private val musicRepository = mockk<MusicRepository>()
    private val downloadUseCase = DownloadTrackUseCase(downloadRepository)
    private val resolveStreamUseCase = ResolveStreamUseCase(musicRepository)

    @Test
    fun `downloaded track provides local file path when network is disabled`() = runTest {
        val localTrack = Track(
            id = "downloaded_track_1",
            title = "Offline Echoes",
            artist = "Local Synth",
            isDownloaded = true,
            localFilePath = "/data/user/0/com.nexus.tune/files/downloads/downloaded_track_1.opus"
        )

        every { downloadRepository.getDownloadedTracks() } returns flowOf(listOf(localTrack))

        val downloadedList = mutableListOf<Track>()
        downloadUseCase.getDownloadedTracks().collect { downloadedList.addAll(it) }

        assertEquals(1, downloadedList.size)
        val track = downloadedList.first()
        assertTrue(track.isDownloaded)
        assertNotNull(track.localFilePath)
        assertTrue(track.localFilePath!!.startsWith("/data/user/0/com.nexus.tune/files/downloads/"))
    }

    @Test
    fun `un-downloaded track stream resolution under network loss returns NetworkError`() = runTest {
        val nonDownloadedId = "stream_only_track_2"

        coEvery { musicRepository.resolveStreamUrl(nonDownloadedId) } returns MusicResult.Failure(ExtractorError.NetworkError())

        val result = resolveStreamUseCase(nonDownloadedId)

        assertTrue(result is MusicResult.Failure)
        val failure = result as MusicResult.Failure
        assertTrue(failure.error is ExtractorError.NetworkError)
        assertEquals("Network connection error", failure.error.message)
    }
}
