package com.nexus.tune.core.player

import com.nexus.tune.core.model.EqualizerPreset
import com.nexus.tune.core.model.SpatialAudioStage
import com.nexus.tune.core.player.effects.NexusAudioEffects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusAudioEffectsTest {

    private val audioEffects = NexusAudioEffects()

    @Test
    fun `audioEffects instance can apply presets safely without session`() {
        audioEffects.applyPreset(EqualizerPreset.BASS_BOOST)
        audioEffects.applyPreset(EqualizerPreset.ROCK)
        audioEffects.applyPreset(EqualizerPreset.FLAT)
        audioEffects.setEqualizerEnabled(true)
        audioEffects.setLoudnessEnhancerEnabled(true)
        audioEffects.setTargetGain(300)
        audioEffects.applyTrackLoudnessGain(2.5f, 200)
        audioEffects.release()
        assertNotNull(audioEffects)
    }

    @Test
    fun `spatialAudioStage conversions map to correct millibel strength values`() {
        assertEquals(0.toShort(), audioEffects.stageToStrength(SpatialAudioStage.OFF))
        assertEquals(250.toShort(), audioEffects.stageToStrength(SpatialAudioStage.STUDIO))
        assertEquals(500.toShort(), audioEffects.stageToStrength(SpatialAudioStage.LOUNGE))
        assertEquals(750.toShort(), audioEffects.stageToStrength(SpatialAudioStage.CONCERT_HALL))
        assertEquals(1000.toShort(), audioEffects.stageToStrength(SpatialAudioStage.WIDE_ARENA))
    }

    @Test
    fun `spatial audio methods operate safely and update state`() {
        audioEffects.setSpatialAudioStage(SpatialAudioStage.STUDIO)
        assertTrue(audioEffects.isSpatialAudioEnabled())
        assertEquals(SpatialAudioStage.STUDIO, audioEffects.getSpatialAudioStage())

        audioEffects.setSpatialAudioStage(SpatialAudioStage.CONCERT_HALL)
        assertEquals(SpatialAudioStage.CONCERT_HALL, audioEffects.getSpatialAudioStage())

        audioEffects.setSpatialAudioStage(SpatialAudioStage.OFF)
        assertFalse(audioEffects.isSpatialAudioEnabled())

        audioEffects.setSpatialAudioEnabled(true)
        assertTrue(audioEffects.isSpatialAudioEnabled())

        audioEffects.release()
        assertNotNull(audioEffects)
    }
}

