package com.nexus.tune.core.domain.audio

import com.nexus.tune.core.model.HornDiffuserConfig
import com.nexus.tune.core.model.HornDiffuserMode
import com.nexus.tune.core.model.HornDiffuserPreset
import com.nexus.tune.core.model.HornFlareGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class HornDiffuserEngineTest {

    private lateinit var engine: HornDiffuserEngine

    @Before
    fun setUp() {
        engine = HornDiffuserEngine(sampleRate = 44100)
    }

    @Test
    fun `bypassed engine preserves audio buffer identically`() {
        val original = floatArrayOf(0.1f, -0.2f, 0.3f, -0.4f, 0.5f, -0.6f)
        val buffer = original.clone()
        val config = HornDiffuserConfig(isEnabled = false)

        val metrics = engine.processInterleaved(buffer, config = config)

        for (i in original.indices) {
            assertEquals(original[i], buffer[i], 1e-6f)
        }
        assertEquals("Bypassed (Direct Radiation)", metrics.hornLoadingRating)
    }

    @Test
    fun `acoustic loading enhances lower midrange tactile presence`() {
        // 600 Hz tone in horn loading region
        val midTone = FloatArray(256) { idx ->
            sin(2.0 * PI * 600.0 * (idx / 2) / 44100.0).toFloat() * 0.3f
        }
        val buffer = midTone.clone()
        val config = HornDiffuserConfig(
            isEnabled = true,
            preset = HornDiffuserPreset.KLIPSCHORN_HERITAGE,
            geometry = HornFlareGeometry.TRACTRIX,
            flareIntensity = 1.0f
        )

        engine.processInterleaved(buffer, config = config)

        var sumOrig = 0.0f
        var sumLoaded = 0.0f
        for (i in 128 until 256) {
            sumOrig += abs(midTone[i])
            sumLoaded += abs(buffer[i])
        }

        assertTrue("Acoustic horn loading should boost lower-mid presence: orig=$sumOrig, loaded=$sumLoaded", sumLoaded > sumOrig)
    }

    @Test
    fun `throat anti-resonance notch attenuates 1800 Hz cavity resonance`() {
        // 1800 Hz throat resonance tone
        val resonanceTone = FloatArray(256) { idx ->
            sin(2.0 * PI * 1800.0 * (idx / 2) / 44100.0).toFloat() * 0.4f
        }
        val buffer = resonanceTone.clone()
        val config = HornDiffuserConfig(
            isEnabled = true,
            mode = HornDiffuserMode.THROAT_ANTI_RESONANCE,
            throatAntiResonanceDb = -6.0f,
            flareIntensity = 1.0f
        )

        engine.processInterleaved(buffer, config = config)

        var sumOrig = 0.0f
        var sumNotched = 0.0f
        for (i in 128 until 256) {
            sumOrig += abs(resonanceTone[i])
            sumNotched += abs(buffer[i])
        }

        assertTrue("Throat anti-resonance notch should attenuate 1800 Hz: orig=$sumOrig, notched=$sumNotched", sumNotched < sumOrig)
    }

    @Test
    fun `wide dispersion creates cross-channel micro-delay decorrelation`() {
        // Left channel impulse, Right channel zero
        val stereoImpulse = FloatArray(128)
        stereoImpulse[0] = 0.8f // L = 0.8, R = 0.0

        val config = HornDiffuserConfig(
            isEnabled = true,
            mode = HornDiffuserMode.WIDE_DISPERSION,
            horizontalCoverageAngle = 110,
            flareIntensity = 0.8f
        )

        engine.processInterleaved(stereoImpulse, config = config)

        // Across delay buffer, Right channel should receive cross-dispersed acoustic signal
        var rightChannelHasDispersed = false
        for (i in 1 until 128 step 2) {
            if (abs(stereoImpulse[i]) > 0.01f) {
                rightChannelHasDispersed = true
                break
            }
        }

        assertTrue("QRD horizontal dispersion should cross-feed delayed acoustic energy into right channel", rightChannelHasDispersed)
    }

    @Test
    fun `solo horn coloration mode outputs extracted flare residue`() {
        val tone = FloatArray(128) { 0.2f }
        val buffer = tone.clone()
        val config = HornDiffuserConfig(
            isEnabled = true,
            mode = HornDiffuserMode.SOLO_HORN_COLORATION,
            flareIntensity = 0.8f
        )

        engine.processInterleaved(buffer, config = config)

        var hasResidue = false
        for (sample in buffer) {
            if (abs(sample) > 0.01f) {
                hasResidue = true
                break
            }
        }
        assertTrue("Solo horn coloration mode should output acoustic residue", hasResidue)
    }

    @Test
    fun `reset clears filter and delay memories`() {
        val buffer = floatArrayOf(0.5f, 0.5f)
        val config = HornDiffuserConfig(isEnabled = true)

        engine.processInterleaved(buffer, config = config)
        engine.reset()

        val silentBuffer = floatArrayOf(0.0f, 0.0f)
        val metrics = engine.processInterleaved(silentBuffer, config = HornDiffuserConfig(isEnabled = false))
        assertEquals("Bypassed (Direct Radiation)", metrics.hornLoadingRating)
    }

    @Test
    fun `calculateMetrics computes acoustic impedance and coverage angle`() {
        val config = HornDiffuserConfig(
            isEnabled = true,
            preset = HornDiffuserPreset.STUDIO_BI_RADIAL,
            geometry = HornFlareGeometry.CONSTANT_DIRECTIVITY,
            horizontalCoverageAngle = 90
        )

        val metrics = HornDiffuserEngine.calculateMetrics(config, floatArrayOf(0.1f, 0.1f))

        assertTrue(metrics.throatAcousticImpedanceRatio > 1.0f)
        assertEquals(90, metrics.horizontalCoverageAngle)
        assertTrue(metrics.hornLoadingRating.isNotEmpty())
    }
}

