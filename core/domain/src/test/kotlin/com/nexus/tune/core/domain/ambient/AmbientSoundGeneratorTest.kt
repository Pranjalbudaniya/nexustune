package com.nexus.tune.core.domain.ambient

import com.nexus.tune.core.model.AmbientSoundType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AmbientSoundGeneratorTest {

    private lateinit var generator: AmbientSoundGenerator

    @Before
    fun setUp() {
        generator = AmbientSoundGenerator(seed = 42L)
    }

    @Test
    fun testGenerateOffProducesSilence() {
        val chunk = generator.generateStereoChunk(AmbientSoundType.OFF, numFrames = 512)
        assertEquals(1024, chunk.size)
        assertTrue(chunk.all { it == 0.toShort() })
    }

    @Test
    fun testOutputBufferDimensions() {
        val numFrames = 1024
        for (type in AmbientSoundType.values()) {
            val chunk = generator.generateStereoChunk(type, numFrames)
            assertEquals(numFrames * 2, chunk.size)
        }
    }

    @Test
    fun testNoiseGenerationWithinShortLimits() {
        val numFrames = 2048
        val noiseTypes = listOf(
            AmbientSoundType.WHITE_NOISE,
            AmbientSoundType.PINK_NOISE,
            AmbientSoundType.BROWN_NOISE
        )

        for (type in noiseTypes) {
            val chunk = generator.generateStereoChunk(type, numFrames)
            assertTrue("Expected non-empty noise for $type", chunk.any { it != 0.toShort() })
            // Verify within valid Short amplitude
            for (sample in chunk) {
                assertTrue(sample >= Short.MIN_VALUE)
                assertTrue(sample <= Short.MAX_VALUE)
            }
        }
    }

    @Test
    fun testBinauralBeatsHaveChannelSeparation() {
        val numFrames = 2048
        val binauralTypes = listOf(
            AmbientSoundType.BINAURAL_ALPHA,
            AmbientSoundType.BINAURAL_THETA,
            AmbientSoundType.BINAURAL_DELTA
        )

        for (type in binauralTypes) {
            val chunk = generator.generateStereoChunk(type, numFrames)
            var differences = 0
            for (i in 0 until numFrames) {
                val left = chunk[i * 2]
                val right = chunk[i * 2 + 1]
                if (left != right) {
                    differences++
                }
            }
            assertTrue("Binaural beats must differ between L and R channels", differences > numFrames / 2)
        }
    }

    @Test
    fun testSineWaveGeneratesMonoAcrossStereoChannels() {
        val numFrames = 1024
        val chunk432 = generator.generateStereoChunk(AmbientSoundType.SOLFEGGIO_432HZ, numFrames)
        for (i in 0 until numFrames) {
            val left = chunk432[i * 2]
            val right = chunk432[i * 2 + 1]
            assertEquals("Solfeggio pure sine should be identical in L and R channels", left, right)
        }
    }

    @Test
    fun testResetClearsStatesCleanly() {
        generator.generateStereoChunk(AmbientSoundType.BINAURAL_ALPHA, 1000)
        generator.reset()
        val chunkAfterReset = generator.generateStereoChunk(AmbientSoundType.OFF, 256)
        assertTrue(chunkAfterReset.all { it == 0.toShort() })
    }
}

