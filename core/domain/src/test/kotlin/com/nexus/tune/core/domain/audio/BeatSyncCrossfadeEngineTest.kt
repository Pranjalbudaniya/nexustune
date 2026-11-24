package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeatSyncCrossfadeEngineTest {

    @Test
    fun testCalculatePhraseDurationMs() {
        // At 120 BPM, 1 beat = 500ms
        // 4 bars * 4 beats = 16 beats = 8000ms
        val duration4Bars = BeatSyncCrossfadeEngine.calculatePhraseDurationMs(120, bars = 4, beatsPerBar = 4)
        assertEquals(8000L, duration4Bars)

        // 8 bars = 32 beats = 16000ms
        val duration8Bars = BeatSyncCrossfadeEngine.calculatePhraseDurationMs(120, bars = 8, beatsPerBar = 4)
        assertEquals(16000L, duration8Bars)
    }

    @Test
    fun testTransitionPlanOff() {
        val plan = BeatSyncCrossfadeEngine.calculateTransitionPlan(
            outgoingProfile = TrackHarmonicProfile("1", CamelotKey.KEY_8B, 120, 80),
            incomingProfile = TrackHarmonicProfile("2", CamelotKey.KEY_9B, 124, 82),
            mode = BeatCrossfadeMode.OFF
        )
        assertEquals(4000L, plan.targetDurationMs)
        assertEquals(0, plan.musicalBars)
        assertFalse(plan.requiresTempoBend)
    }

    @Test
    fun testTransitionPlanSmartBeatSync() {
        val plan = BeatSyncCrossfadeEngine.calculateTransitionPlan(
            outgoingProfile = TrackHarmonicProfile("1", CamelotKey.KEY_8B, 124, 80),
            incomingProfile = TrackHarmonicProfile("2", CamelotKey.KEY_9B, 126, 82),
            mode = BeatCrossfadeMode.SMART_BEAT_SYNC,
            maxBendPercent = 6
        )
        assertEquals(8, plan.musicalBars)
        assertEquals(32, plan.totalBeats)
        assertTrue(plan.requiresTempoBend)
    }

    @Test
    fun testTransitionPlanClub16Bar() {
        val plan = BeatSyncCrossfadeEngine.calculateTransitionPlan(
            outgoingProfile = TrackHarmonicProfile("1", CamelotKey.KEY_8B, 128, 85),
            incomingProfile = TrackHarmonicProfile("2", CamelotKey.KEY_8B, 128, 90),
            mode = BeatCrossfadeMode.PHRASE_ALIGNED_16BAR
        )
        assertEquals(16, plan.musicalBars)
        assertEquals(64, plan.totalBeats)
        // 64 beats at 128 BPM = 30000ms
        assertEquals(30000L, plan.targetDurationMs)
    }

    @Test
    fun testBassSwapGainsAsymmetry() {
        // Start (0.0): Outgoing bass full, Incoming bass muted
        val startGains = BeatSyncCrossfadeEngine.calculateBassSwapGains(0.0f)
        assertEquals(0.0f, startGains.outgoingBassGainDb, 0.001f)
        assertEquals(-48.0f, startGains.incomingBassGainDb, 0.001f)

        // Midpoint (0.50): Outgoing bass is significantly cut (-24dB), Incoming bass is still silent (-48dB)
        // Prevents low-frequency summing collision!
        val midGains = BeatSyncCrossfadeEngine.calculateBassSwapGains(0.50f)
        assertEquals(-24.0f, midGains.outgoingBassGainDb, 0.01f)
        assertEquals(-48.0f, midGains.incomingBassGainDb, 0.01f)

        // End (1.0): Outgoing bass muted, Incoming bass full
        val endGains = BeatSyncCrossfadeEngine.calculateBassSwapGains(1.0f)
        assertEquals(-48.0f, endGains.outgoingBassGainDb, 0.001f)
        assertEquals(0.0f, endGains.incomingBassGainDb, 0.001f)
    }

    @Test
    fun testTempoRampWithinTolerance() {
        val rampMid = BeatSyncCrossfadeEngine.calculateTempoRamp(0.5f, startBpm = 120, targetBpm = 126, maxBendPercent = 6)
        // Ratio = 126/120 = 1.05. At 0.5: 1.0 + 0.025 = 1.025
        assertEquals(1.025f, rampMid, 0.001f)
    }

    @Test
    fun testTempoRampExceedingTolerance() {
        // Diff = |120 - 150| / 120 = 25% > 6%
        val ramp = BeatSyncCrossfadeEngine.calculateTempoRamp(0.5f, startBpm = 120, targetBpm = 150, maxBendPercent = 6)
        assertEquals(1.0f, ramp, 0.001f)
    }
}

