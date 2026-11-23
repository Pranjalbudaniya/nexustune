package com.nexus.tune.core.player

import com.nexus.tune.core.model.EqualizerPreset
import com.nexus.tune.core.player.effects.NexusAudioEffects
import org.junit.Assert.assertNotNull
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
}

