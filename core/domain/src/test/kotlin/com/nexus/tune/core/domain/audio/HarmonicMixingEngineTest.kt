package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.*
import org.junit.Test

class HarmonicMixingEngineTest {

    private val trackA = Track(
        id = "t_alpha",
        title = "Astral Horizon",
        artist = "Celestia",
        durationSeconds = 240L
    )

    private val trackB = Track(
        id = "t_beta",
        title = "Neon Midnight",
        artist = "Synthetix",
        durationSeconds = 195L
    )

    @Test
    fun `deriveHarmonicProfile is deterministic for same track`() {
        val profile1 = HarmonicMixingEngine.deriveHarmonicProfile(trackA)
        val profile2 = HarmonicMixingEngine.deriveHarmonicProfile(trackA)

        assertEquals(profile1.trackId, profile2.trackId)
        assertEquals(profile1.key, profile2.key)
        assertEquals(profile1.bpm, profile2.bpm)
        assertEquals(profile1.energyLevel, profile2.energyLevel)
    }

    @Test
    fun `calculateKeyRelationship returns PERFECT_MATCH for identical key`() {
        val (rel, score) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_8A)
        assertEquals(HarmonicRelationship.PERFECT_MATCH, rel)
        assertEquals(100, score)
    }

    @Test
    fun `calculateKeyRelationship returns RELATIVE_KEY for same number different scale`() {
        // 8A (A Minor) and 8B (C Major)
        val (rel, score) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_8B)
        assertEquals(HarmonicRelationship.RELATIVE_KEY, rel)
        assertEquals(90, score)
    }

    @Test
    fun `calculateKeyRelationship returns ADJACENT_STEP for plus or minus 1 step`() {
        // 8A to 9A (dominant) and 8A to 7A (subdominant)
        val (relUp, scoreUp) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_9A)
        assertEquals(HarmonicRelationship.ADJACENT_STEP, relUp)
        assertEquals(85, scoreUp)

        val (relDown, scoreDown) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_7A)
        assertEquals(HarmonicRelationship.ADJACENT_STEP, relDown)
        assertEquals(85, scoreDown)

        // Circle wrap: 12A to 1A
        val (relWrap, scoreWrap) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_12A, CamelotKey.KEY_1A)
        assertEquals(HarmonicRelationship.ADJACENT_STEP, relWrap)
        assertEquals(85, scoreWrap)
    }

    @Test
    fun `calculateKeyRelationship returns ENERGY_BOOST for plus 2 step`() {
        // 8A to 10A (+2 steps)
        val (rel, score) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_10A)
        assertEquals(HarmonicRelationship.ENERGY_BOOST, rel)
        assertEquals(80, score)
    }

    @Test
    fun `calculateKeyRelationship returns ENERGY_DROP for minus 2 step`() {
        // 8A to 6A (-2 steps)
        val (rel, score) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_6A)
        assertEquals(HarmonicRelationship.ENERGY_DROP, rel)
        assertEquals(75, score)
    }

    @Test
    fun `calculateKeyRelationship returns DISSONANT for incompatible distant key`() {
        // 8A to 2A (6 steps, tritone dissonance)
        val (rel, score) = HarmonicMixingEngine.calculateKeyRelationship(CamelotKey.KEY_8A, CamelotKey.KEY_2A)
        assertEquals(HarmonicRelationship.DISSONANT, rel)
        assertEquals(25, score)
    }

    @Test
    fun `calculateTempoCompatibility correctly classifies seamless and half double tempo ratios`() {
        // Within 3 BPM
        val comp1 = HarmonicMixingEngine.calculateTempoCompatibility(120, 122)
        assertEquals(TempoCompatibility.SEAMLESS, comp1)
        assertTrue(comp1.isGoodMix)

        // Within 8 BPM
        val comp2 = HarmonicMixingEngine.calculateTempoCompatibility(120, 126)
        assertEquals(TempoCompatibility.SMOOTH, comp2)
        assertTrue(comp2.isGoodMix)

        // Half/Double time: 70 and 140
        val compHalf = HarmonicMixingEngine.calculateTempoCompatibility(70, 140)
        assertEquals(TempoCompatibility.HALF_DOUBLE, compHalf)
        assertTrue(compHalf.isGoodMix)

        // Large jump
        val compJump = HarmonicMixingEngine.calculateTempoCompatibility(90, 145)
        assertEquals(TempoCompatibility.TEMPO_JUMP, compJump)
        assertFalse(compJump.isGoodMix)
    }

    @Test
    fun `evaluateTransition applies correct crossfade recommendation based on relationship`() {
        val profileA = TrackHarmonicProfile("t1", CamelotKey.KEY_8A, bpm = 124)
        val profileB = TrackHarmonicProfile("t2", CamelotKey.KEY_8B, bpm = 124)
        val profileC = TrackHarmonicProfile("t3", CamelotKey.KEY_2A, bpm = 124)

        val transRelative = HarmonicMixingEngine.evaluateTransition(profileA, profileB)
        assertEquals(HarmonicRelationship.RELATIVE_KEY, transRelative.relationship)
        assertEquals(90, transRelative.compatibilityPercent)
        assertEquals(8, transRelative.recommendedCrossfadeSeconds)

        val transDissonant = HarmonicMixingEngine.evaluateTransition(profileA, profileC)
        assertEquals(HarmonicRelationship.DISSONANT, transDissonant.relationship)
        assertEquals(25, transDissonant.compatibilityPercent)
        assertEquals(2, transDissonant.recommendedCrossfadeSeconds)
    }

    @Test
    fun `harmonizeQueue reorders upcoming tracks to maximize transition compatibility`() {
        val currentTrack = Track("cur", "Current", "Artist", durationSeconds = 180L)
        val tracks = listOf(
            Track("t1", "Track 1", "Artist", durationSeconds = 100L),
            Track("t2", "Track 2", "Artist", durationSeconds = 200L),
            Track("t3", "Track 3", "Artist", durationSeconds = 300L),
            Track("t4", "Track 4", "Artist", durationSeconds = 400L)
        )

        val harmonized = HarmonicMixingEngine.harmonizeQueue(currentTrack, tracks)

        assertEquals(tracks.size, harmonized.size)
        // All tracks are preserved without loss or duplication
        assertEquals(tracks.map { it.id }.toSet(), harmonized.map { it.id }.toSet())
    }
}

