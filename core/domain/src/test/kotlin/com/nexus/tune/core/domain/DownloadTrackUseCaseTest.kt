package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.repository.DownloadRepository
import com.nexus.tune.core.domain.usecase.DownloadTrackUseCase
import com.nexus.tune.core.model.Track
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadTrackUseCaseTest {

    private val downloadRepository = mockk<DownloadRepository>()
    private val useCase = DownloadTrackUseCase(downloadRepository)

    @Test
    fun `download delegates to repository`() = runTest {
        val track = Track(id = "d1", title = "Download Track", artist = "Artist")
        coEvery { downloadRepository.downloadTrack(track) } returns true

        val result = useCase.download(track)
        assertTrue(result)
        coVerify { downloadRepository.downloadTrack(track) }
    }

    @Test
    fun `getDownloadedTracks returns flow from repository`() = runTest {
        val tracks = listOf(Track(id = "d1", title = "Offline Track", artist = "Artist", isDownloaded = true))
        every { downloadRepository.getDownloadedTracks() } returns flowOf(tracks)

        useCase.getDownloadedTracks().collect { list ->
            assertEquals(1, list.size)
            assertEquals("Offline Track", list.first().title)
        }
    }

    @Test
    fun `delete delegates to repository`() = runTest {
        coEvery { downloadRepository.deleteDownload("d1") } returns Unit
        useCase.delete("d1")
        coVerify { downloadRepository.deleteDownload("d1") }
    }
}

