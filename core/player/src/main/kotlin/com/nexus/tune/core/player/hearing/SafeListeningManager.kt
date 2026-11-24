package com.nexus.tune.core.player.hearing

import com.nexus.tune.core.model.SoundDoseInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

class SafeListeningManager {

    private var cumulativeDoseUnits: Double = 0.0
    private var totalListeningSeconds: Long = 0L

    private val _soundDoseInfo = MutableStateFlow(SoundDoseInfo())
    val soundDoseInfo: StateFlow<SoundDoseInfo> = _soundDoseInfo.asStateFlow()

    // 100% weekly dose reference = 80 dBA for 40 hours = 144,000 reference dose units
    private val maxWeeklyReferenceUnits = 144000.0

    fun recordExposure(durationSeconds: Long, volumeLevel: Float) {
        if (durationSeconds <= 0) return
        val clampedVol = volumeLevel.coerceIn(0.0f, 1.0f)
        val intensityFactor = (clampedVol / 0.8).coerceAtLeast(0.1).pow(3.5)
        val doseAdded = durationSeconds * intensityFactor
        cumulativeDoseUnits += doseAdded
        totalListeningSeconds += durationSeconds

        val dosePercent = ((cumulativeDoseUnits / maxWeeklyReferenceUnits) * 100f).toFloat().coerceIn(0f, 200f)
        _soundDoseInfo.value = SoundDoseInfo(
            dosePercent = dosePercent,
            isOverExposed = dosePercent >= 100f,
            listeningMinutes = totalListeningSeconds / 60
        )
    }

    fun resetDose() {
        cumulativeDoseUnits = 0.0
        totalListeningSeconds = 0L
        _soundDoseInfo.value = SoundDoseInfo()
    }
}
