package com.nexus.tune.feature.search

import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.domain.usecase.SearchMusicUseCase
import com.nexus.tune.core.model.SearchResult
import com.nexus.tune.core.model.Track
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val musicRepository = mockk<MusicRepository>()
    private val queueRepository = mockk<QueueRepository>(relaxed = true)

    private val searchMusicUseCase = SearchMusicUseCase(musicRepository)
    private val manageQueueUseCase = ManageQueueUseCase(queueRepository)

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { musicRepository.getRecentSearches() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { musicRepository.addRecentSearch(any()) } returns Unit
        coEvery { musicRepository.removeRecentSearch(any()) } returns Unit
        coEvery { musicRepository.clearRecentSearches() } returns Unit
        viewModel = SearchViewModel(searchMusicUseCase, manageQueueUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui state is Idle`() {
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `query change with blank text resets to Idle`() = runTest {
        viewModel.onQueryChange("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is SearchUiState.Idle)
        assertTrue(viewModel.suggestions.value.isEmpty())
    }

    @Test
    fun `query change triggers debounced search and suggestions`() = runTest {
        val sampleTrack = Track(id = "1", title = "Midnight City", artist = "M83")
        coEvery { musicRepository.getSearchSuggestions("mid") } returns listOf("Midnight City", "Midnight Sky")
        coEvery { musicRepository.search("mid", null) } returns MusicResult.Success(
            SearchResult(tracks = listOf(sampleTrack))
        )

        viewModel.onQueryChange("mid")

        // Before debounce delay
        testDispatcher.scheduler.advanceTimeBy(100)
        assertTrue(viewModel.suggestions.value.isEmpty())

        // After debounce delay
        testDispatcher.scheduler.advanceTimeBy(350)
        assertEquals(listOf("Midnight City", "Midnight Sky"), viewModel.suggestions.value)
        assertTrue(viewModel.uiState.value is SearchUiState.Success)
        val successState = viewModel.uiState.value as SearchUiState.Success
        assertEquals(1, successState.result.tracks.size)
        assertEquals("Midnight City", successState.result.tracks[0].title)
    }

    @Test
    fun `playTrack delegates to manageQueueUseCase`() = runTest {
        val sampleTrack = Track(id = "1", title = "Track", artist = "Artist")
        viewModel.playTrack(sampleTrack)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { queueRepository.setQueue(listOf(sampleTrack), 0) }
    }

    @Test
    fun `performSearch records query to searchMusicUseCase`() = runTest {
        coEvery { musicRepository.getRecentSearches() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { musicRepository.addRecentSearch("cyberpunk") } returns Unit
        coEvery { musicRepository.search("cyberpunk", null) } returns MusicResult.Success(SearchResult())

        val vm = SearchViewModel(searchMusicUseCase, manageQueueUseCase)
        vm.performSearch("cyberpunk")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.addRecentSearch("cyberpunk") }
    }

    @Test
    fun `clearRecentSearches delegates to searchMusicUseCase`() = runTest {
        coEvery { musicRepository.getRecentSearches() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { musicRepository.clearRecentSearches() } returns Unit

        val vm = SearchViewModel(searchMusicUseCase, manageQueueUseCase)
        vm.clearRecentSearches()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { musicRepository.clearRecentSearches() }
    }
}

