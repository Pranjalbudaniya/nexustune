package com.nexus.tune.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase
import com.nexus.tune.core.model.*
import com.nexus.tune.core.player.SleepTimerManager
import com.nexus.tune.core.player.SleepTimerState
import com.nexus.tune.core.player.effects.NexusAudioEffects
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val getListeningStatsUseCase: GetListeningStatsUseCase? = null,
    private val sleepTimerManager: SleepTimerManager? = null
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = settingsRepository.getThemeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig())

    val sleepTimerState: StateFlow<SleepTimerState> = sleepTimerManager?.timerState
        ?: MutableStateFlow(SleepTimerState()).asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.FLAT)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _equalizerEnabled = MutableStateFlow(true)
    val equalizerEnabled: StateFlow<Boolean> = _equalizerEnabled.asStateFlow()

    val listeningStats: StateFlow<ListeningStats> = (getListeningStatsUseCase?.invoke()
        ?: flowOf(ListeningStats()))
        .stateIn(viewModelScope, SharingStarted.Eagerly, ListeningStats())

    fun updateTheme(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.updateThemeConfig(config)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(isDarkMode = isDark))
    }

    fun toggleAmoled(isAmoled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(isAmoled = isAmoled))
    }

    fun setShapePreset(preset: ShapePreset) {
        val current = themeConfig.value
        updateTheme(current.copy(shapePreset = preset))
    }

    fun setPlayerLayout(layout: PlayerLayout) {
        val current = themeConfig.value
        updateTheme(current.copy(playerLayout = layout))
    }

    fun setMiniPlayerStyle(style: MiniPlayerStyle) {
        val current = themeConfig.value
        updateTheme(current.copy(miniPlayerStyle = style))
    }

    fun setCrossfade(seconds: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(crossfadeSeconds = seconds))
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerManager?.startTimer(minutes)
    }

    fun startSleepTimerEndOfTrack() {
        sleepTimerManager?.startEndOfTrackTimer()
    }

    fun cancelSleepTimer() {
        sleepTimerManager?.cancelTimer()
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        NexusAudioEffects.instance.applyPreset(preset)
    }

    fun toggleEqualizer(enabled: Boolean) {
        _equalizerEnabled.value = enabled
        NexusAudioEffects.instance.setEqualizerEnabled(enabled)
    }
}
