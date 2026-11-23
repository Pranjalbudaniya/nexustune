package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase
import com.nexus.tune.core.model.ListeningStats
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetListeningStatsUseCaseTest {

    private val musicRepository = mockk<MusicRepository>()
    private val getListeningStatsUseCase = GetListeningStatsUseCase(musicRepository)

    @Test
    fun `getListeningStats emits correct play counts and top artists`() = runTest {
        val expected = ListeningStats(
            totalPlays = 42,
            topArtists = listOf("Daft Punk", "The Weeknd", "Kavinsky")
        )
        every { musicRepository.getListeningStats() } returns flowOf(expected)

        val resultList = mutableListOf<ListeningStats>()
        getListeningStatsUseCase().collect { resultList.add(it) }

        assertEquals(1, resultList.size)
        assertEquals(42, resultList.first().totalPlays)
        assertEquals(3, resultList.first().topArtists.size)
        assertEquals("Daft Punk", resultList.first().topArtists.first())
    }
}

