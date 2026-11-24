package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.LoudnessConfig
import com.nexus.tune.core.model.LoudnessNormalizationMode
import com.nexus.tune.core.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioNormalizationEngineTest {

    @Test
    fun `calculateGainMultiplier returns 1_0 when mode is OFF`() {
        val config = LoudnessConfig(mode = LoudnessNormalizationMode.OFF, targetLufs = -14.0f, preAmpGainDb = 3.0f)
        val mult = AudioNormalizationEngine.calculateGainMultiplier(-20.0f, config)
        assertEquals(1.0f, mult, 0.001f)
    }

    @Test
    fun `calculateGainMultiplier calculates correct boost for quiet track`() {
        // Track at -20 LUFS, target at -14 LUFS, preamp 0dB -> +6dB gain -> ~1.995x multiplier
        val config = LoudnessConfig(mode = LoudnessNormalizationMode.REPLAYGAIN_TRACK, targetLufs = -14.0f, preAmpGainDb = 0.0f)
        val mult = AudioNormalizationEngine.calculateGainMultiplier(-20.0f, config)
        assertTrue(mult > 1.9f && mult <= 2.0f)
    }

    @Test
    fun `calculateGainMultiplier calculates correct attenuation for loud track`() {
        // Track at -8 LUFS, target at -14 LUFS, preamp 0dB -> -6dB gain -> ~0.501x multiplier
        val config = LoudnessConfig(mode = LoudnessNormalizationMode.REPLAYGAIN_TRACK, targetLufs = -14.0f, preAmpGainDb = 0.0f)
        val mult = AudioNormalizationEngine.calculateGainMultiplier(-8.0f, config)
        assertTrue(mult > 0.49f && mult < 0.52f)
    }

    @Test
    fun `calculateGainMultiplier applies preamp gain correctly`() {
        // Track at -14 LUFS, target at -14 LUFS, preamp +3dB -> +3dB gain -> ~1.41x multiplier
        val config = LoudnessConfig(mode = LoudnessNormalizationMode.REPLAYGAIN_TRACK, targetLufs = -14.0f, preAmpGainDb = 3.0f)
        val mult = AudioNormalizationEngine.calculateGainMultiplier(-14.0f, config)
        assertTrue(mult > 1.4f && mult < 1.43f)
    }

    @Test
    fun `calculateGainMultiplier clamps within safety bounds`() {
        // Extremely quiet track (-40 LUFS), +26dB requested, clamped to MAX_GAIN_MULTIPLIER (2.0f)
        val config = LoudnessConfig(mode = LoudnessNormalizationMode.REPLAYGAIN_TRACK, targetLufs = -14.0f, preAmpGainDb = 6.0f)
        val mult = AudioNormalizationEngine.calculateGainMultiplier(-40.0f, config)
        assertEquals(2.0f, mult, 0.001f)

        // Extremely loud track (0 LUFS), -20dB requested, clamped to MIN_GAIN_MULTIPLIER (0.1f)
        val quietConfig = LoudnessConfig(mode = LoudnessNormalizationMode.REPLAYGAIN_TRACK, targetLufs = -23.0f, preAmpGainDb = -6.0f)
        val quietMult = AudioNormalizationEngine.calculateGainMultiplier(0.0f, quietConfig)
        assertEquals(0.1f, quietMult, 0.001f)
    }

    @Test
    fun `applySoftLimiter preserves linear pass-through below threshold`() {
        val sample = 0.8f
        val limited = AudioNormalizationEngine.applySoftLimiter(sample, threshold = 0.95f)
        assertEquals(0.8f, limited, 0.0001f)

        val negativeSample = -0.75f
        val limitedNeg = AudioNormalizationEngine.applySoftLimiter(negativeSample, threshold = 0.95f)
        assertEquals(-0.75f, limitedNeg, 0.0001f)
    }

    @Test
    fun `applySoftLimiter saturates smoothly above threshold without exceeding 1_0`() {
        val hotSample = 1.2f
        val limited = AudioNormalizationEngine.applySoftLimiter(hotSample, threshold = 0.95f)
        assertTrue(limited > 0.95f)
        assertTrue(limited <= 1.0f)

        val veryHotSample = 2.5f
        val limitedVeryHot = AudioNormalizationEngine.applySoftLimiter(veryHotSample, threshold = 0.95f)
        assertTrue(limitedVeryHot <= 1.0f)
    }

    @Test
    fun `estimateTrackLufs categorizes acoustic, remix, and standard tracks accurately`() {
        val acousticTrack = Track(id = "1", title = "Acoustic Sunset", artist = "Folk Band")
        assertEquals(-18.0f, AudioNormalizationEngine.estimateTrackLufs(acousticTrack), 0.1f)

        val remixTrack = Track(id = "2", title = "Summer Anthem (Club Remix)", artist = "DJ Pulse")
        assertEquals(-9.5f, AudioNormalizationEngine.estimateTrackLufs(remixTrack), 0.1f)

        val standardTrack = Track(id = "3", title = "Normal Song", artist = "Artist")
        assertEquals(-14.0f, AudioNormalizationEngine.estimateTrackLufs(standardTrack), 0.1f)
    }
}
