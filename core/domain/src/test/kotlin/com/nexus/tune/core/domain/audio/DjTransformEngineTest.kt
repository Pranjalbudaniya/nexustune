package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.DjPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DjTransformEngineTest {

    @Test
    fun `getPresetConfig returns expected parameters for all presets`() {
        val normal = DjTransformEngine.getPresetConfig(DjPreset.NORMAL)
        assertEquals(1.0f, normal.speed, 0.001f)
        assertEquals(0, normal.semitones)
        assertEquals(1.0f, normal.soundstageWidth, 0.001f)
        assertFalse(normal.loFiFilterEnabled)

        val nightcore = DjTransformEngine.getPresetConfig(DjPreset.NIGHTCORE)
        assertEquals(1.25f, nightcore.speed, 0.001f)
        assertEquals(2, nightcore.semitones)
        assertEquals(1.15f, nightcore.soundstageWidth, 0.001f)
        assertEquals(2, nightcore.bassBoostDb)

        val slowed = DjTransformEngine.getPresetConfig(DjPreset.SLOWED_REVERB)
        assertEquals(0.85f, slowed.speed, 0.001f)
        assertEquals(-2, slowed.semitones)
        assertEquals(1.40f, slowed.soundstageWidth, 0.001f)

        val vinyl = DjTransformEngine.getPresetConfig(DjPreset.VINTAGE_VINYL)
        assertEquals(0.95f, vinyl.speed, 0.001f)
        assertEquals(-1, vinyl.semitones)
        assertTrue(vinyl.loFiFilterEnabled)

        val vaporwave = DjTransformEngine.getPresetConfig(DjPreset.VAPORWAVE)
        assertEquals(0.80f, vaporwave.speed, 0.001f)
        assertEquals(-3, vaporwave.semitones)
        assertTrue(vaporwave.loFiFilterEnabled)

        val deepBass = DjTransformEngine.getPresetConfig(DjPreset.DEEP_BASS)
        assertEquals(1.0f, deepBass.speed, 0.001f)
        assertEquals(8, deepBass.bassBoostDb)
    }

    @Test
    fun `clamping functions enforce valid physical ranges`() {
        assertEquals(0.50f, DjTransformEngine.clampSpeed(0.1f), 0.001f)
        assertEquals(2.00f, DjTransformEngine.clampSpeed(3.5f), 0.001f)
        assertEquals(1.15f, DjTransformEngine.clampSpeed(1.15f), 0.001f)

        assertEquals(-6, DjTransformEngine.clampSemitones(-12))
        assertEquals(6, DjTransformEngine.clampSemitones(12))
        assertEquals(3, DjTransformEngine.clampSemitones(3))

        assertEquals(0.0f, DjTransformEngine.clampSoundstageWidth(-0.5f), 0.001f)
        assertEquals(2.0f, DjTransformEngine.clampSoundstageWidth(3.0f), 0.001f)
        assertEquals(1.25f, DjTransformEngine.clampSoundstageWidth(1.25f), 0.001f)
    }

    @Test
    fun `calculateEffectiveDuration properly scales duration by speed`() {
        assertEquals(0L, DjTransformEngine.calculateEffectiveDuration(0L, 1.0f))
        // 200 seconds at 1.0x = 200000ms
        assertEquals(200000L, DjTransformEngine.calculateEffectiveDuration(200000L, 1.0f))
        // 200 seconds at 2.0x = 100000ms
        assertEquals(100000L, DjTransformEngine.calculateEffectiveDuration(200000L, 2.0f))
        // 200 seconds at 0.5x = 400000ms
        assertEquals(400000L, DjTransformEngine.calculateEffectiveDuration(200000L, 0.5f))
    }

    @Test
    fun `formatters produce human-readable status strings`() {
        assertEquals("1.25x", DjTransformEngine.formatSpeed(1.25f))
        assertEquals("+2 st", DjTransformEngine.formatPitch(2))
        assertEquals("-3 st", DjTransformEngine.formatPitch(-3))
        assertEquals("0 st (Standard)", DjTransformEngine.formatPitch(0))
        assertEquals("Mono (0%)", DjTransformEngine.formatSoundstage(0.0f))
        assertEquals("Narrow (75%)", DjTransformEngine.formatSoundstage(0.75f))
        assertEquals("Natural Stereo (100%)", DjTransformEngine.formatSoundstage(1.0f))
        assertEquals("Wide (140%)", DjTransformEngine.formatSoundstage(1.40f))
        assertEquals("Super-Wide (180%)", DjTransformEngine.formatSoundstage(1.80f))
    }

    @Test
    fun `calculateStereoCoefficients returns valid matrix`() {
        val (mid, side) = DjTransformEngine.calculateStereoCoefficients(1.5f)
        assertEquals(1.0f, mid, 0.001f)
        assertEquals(1.5f, side, 0.001f)
    }

    @Test
    fun `matchClosestPreset identifies nearest aesthetic profile`() {
        assertEquals(DjPreset.NIGHTCORE, DjTransformEngine.matchClosestPreset(1.25f, 2))
        assertEquals(DjPreset.SLOWED_REVERB, DjTransformEngine.matchClosestPreset(0.85f, -2))
        assertEquals(DjPreset.NORMAL, DjTransformEngine.matchClosestPreset(1.0f, 0))
        assertEquals(DjPreset.VAPORWAVE, DjTransformEngine.matchClosestPreset(0.78f, -3))
    }
}

