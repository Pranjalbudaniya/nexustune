package com.nexus.tune.core.domain

import com.nexus.tune.core.domain.analytics.ListeningHabitAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListeningHabitAnalyzerTest {

    @Test
    fun `determinePeakPeriod selects highest time slot correctly`() {
        assertEquals("Morning (6 AM - 12 PM)", ListeningHabitAnalyzer.determinePeakPeriod(10, 2, 3, 1))
        assertEquals("Afternoon (12 PM - 6 PM)", ListeningHabitAnalyzer.determinePeakPeriod(2, 15, 3, 1))
        assertEquals("Evening (6 PM - 12 AM)", ListeningHabitAnalyzer.determinePeakPeriod(2, 5, 20, 1))
        assertEquals("Late Night (12 AM - 6 AM)", ListeningHabitAnalyzer.determinePeakPeriod(2, 5, 3, 25))
        assertEquals("All Day", ListeningHabitAnalyzer.determinePeakPeriod(0, 0, 0, 0))
    }

    @Test
    fun `analyzeGenres classifies genres and calculates percentages`() {
        val tracks = listOf(
            Triple("Acoustic Sunset", "Guitarist Guy", "Acoustic Dreams"),
            Triple("Rock Anthem", "Heavy Guitar Band", "Rock Live"),
            Triple("Synthwave Night", "Electronic DJ", "EDM Club"),
            Triple("Piano Sonata", "Orchestra Master", "Classical Classics")
        )

        val genres = ListeningHabitAnalyzer.analyzeGenres(tracks)
        assertEquals(4, genres.size)
        // 1 of each = 25% each
        genres.forEach {
            assertEquals(25, it.percentage)
        }
    }

    @Test
    fun `analyzeGenres handles empty list gracefully`() {
        val genres = ListeningHabitAnalyzer.analyzeGenres(emptyList())
        assertTrue(genres.isEmpty())
    }
}

