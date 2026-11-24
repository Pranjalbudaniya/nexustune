package com.nexus.tune.core.player.effects

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.nexus.tune.core.model.EqualizerPreset
import com.nexus.tune.core.model.SpatialAudioStage

class NexusAudioEffects {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private var spatialAudioEnabled: Boolean = false
    private var currentSpatialStage: SpatialAudioStage = SpatialAudioStage.OFF
    private var customBandLevels: IntArray = IntArray(5) { 0 }
    private var karaokeOffsets: List<Int> = List(5) { 0 }
    private var headphoneOffsets: List<Int> = List(5) { 0 }
    private var sleepDriftOffsets: List<Int> = List(5) { 0 }
    private var dynamicLoudnessOffsets: List<Int> = List(5) { 0 }
    private var tiltOffsets: List<Int> = List(5) { 0 }
    private var midBassClarityOffset: Int = 0
    private var airBandOffset: Int = 0
    private var subHarmonicOffset: Int = 0
    private var deEsserOffset: Int = 0
    private var tubeWarmthOffset: Int = 0
    private var transientPunchOffset: Int = 0
    private var spatialSpreadOffset: Int = 0
    private var tapeWarmthOffset: Int = 0
    private var mainsHumOffset: Int = 0
    private var feedbackGuardOffset: Int = 0
    private var soundstageElevationOffset: Int = 0
    private var monoBassPunchOffset: Int = 0
    private var preampTiltOffset: Int = 0
    private var slewRateOffset: Int = 0
    private var hornDiffuserOffset: Int = 0

    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == currentSessionId) return
        release()
        currentSessionId = sessionId
        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
                setStrength(500.toShort()) // 50% default boost
            }
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = false
                setTargetGain(0)
            }
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = spatialAudioEnabled
                if (spatialAudioEnabled) {
                    setStrength(stageToStrength(currentSpatialStage))
                }
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "AudioEffect initialization not supported on session $sessionId: ${e.message}")
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to toggle equalizer: ${e.message}")
        }
    }

    fun setLoudnessEnhancerEnabled(enabled: Boolean) {
        try {
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to toggle loudness enhancer: ${e.message}")
        }
    }

    fun setTargetGain(gainmB: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainmB)
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set target gain: ${e.message}")
        }
    }

    fun applyTrackLoudnessGain(gainDb: Float, baseTargetGainmB: Int = 0) {
        val additionalGainmB = (gainDb * 100).toInt()
        val combinedGainmB = (baseTargetGainmB + additionalGainmB).coerceIn(0, 1000)
        setTargetGain(combinedGainmB)
    }

    fun setPreset(presetIndex: Int) {
        val eq = equalizer ?: return
        try {
            val numPresets = eq.numberOfPresets
            if (presetIndex in 0 until numPresets) {
                eq.usePreset(presetIndex.toShort())
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set preset $presetIndex: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            when (preset) {
                EqualizerPreset.FLAT -> {
                    for (i in 0 until numBands) eq.setBandLevel(i.toShort(), 0)
                    bassBoost?.setStrength(0.toShort())
                }
                EqualizerPreset.BASS_BOOST -> {
                    if (numBands >= 2) {
                        eq.setBandLevel(0.toShort(), 600.toShort())
                        eq.setBandLevel(1.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(800.toShort())
                }
                EqualizerPreset.TREBLE_BOOST -> {
                    if (numBands >= 5) {
                        eq.setBandLevel((numBands - 1).toShort(), 600.toShort())
                        eq.setBandLevel((numBands - 2).toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(200.toShort())
                }
                EqualizerPreset.VOCAL -> {
                    if (numBands >= 3) {
                        eq.setBandLevel(1.toShort(), 500.toShort())
                        eq.setBandLevel(2.toShort(), 500.toShort())
                    }
                    bassBoost?.setStrength(200.toShort())
                }
                EqualizerPreset.ELECTRONIC -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 500.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(700.toShort())
                }
                EqualizerPreset.ROCK -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 400.toShort())
                        eq.setBandLevel(1.toShort(), 200.toShort())
                        eq.setBandLevel(2.toShort(), (-100).toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(500.toShort())
                }
                EqualizerPreset.CLASSICAL -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 300.toShort())
                        eq.setBandLevel(1.toShort(), 200.toShort())
                        eq.setBandLevel(2.toShort(), (-200).toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(100.toShort())
                }
                EqualizerPreset.JAZZ -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 200.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(2.toShort(), 300.toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 300.toShort())
                    }
                    bassBoost?.setStrength(300.toShort())
                }
                EqualizerPreset.HIP_HOP -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 700.toShort())
                        eq.setBandLevel(1.toShort(), 500.toShort())
                        eq.setBandLevel(2.toShort(), 0.toShort())
                        eq.setBandLevel(3.toShort(), 200.toShort())
                        eq.setBandLevel(4.toShort(), 400.toShort())
                    }
                    bassBoost?.setStrength(800.toShort())
                }
                EqualizerPreset.ACOUSTIC -> {
                    if (numBands >= 5) {
                        eq.setBandLevel(0.toShort(), 200.toShort())
                        eq.setBandLevel(1.toShort(), 300.toShort())
                        eq.setBandLevel(2.toShort(), 400.toShort())
                        eq.setBandLevel(3.toShort(), 400.toShort())
                        eq.setBandLevel(4.toShort(), 300.toShort())
                    }
                    bassBoost?.setStrength(150.toShort())
                }
                EqualizerPreset.CUSTOM -> {
                    setCustomBands(customBandLevels.toList())
                }
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to apply preset $preset: ${e.message}")
        }
    }

    fun setBandLevel(band: Int, levelmB: Int) {
        val clamped = levelmB.coerceIn(-1200, 1200)
        if (band in 0 until 5) {
            customBandLevels[band] = clamped
        }
        val eq = equalizer ?: return
        try {
            if (band in 0 until eq.numberOfBands.toInt()) {
                val kOffset = karaokeOffsets.getOrElse(band) { 0 }
                val hOffset = headphoneOffsets.getOrElse(band) { 0 }
                val sOffset = sleepDriftOffsets.getOrElse(band) { 0 }
                val dOffset = dynamicLoudnessOffsets.getOrElse(band) { 0 }
                val combined = (clamped + kOffset + hOffset + sOffset + dOffset).coerceIn(-1500, 1500)
                eq.setBandLevel(band.toShort(), combined.toShort())
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set band $band level: ${e.message}")
        }
    }

    fun applyKaraokeOffsets(offsets: List<Int>) {
        karaokeOffsets = offsets
        refreshBands()
    }

    fun clearKaraokeOffsets() {
        karaokeOffsets = List(5) { 0 }
        refreshBands()
    }

    fun getKaraokeOffsets(): List<Int> = karaokeOffsets

    fun applyHeadphoneOffsets(offsets: List<Int>) {
        headphoneOffsets = offsets
        refreshBands()
    }

    fun clearHeadphoneOffsets() {
        headphoneOffsets = List(5) { 0 }
        refreshBands()
    }

    fun getHeadphoneOffsets(): List<Int> = headphoneOffsets

    fun applySleepDriftOffsets(offsets: List<Int>) {
        sleepDriftOffsets = offsets
        refreshBands()
    }

    fun clearSleepDriftOffsets() {
        sleepDriftOffsets = List(5) { 0 }
        refreshBands()
    }

    fun getSleepDriftOffsets(): List<Int> = sleepDriftOffsets

    fun applyDynamicLoudnessOffsets(offsets: List<Int>) {
        dynamicLoudnessOffsets = offsets
        refreshBands()
    }

    fun clearDynamicLoudnessOffsets() {
        dynamicLoudnessOffsets = List(5) { 0 }
        refreshBands()
    }

    fun getDynamicLoudnessOffsets(): List<Int> = dynamicLoudnessOffsets

    fun applyTiltOffsets(offsets: List<Int>) {
        tiltOffsets = offsets
        refreshBands()
    }

    fun clearTiltOffsets() {
        tiltOffsets = List(5) { 0 }
        refreshBands()
    }

    fun getTiltOffsets(): List<Int> = tiltOffsets

    fun applyMidBassClarityOffset(offset: Int) {
        midBassClarityOffset = offset
        refreshBands()
    }

    fun clearMidBassClarityOffset() {
        midBassClarityOffset = 0
        refreshBands()
    }

    fun getMidBassClarityOffset(): Int = midBassClarityOffset

    fun applyAirBandOffset(offset: Int) {
        airBandOffset = offset
        refreshBands()
    }

    fun clearAirBandOffset() {
        airBandOffset = 0
        refreshBands()
    }

    fun getAirBandOffset(): Int = airBandOffset

    fun applySubHarmonicOffset(offset: Int) {
        subHarmonicOffset = offset
        refreshBands()
    }

    fun clearSubHarmonicOffset() {
        subHarmonicOffset = 0
        refreshBands()
    }

    fun getSubHarmonicOffset(): Int = subHarmonicOffset

    fun applyDeEsserOffset(offset: Int) {
        deEsserOffset = offset
        refreshBands()
    }

    fun clearDeEsserOffset() {
        deEsserOffset = 0
        refreshBands()
    }

    fun getDeEsserOffset(): Int = deEsserOffset

    fun applyTubeWarmthOffset(offset: Int) {
        tubeWarmthOffset = offset
        refreshBands()
    }

    fun clearTubeWarmthOffset() {
        tubeWarmthOffset = 0
        refreshBands()
    }

    fun getTubeWarmthOffset(): Int = tubeWarmthOffset

    fun applyTransientPunchOffset(offset: Int) {
        transientPunchOffset = offset
        refreshBands()
    }

    fun clearTransientPunchOffset() {
        transientPunchOffset = 0
        refreshBands()
    }

    fun getTransientPunchOffset(): Int = transientPunchOffset

    fun applySpatialSpreadOffset(offset: Int) {
        spatialSpreadOffset = offset
        refreshBands()
    }

    fun clearSpatialSpreadOffset() {
        spatialSpreadOffset = 0
        refreshBands()
    }

    fun getSpatialSpreadOffset(): Int = spatialSpreadOffset

    fun applyTapeWarmthOffset(offset: Int) {
        tapeWarmthOffset = offset
        refreshBands()
    }

    fun clearTapeWarmthOffset() {
        tapeWarmthOffset = 0
        refreshBands()
    }

    fun getTapeWarmthOffset(): Int = tapeWarmthOffset

    fun applyMainsHumOffset(offset: Int) {
        mainsHumOffset = offset
        refreshBands()
    }

    fun clearMainsHumOffset() {
        mainsHumOffset = 0
        refreshBands()
    }

    fun getMainsHumOffset(): Int = mainsHumOffset

    fun applyFeedbackGuardOffset(offset: Int) {
        feedbackGuardOffset = offset
        refreshBands()
    }

    fun clearFeedbackGuardOffset() {
        feedbackGuardOffset = 0
        refreshBands()
    }

    fun getFeedbackGuardOffset(): Int = feedbackGuardOffset

    fun applySoundstageElevationOffset(offset: Int) {
        soundstageElevationOffset = offset
        refreshBands()
    }

    fun clearSoundstageElevationOffset() {
        soundstageElevationOffset = 0
        refreshBands()
    }

    fun getSoundstageElevationOffset(): Int = soundstageElevationOffset

    fun applyMonoBassPunchOffset(offset: Int) {
        monoBassPunchOffset = offset
        refreshBands()
    }

    fun clearMonoBassPunchOffset() {
        monoBassPunchOffset = 0
        refreshBands()
    }

    fun getMonoBassPunchOffset(): Int = monoBassPunchOffset

    fun applyPreampTiltOffset(offset: Int) {
        preampTiltOffset = offset
        refreshBands()
    }

    fun clearPreampTiltOffset() {
        preampTiltOffset = 0
        refreshBands()
    }

    fun getPreampTiltOffset(): Int = preampTiltOffset

    fun applySlewRateOffset(offset: Int) {
        slewRateOffset = offset
        refreshBands()
    }

    fun clearSlewRateOffset() {
        slewRateOffset = 0
        refreshBands()
    }

    fun getSlewRateOffset(): Int = slewRateOffset

    fun applyHornDiffuserOffset(offset: Int) {
        hornDiffuserOffset = offset
        refreshBands()
    }

    fun clearHornDiffuserOffset() {
        hornDiffuserOffset = 0
        refreshBands()
    }

    fun getHornDiffuserOffset(): Int = hornDiffuserOffset

    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength)
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set bass boost strength: ${e.message}")
        }
    }

    private fun refreshBands() {
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt().coerceAtMost(5)
            for (i in 0 until numBands) {
                val base = customBandLevels.getOrElse(i) { 0 }
                val kOffset = karaokeOffsets.getOrElse(i) { 0 }
                val hOffset = headphoneOffsets.getOrElse(i) { 0 }
                val sOffset = sleepDriftOffsets.getOrElse(i) { 0 }
                val dOffset = dynamicLoudnessOffsets.getOrElse(i) { 0 }
                val tOffset = tiltOffsets.getOrElse(i) { 0 }
                val mOffset = if (i == 1) midBassClarityOffset else 0
                val aOffset = if (i == 4) airBandOffset else 0
                val sHarmonicOffset = if (i == 0) subHarmonicOffset else 0
                val deEssOffset = if (i == 3) deEsserOffset else 0
                val tubeOffset = if (i == 1 || i == 2) (tubeWarmthOffset * (if (i == 1) 1.0f else 0.6f)).toInt() else 0
                val transientOffset = if (i == 0 || i == 4) (transientPunchOffset * (if (i == 0) 1.0f else 0.7f)).toInt() else 0
                val spatialOffset = if (i == 4) (spatialSpreadOffset * 0.8f).toInt() else 0
                val tapeOffset = if (i == 0 || i == 1) (tapeWarmthOffset * (if (i == 0) 1.0f else 0.7f)).toInt() else 0
                val humOffset = if (i == 0) mainsHumOffset else 0
                val feedbackOffset = if (i == 2 || i == 3) (feedbackGuardOffset * (if (i == 2) 0.8f else 1.0f)).toInt() else 0
                val elevationOffset = if (i == 4) soundstageElevationOffset else 0
                val monoBassOffset = if (i == 0 || i == 1) (monoBassPunchOffset * (if (i == 0) 1.0f else 0.6f)).toInt() else 0
                val preampOffset = if (i == 4) preampTiltOffset else 0
                val slewOffset = if (i == 3 || i == 4) (slewRateOffset * (if (i == 4) 1.0f else 0.7f)).toInt() else 0
                val hornOffset = if (i == 2 || i == 4) (hornDiffuserOffset * (if (i == 2) 1.0f else 0.8f)).toInt() else 0
                val combined = (base + kOffset + hOffset + sOffset + dOffset + tOffset + mOffset + aOffset + sHarmonicOffset + deEssOffset + tubeOffset + transientOffset + spatialOffset + tapeOffset + humOffset + feedbackOffset + elevationOffset + monoBassOffset + preampOffset + slewOffset + hornOffset).coerceIn(-1500, 1500)
                eq.setBandLevel(i.toShort(), combined.toShort())
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to refresh bands: ${e.message}")
        }
    }

    fun setCustomBands(bands: List<Int>) {
        bands.forEachIndexed { index, level ->
            setBandLevel(index, level)
        }
    }

    fun getBandLevels(): List<Int> {
        val eq = equalizer
        if (eq != null) {
            try {
                val bands = (0 until eq.numberOfBands.toInt().coerceAtMost(5)).map { band ->
                    eq.getBandLevel(band.toShort()).toInt()
                }
                if (bands.isNotEmpty()) return bands
            } catch (e: Exception) {
                // Return cached
            }
        }
        return customBandLevels.toList()
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        equalizer = null
        bassBoost = null
        loudnessEnhancer = null
        virtualizer = null
        currentSessionId = 0
    }

    fun setSpatialAudioEnabled(enabled: Boolean) {
        spatialAudioEnabled = enabled
        try {
            virtualizer?.enabled = enabled
            if (enabled && currentSpatialStage != SpatialAudioStage.OFF) {
                virtualizer?.setStrength(stageToStrength(currentSpatialStage))
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to toggle virtualizer: ${e.message}")
        }
    }

    fun setSpatialAudioStage(stage: SpatialAudioStage) {
        currentSpatialStage = stage
        if (stage == SpatialAudioStage.OFF) {
            setSpatialAudioEnabled(false)
            return
        }
        spatialAudioEnabled = true
        try {
            virtualizer?.enabled = true
            virtualizer?.setStrength(stageToStrength(stage))
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set spatial audio stage: ${e.message}")
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        try {
            if (strength > 0) {
                virtualizer?.enabled = true
                virtualizer?.setStrength(strength)
            } else if (!spatialAudioEnabled) {
                virtualizer?.enabled = false
            }
        } catch (e: Exception) {
            Log.w("NexusAudioEffects", "Failed to set virtualizer strength: ${e.message}")
        }
    }

    fun stageToStrength(stage: SpatialAudioStage): Short {
        return when (stage) {
            SpatialAudioStage.OFF -> 0
            SpatialAudioStage.STUDIO -> 250
            SpatialAudioStage.LOUNGE -> 500
            SpatialAudioStage.CONCERT_HALL -> 750
            SpatialAudioStage.WIDE_ARENA -> 1000
        }.toShort()
    }

    fun getSpatialAudioStage(): SpatialAudioStage = currentSpatialStage
    fun isSpatialAudioEnabled(): Boolean = spatialAudioEnabled

    companion object {
        val instance: NexusAudioEffects by lazy { NexusAudioEffects() }
    }
}
