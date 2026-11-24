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

    @Test
    fun `custom equalizer band tuning clamps and stores band levels`() {
        audioEffects.setBandLevel(0, 500)
        audioEffects.setBandLevel(1, -300)
        audioEffects.setBandLevel(2, 1500) // Clamps to 1200
        audioEffects.setBandLevel(3, -2000) // Clamps to -1200
        audioEffects.setBandLevel(4, 0)

        val levels = audioEffects.getBandLevels()
        assertEquals(500, levels[0])
        assertEquals(-300, levels[1])
        assertEquals(1200, levels[2])
        assertEquals(-1200, levels[3])
        assertEquals(0, levels[4])

        audioEffects.setCustomBands(listOf(100, 200, 300, 400, 500))
        val newLevels = audioEffects.getBandLevels()
        assertEquals(listOf(100, 200, 300, 400, 500), newLevels)

        audioEffects.applyPreset(EqualizerPreset.CUSTOM)
        assertEquals(listOf(100, 200, 300, 400, 500), audioEffects.getBandLevels())
    }

    @Test
    fun `headphone target offsets and bass boost can be applied and cleared`() {
        val offsets = listOf(450, 150, 0, 300, 0)
        audioEffects.applyHeadphoneOffsets(offsets)
        assertEquals(offsets, audioEffects.getHeadphoneOffsets())

        audioEffects.setBassBoostStrength(400.toShort())

        audioEffects.clearHeadphoneOffsets()
        assertEquals(listOf(0, 0, 0, 0, 0), audioEffects.getHeadphoneOffsets())

        audioEffects.setVirtualizerStrength(650.toShort())
        audioEffects.setVirtualizerStrength(0.toShort())
    }

    @Test
    fun `sleep drift frequency roll off offsets can be applied and cleared`() {
        val rollOff = listOf(0, 0, -200, -600, -1200)
        audioEffects.applySleepDriftOffsets(rollOff)
        assertEquals(rollOff, audioEffects.getSleepDriftOffsets())

        audioEffects.clearSleepDriftOffsets()
        assertEquals(listOf(0, 0, 0, 0, 0), audioEffects.getSleepDriftOffsets())
    }

    @Test
    fun `dynamic loudness contour offsets can be applied and cleared`() {
        val loudnessOffsets = listOf(800, 440, 0, -45, 450)
        audioEffects.applyDynamicLoudnessOffsets(loudnessOffsets)
        assertEquals(loudnessOffsets, audioEffects.getDynamicLoudnessOffsets())

        audioEffects.clearDynamicLoudnessOffsets()
        assertEquals(listOf(0, 0, 0, 0, 0), audioEffects.getDynamicLoudnessOffsets())
    }

    @Test
    fun `tilt equalizer offsets can be applied and cleared`() {
        val tiltOffsets = listOf(300, 150, 0, -150, -300)
        audioEffects.applyTiltOffsets(tiltOffsets)
        assertEquals(tiltOffsets, audioEffects.getTiltOffsets())

        audioEffects.clearTiltOffsets()
        assertEquals(listOf(0, 0, 0, 0, 0), audioEffects.getTiltOffsets())
    }

    @Test
    fun `mid-bass clarity offset can be applied and cleared`() {
        audioEffects.applyMidBassClarityOffset(-350)
        assertEquals(-350, audioEffects.getMidBassClarityOffset())

        audioEffects.clearMidBassClarityOffset()
        assertEquals(0, audioEffects.getMidBassClarityOffset())
    }
}

