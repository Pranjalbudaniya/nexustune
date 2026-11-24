package com.nexus.tune.core.player

import com.nexus.tune.core.model.CrossfadeCurve
import com.nexus.tune.core.player.crossfade.CrossfadeFader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossfadeFaderTest {

    private val fader = CrossfadeFader()

    @Test
    fun `equal power curve maintains constant acoustic energy`() {
        val durationMs = 4000L

        // Boundary: Start (0ms)
        val start = fader.calculateAttenuation(0L, durationMs, CrossfadeCurve.EQUAL_POWER)
        assertEquals(1.0f, start.outgoingVolume, 0.001f)
        assertEquals(0.0f, start.incomingVolume, 0.001f)

        // Boundary: Midpoint (2000ms)
        val mid = fader.calculateAttenuation(2000L, durationMs, CrossfadeCurve.EQUAL_POWER)
        assertEquals(0.7071f, mid.outgoingVolume, 0.005f)
        assertEquals(0.7071f, mid.incomingVolume, 0.005f)

        // Check power sum: out^2 + in^2 = 1.0
        val midPower = (mid.outgoingVolume * mid.outgoingVolume) + (mid.incomingVolume * mid.incomingVolume)
        assertEquals(1.0f, midPower, 0.01f)

        // Boundary: End (4000ms)
        val end = fader.calculateAttenuation(4000L, durationMs, CrossfadeCurve.EQUAL_POWER)
        assertEquals(0.0f, end.outgoingVolume, 0.001f)
        assertEquals(1.0f, end.incomingVolume, 0.001f)
    }

    @Test
    fun `linear curve produces linear ramp`() {
        val durationMs = 6000L

        val start = fader.calculateAttenuation(0L, durationMs, CrossfadeCurve.LINEAR)
        assertEquals(1.0f, start.outgoingVolume, 0.001f)
        assertEquals(0.0f, start.incomingVolume, 0.001f)

        val mid = fader.calculateAttenuation(3000L, durationMs, CrossfadeCurve.LINEAR)
        assertEquals(0.5f, mid.outgoingVolume, 0.001f)
        assertEquals(0.5f, mid.incomingVolume, 0.001f)

        val end = fader.calculateAttenuation(6000L, durationMs, CrossfadeCurve.LINEAR)
        assertEquals(0.0f, end.outgoingVolume, 0.001f)
        assertEquals(1.0f, end.incomingVolume, 0.001f)
    }

    @Test
    fun `exponential curve creates quadratic falloff`() {
        val durationMs = 1000L

        val mid = fader.calculateAttenuation(500L, durationMs, CrossfadeCurve.EXPONENTIAL)
        assertEquals(0.25f, mid.outgoingVolume, 0.001f)
        assertEquals(0.25f, mid.incomingVolume, 0.001f)
    }

    @Test
    fun `invalid duration and bounds handled safely`() {
        val zeroDuration = fader.calculateAttenuation(100L, 0L)
        assertEquals(0.0f, zeroDuration.outgoingVolume, 0.001f)
        assertEquals(1.0f, zeroDuration.incomingVolume, 0.001f)

        val beyondEnd = fader.calculateAttenuation(5000L, 2000L)
        assertEquals(0.0f, beyondEnd.outgoingVolume, 0.001f)
        assertEquals(1.0f, beyondEnd.incomingVolume, 0.001f)

        val negativeElapsed = fader.calculateAttenuation(-100L, 2000L)
        assertEquals(1.0f, negativeElapsed.outgoingVolume, 0.001f)
        assertEquals(0.0f, negativeElapsed.incomingVolume, 0.001f)
    }
}

