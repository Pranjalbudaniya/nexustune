package com.nexus.tune.core.domain

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.SearchMusicUseCase
import com.nexus.tune.core.model.SearchResult
import com.nexus.tune.core.model.Track
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMusicUseCaseTest {
    private val repository = mockk<MusicRepository>()
    private val useCase = SearchMusicUseCase(repository)

    @Test
    fun `blank query returns empty success without querying repository`() = runTest {
        val result = useCase("")
        assertTrue(result is MusicResult.Success)
        val data = (result as MusicResult.Success).data
        assertEquals(0, data.tracks.size)
    }

    @Test
    fun `valid query delegates to repository`() = runTest {
        val mockTrack = Track(id = "1", title = "Test Song", artist = "Test Artist")
        coEvery { repository.search("rock", null) } returns MusicResult.Success(
            SearchResult(tracks = listOf(mockTrack))
        )

        val result = useCase("rock")
        assertTrue(result is MusicResult.Success)
        val tracks = (result as MusicResult.Success).data.tracks
        assertEquals(1, tracks.size)
        assertEquals("Test Song", tracks[0].title)
    }

    @Test
    fun `recent search methods delegate to repository`() = runTest {
        coEvery { repository.getRecentSearches() } returns kotlinx.coroutines.flow.flowOf(listOf("Synthwave"))
        coEvery { repository.addRecentSearch("Synthwave") } returns Unit
        coEvery { repository.removeRecentSearch("Synthwave") } returns Unit
        coEvery { repository.clearRecentSearches() } returns Unit

        useCase.addRecentSearch("Synthwave")
        useCase.removeRecentSearch("Synthwave")
        useCase.clearRecentSearches()

        val list = mutableListOf<List<String>>()
        useCase.getRecentSearches().collect { list.add(it) }

        assertEquals(1, list.size)
        assertEquals("Synthwave", list.first().first())
    }
}

