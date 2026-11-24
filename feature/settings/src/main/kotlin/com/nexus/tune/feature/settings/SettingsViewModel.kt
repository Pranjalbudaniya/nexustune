package com.nexus.tune.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.data.storage.StorageManager
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
    private val sleepTimerManager: SleepTimerManager? = null,
    private val storageManager: StorageManager? = null,
    private val audioOutputRouteManager: com.nexus.tune.core.player.routing.AudioOutputRouteManager? = null,
    private val musicRepository: com.nexus.tune.core.domain.repository.MusicRepository? = null,
    private val safeListeningManager: com.nexus.tune.core.player.hearing.SafeListeningManager? = null,
    private val backupManager: com.nexus.tune.core.data.backup.BackupManager? = null,
    private val isMeteredConnectionProvider: (() -> Boolean)? = null,
    private val ambientSoundSynthesizer: com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer? = null,
    private val wakeAlarmManager: com.nexus.tune.core.player.alarm.WakeAlarmManager? = null
) : ViewModel() {

    private val activeAmbientSynthesizer: com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer by lazy {
        ambientSoundSynthesizer ?: com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer.instance
    }

    val ambientState: StateFlow<com.nexus.tune.core.player.ambient.AmbientSynthesizerState> =
        activeAmbientSynthesizer.state

    fun setAmbientSound(type: AmbientSoundType) {
        activeAmbientSynthesizer.setSoundType(type)
        val current = themeConfig.value
        updateTheme(current.copy(ambientSoundType = type))
        if (type != AmbientSoundType.OFF && !activeAmbientSynthesizer.state.value.isPlaying) {
            activeAmbientSynthesizer.start(type, current.ambientSoundVolume)
        }
    }

    fun toggleAmbientPlay() {
        val current = themeConfig.value
        val soundType = if (current.ambientSoundType != AmbientSoundType.OFF) current.ambientSoundType else AmbientSoundType.BROWN_NOISE
        activeAmbientSynthesizer.togglePlay(soundType)
    }

    fun setAmbientVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        activeAmbientSynthesizer.setVolume(clamped)
        val current = themeConfig.value
        updateTheme(current.copy(ambientSoundVolume = clamped))
    }

    val preferredQuality: StateFlow<String> = settingsRepository.getPreferredQuality()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "AUTO")

    val isMeteredConnection: StateFlow<Boolean> = MutableStateFlow(isMeteredConnectionProvider?.invoke() ?: false).asStateFlow()

    fun setPreferredQuality(quality: String) {
        viewModelScope.launch {
            settingsRepository.setPreferredQuality(quality)
        }
    }

    val backupExportResult = MutableStateFlow<String?>(null)
    val backupImportStatus = MutableStateFlow<String?>(null)

    fun exportBackup() {
        viewModelScope.launch {
            val json = backupManager?.exportBackupJson()
            backupExportResult.value = json
            backupImportStatus.value = if (json != null) "Backup exported successfully" else "Export failed"
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val success = backupManager?.importBackupJson(jsonString) ?: false
            backupImportStatus.value = if (success) "Backup restored successfully" else "Import failed"
        }
    }

    fun importFromNewPipe(jsonString: String) {
        viewModelScope.launch {
            val res = backupManager?.importFromNewPipeJson(jsonString)
            backupImportStatus.value = if (res?.success == true) {
                "Imported ${res.playlistsImported} playlists (${res.tracksImported} tracks) from NewPipe"
            } else {
                "NewPipe import completed or no streams found"
            }
        }
    }

    val m3uExportResult = MutableStateFlow<String?>(null)

    fun exportLikedTracksToM3u() {
        viewModelScope.launch {
            val liked = musicRepository?.getLikedTracks()?.firstOrNull() ?: emptyList()
            val m3u = backupManager?.exportPlaylistToM3u("NexusTune Favorites", liked)
            m3uExportResult.value = m3u
            backupImportStatus.value = if (m3u != null) "Exported ${liked.size} tracks to M3U" else "M3U export failed"
        }
    }

    fun importPlaylistFromM3u(m3uContent: String, fallbackTitle: String = "Imported Playlist") {
        viewModelScope.launch {
            val res = backupManager?.importPlaylistFromM3u(m3uContent, fallbackTitle)
            if (res != null && res.second > 0) {
                backupImportStatus.value = "Imported playlist '${res.first}' (${res.second} tracks)"
            } else {
                backupImportStatus.value = "No tracks found in M3U playlist"
            }
        }
    }

    val soundDoseInfo: StateFlow<SoundDoseInfo> = safeListeningManager?.soundDoseInfo
        ?: MutableStateFlow(SoundDoseInfo()).asStateFlow()

    init {
        viewModelScope.launch {
            themeConfig.collect { config ->
                audioOutputRouteManager?.setBitPerfectEnabled(config.bitPerfectUsbAudioEnabled)
                audioOutputRouteManager?.setForcedSampleRate(config.forceHiResSamplingRate)
                NexusAudioEffects.instance.setSpatialAudioEnabled(config.spatialAudioEnabled)
                NexusAudioEffects.instance.setSpatialAudioStage(config.spatialAudioStage)
                sleepTimerManager?.setShakeToExtend(config.shakeToExtendSleepTimerEnabled, config.shakeExtendMinutes)
            }
        }
    }

    val themeConfig: StateFlow<ThemeConfig> = settingsRepository.getThemeConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig())

    val sleepTimerState: StateFlow<SleepTimerState> = sleepTimerManager?.timerState
        ?: MutableStateFlow(SleepTimerState()).asStateFlow()

    val isAlarmRinging: StateFlow<Boolean> = wakeAlarmManager?.isAlarmActive
        ?: MutableStateFlow(false).asStateFlow()

    val alarmFadedVolume: StateFlow<Float> = wakeAlarmManager?.currentFadedVolume
        ?: MutableStateFlow(0f).asStateFlow()

    private val _equalizerPreset = MutableStateFlow(EqualizerPreset.FLAT)
    val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

    private val _equalizerEnabled = MutableStateFlow(true)
    val equalizerEnabled: StateFlow<Boolean> = _equalizerEnabled.asStateFlow()

    val listeningStats: StateFlow<ListeningStats> = (getListeningStatsUseCase?.invoke()
        ?: flowOf(ListeningStats()))
        .stateIn(viewModelScope, SharingStarted.Eagerly, ListeningStats())

    val listeningStreak: StateFlow<ListeningStreak> = settingsRepository.getListeningStreak()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ListeningStreak())

    val milestones: StateFlow<List<ListeningMilestone>> = combine(
        listeningStats,
        listeningStreak,
        themeConfig
    ) { stats, streak, config ->
        val isAudiophile = config.spatialAudioEnabled || config.bitPerfectUsbAudioEnabled || config.loudnessConfig.mode != LoudnessNormalizationMode.OFF
        com.nexus.tune.core.domain.analytics.MilestoneCalculator.evaluateMilestones(stats, streak, isAudiophile)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val timeCapsuleRecap: StateFlow<TimeCapsuleRecap> = combine(
        listeningStats,
        listeningStreak
    ) { stats, streak ->
        com.nexus.tune.core.domain.analytics.MilestoneCalculator.generateRecap(stats, streak)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TimeCapsuleRecap())

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

    fun setCrossfadeCurve(curve: CrossfadeCurve) {
        val current = themeConfig.value
        updateTheme(current.copy(crossfadeCurve = curve))
    }

    fun toggleAutoSkipSegments(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(autoSkipSegments = enabled))
    }

    fun toggleInfiniteAutoPlay(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(infiniteAutoPlayEnabled = enabled))
    }

    fun toggleVolumeNormalization(enabled: Boolean) {
        val current = themeConfig.value
        val newMode = if (enabled) LoudnessNormalizationMode.REPLAYGAIN_TRACK else LoudnessNormalizationMode.OFF
        updateTheme(current.copy(
            volumeNormalizationEnabled = enabled,
            loudnessConfig = current.loudnessConfig.copy(mode = newMode)
        ))
        NexusAudioEffects.instance.setLoudnessEnhancerEnabled(enabled)
    }

    fun setLoudnessMode(mode: LoudnessNormalizationMode) {
        val current = themeConfig.value
        val enabled = mode != LoudnessNormalizationMode.OFF
        updateTheme(current.copy(
            volumeNormalizationEnabled = enabled,
            loudnessConfig = current.loudnessConfig.copy(mode = mode)
        ))
        NexusAudioEffects.instance.setLoudnessEnhancerEnabled(enabled)
    }

    fun setTargetLufs(targetLufs: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(loudnessConfig = current.loudnessConfig.copy(targetLufs = targetLufs)))
    }

    fun setPreAmpGainDb(preAmpGainDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(loudnessConfig = current.loudnessConfig.copy(preAmpGainDb = preAmpGainDb)))
    }

    fun setTargetGain(gainmB: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(targetGainmB = gainmB))
        NexusAudioEffects.instance.setTargetGain(gainmB)
    }

    fun togglePerTrackGain(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(perTrackGainEnabled = enabled))
    }

    fun toggleBitPerfectUsbAudio(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(bitPerfectUsbAudioEnabled = enabled))
        audioOutputRouteManager?.setBitPerfectEnabled(enabled)
    }

    fun setForceHiResSamplingRate(rate: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(forceHiResSamplingRate = rate))
        audioOutputRouteManager?.setForcedSampleRate(rate)
    }

    fun toggleGaplessPreload(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(gaplessPreloadEnabled = enabled))
    }

    fun toggleAutoGenreEq(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(autoGenreEqEnabled = enabled))
    }

    fun toggleSpatialAudio(enabled: Boolean) {
        val current = themeConfig.value
        val stage = if (enabled) {
            if (current.spatialAudioStage == SpatialAudioStage.OFF) SpatialAudioStage.STUDIO else current.spatialAudioStage
        } else {
            SpatialAudioStage.OFF
        }
        updateTheme(current.copy(spatialAudioEnabled = enabled, spatialAudioStage = stage))
        NexusAudioEffects.instance.setSpatialAudioEnabled(enabled)
        if (enabled) {
            NexusAudioEffects.instance.setSpatialAudioStage(stage)
        }
    }

    fun setSpatialAudioStage(stage: SpatialAudioStage) {
        val current = themeConfig.value
        val enabled = stage != SpatialAudioStage.OFF
        updateTheme(current.copy(spatialAudioEnabled = enabled, spatialAudioStage = stage))
        NexusAudioEffects.instance.setSpatialAudioStage(stage)
    }

    fun setVolumeLimit(percent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(volumeLimitPercent = percent))
    }

    fun setSeekbarStyle(style: SeekbarStyle) {
        val current = themeConfig.value
        updateTheme(current.copy(seekbarStyle = style))
    }

    fun toggleResetPitchOnTrackChange(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(resetPitchOnTrackChange = enabled))
    }

    fun toggleSmartShuffle(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(smartShuffleEnabled = enabled))
    }

    fun toggleSafeListeningAlert(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(safeListeningAlertEnabled = enabled))
    }

    fun resetSoundDose() {
        safeListeningManager?.resetDose()
    }

    fun setGaplessPreloadSeconds(seconds: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(gaplessPreloadSeconds = seconds))
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        val current = themeConfig.value
        updateTheme(current.copy(visualizerMode = mode))
        com.nexus.tune.core.player.visualizer.VisualizerManager.instance.setMode(mode)
    }

    fun setSleepTimerFadeSeconds(seconds: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(sleepTimerFadeSeconds = seconds))
        sleepTimerManager?.setFadeDuration(seconds)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerManager?.startTimer(minutes, themeConfig.value.sleepTimerFadeSeconds)
    }

    fun startSleepTimerEndOfTrack() {
        sleepTimerManager?.startEndOfTrackTimer()
    }

    fun cancelSleepTimer() {
        sleepTimerManager?.cancelTimer()
    }

    fun extendSleepTimer(minutes: Int) {
        sleepTimerManager?.extendTimer(minutes)
    }

    fun toggleShakeToExtend(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(shakeToExtendSleepTimerEnabled = enabled))
        sleepTimerManager?.setShakeToExtend(enabled, current.shakeExtendMinutes)
    }

    fun setShakeExtendMinutes(minutes: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(shakeExtendMinutes = minutes))
        sleepTimerManager?.setShakeToExtend(current.shakeToExtendSleepTimerEnabled, minutes)
    }

    fun toggleBedtimeHandoff(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(bedtimeHandoffEnabled = enabled))
        sleepTimerManager?.setBedtimeHandoff(
            enabled = enabled,
            soundType = current.bedtimeHandoffSoundType,
            volume = current.bedtimeHandoffVolume,
            durationMinutes = current.bedtimeHandoffDurationMinutes
        )
    }

    fun setBedtimeHandoffSoundType(type: AmbientSoundType) {
        val current = themeConfig.value
        updateTheme(current.copy(bedtimeHandoffSoundType = type))
        sleepTimerManager?.setBedtimeHandoff(
            enabled = current.bedtimeHandoffEnabled,
            soundType = type,
            volume = current.bedtimeHandoffVolume,
            durationMinutes = current.bedtimeHandoffDurationMinutes
        )
    }

    fun setBedtimeHandoffVolume(volume: Float) {
        val current = themeConfig.value
        val clamped = volume.coerceIn(0.05f, 1.0f)
        updateTheme(current.copy(bedtimeHandoffVolume = clamped))
        sleepTimerManager?.setBedtimeHandoff(
            enabled = current.bedtimeHandoffEnabled,
            soundType = current.bedtimeHandoffSoundType,
            volume = clamped,
            durationMinutes = current.bedtimeHandoffDurationMinutes
        )
    }

    fun setBedtimeHandoffDuration(minutes: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(bedtimeHandoffDurationMinutes = minutes))
        sleepTimerManager?.setBedtimeHandoff(
            enabled = current.bedtimeHandoffEnabled,
            soundType = current.bedtimeHandoffSoundType,
            volume = current.bedtimeHandoffVolume,
            durationMinutes = minutes
        )
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerPreset.value = preset
        NexusAudioEffects.instance.applyPreset(preset)
        if (preset == EqualizerPreset.CUSTOM) {
            NexusAudioEffects.instance.setCustomBands(themeConfig.value.customEqBands)
        }
    }

    val customEqBands: StateFlow<List<Int>> = themeConfig.map { it.customEqBands }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(0, 0, 0, 0, 0))

    fun setCustomEqBandLevel(bandIndex: Int, levelmB: Int) {
        val currentBands = themeConfig.value.customEqBands.toMutableList()
        while (currentBands.size < 5) currentBands.add(0)
        if (bandIndex in 0 until currentBands.size) {
            currentBands[bandIndex] = levelmB.coerceIn(-1200, 1200)
            updateTheme(themeConfig.value.copy(customEqBands = currentBands))
            NexusAudioEffects.instance.setBandLevel(bandIndex, levelmB)
            if (_equalizerPreset.value != EqualizerPreset.CUSTOM) {
                _equalizerPreset.value = EqualizerPreset.CUSTOM
            }
        }
    }

    fun resetCustomEqBands() {
        val flatBands = listOf(0, 0, 0, 0, 0)
        updateTheme(themeConfig.value.copy(customEqBands = flatBands))
        NexusAudioEffects.instance.setCustomBands(flatBands)
        if (_equalizerPreset.value != EqualizerPreset.CUSTOM) {
            _equalizerPreset.value = EqualizerPreset.CUSTOM
        }
    }

    val customEqProfiles: StateFlow<List<CustomEqProfile>> = settingsRepository.getCustomEqProfiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val closestPresetMatch: StateFlow<Pair<EqualizerPreset, Int>> = themeConfig.map { config ->
        com.nexus.tune.core.domain.audio.EqCurveCalculator.findClosestPreset(config.customEqBands)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EqualizerPreset.FLAT to 100)

    fun saveCustomEqProfile(name: String, bands: List<Int>) {
        if (name.isBlank()) return
        val profile = CustomEqProfile(name = name.trim(), bands = bands)
        viewModelScope.launch {
            settingsRepository.saveCustomEqProfile(profile)
        }
    }

    fun deleteCustomEqProfile(profileId: String) {
        viewModelScope.launch {
            settingsRepository.deleteCustomEqProfile(profileId)
        }
    }

    fun applyCustomEqProfile(profile: CustomEqProfile) {
        _equalizerPreset.value = EqualizerPreset.CUSTOM
        updateTheme(themeConfig.value.copy(customEqBands = profile.bands))
        NexusAudioEffects.instance.setCustomBands(profile.bands)
    }

    fun toggleEqualizer(enabled: Boolean) {
        _equalizerEnabled.value = enabled
        NexusAudioEffects.instance.setEqualizerEnabled(enabled)
    }

    fun setCacheBudget(budgetMb: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(cacheBudgetMb = budgetMb))
        storageManager?.pruneCache(budgetMb)
    }

    fun clearCache(): Long {
        return storageManager?.clearCache() ?: 0L
    }

    fun clearLyricsCache() {
        viewModelScope.launch {
            musicRepository?.clearLyricsCache()
        }
    }

    fun deduplicateLikedTracks() {
        viewModelScope.launch {
            val removed = musicRepository?.deduplicateLikedTracks() ?: 0
            backupImportStatus.value = if (removed > 0) "Removed $removed duplicate tracks" else "No duplicates found in library"
        }
    }

    fun updateWakeAlarmConfig(config: WakeAlarmConfig) {
        val current = themeConfig.value
        updateTheme(current.copy(wakeAlarmConfig = config))
        wakeAlarmManager?.scheduleAlarm(config)
    }

    fun testTriggerWakeAlarm() {
        wakeAlarmManager?.triggerWakeAlarm(themeConfig.value.wakeAlarmConfig)
    }

    fun stopWakeAlarm() {
        wakeAlarmManager?.stopAlarm()
    }

    fun snoozeWakeAlarm(minutes: Int = 10) {
        wakeAlarmManager?.snooze(minutes)
    }

    fun setDjPreset(preset: DjPreset) {
        val config = com.nexus.tune.core.domain.audio.DjTransformEngine.getPresetConfig(preset)
        updateTheme(themeConfig.value.copy(djFxConfig = config))
    }

    fun setDjRememberAcrossTracks(remember: Boolean) {
        updateTheme(themeConfig.value.copy(djRememberAcrossTracks = remember))
    }
}
