package com.nexus.tune.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.tune.core.data.storage.StorageManager
import com.nexus.tune.core.domain.audio.HeadphoneCorrectionEngine
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
                if (config.headphoneCorrectionConfig.isEnabled) {
                    val offsets = HeadphoneCorrectionEngine.calculateTargetEqOffsets(config.headphoneCorrectionConfig.targetCurve)
                    NexusAudioEffects.instance.applyHeadphoneOffsets(offsets)
                    val bassStrength = HeadphoneCorrectionEngine.calculateBassBoostStrength(config.headphoneCorrectionConfig.targetCurve)
                    if (bassStrength > 0) {
                        NexusAudioEffects.instance.setBassBoostStrength(bassStrength.toShort())
                    }
                } else {
                    NexusAudioEffects.instance.clearHeadphoneOffsets()
                }
                if (config.stereoWidthConfig.isEnabled) {
                    val strength = com.nexus.tune.core.domain.audio.StereoWidenerEngine.mapWidthToVirtualizerStrength(config.stereoWidthConfig.customWidthPercent)
                    NexusAudioEffects.instance.setVirtualizerStrength(strength)
                }
                sleepTimerManager?.setSleepDriftProfile(config.sleepDriftProfile)
                if (config.dynamicLoudnessConfig.isEnabled) {
                    val loudnessOffsets = com.nexus.tune.core.domain.audio.DynamicLoudnessEngine.calculateContourOffsets(
                        mode = config.dynamicLoudnessConfig.mode,
                        currentVolume = 0.5f,
                        intensityPercent = config.dynamicLoudnessConfig.intensityPercent
                    )
                    NexusAudioEffects.instance.applyDynamicLoudnessOffsets(loudnessOffsets)
                } else {
                    NexusAudioEffects.instance.clearDynamicLoudnessOffsets()
                }
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

    val sonicDnaProfile: StateFlow<SonicDnaProfile> = combine(
        listeningStats,
        listeningStreak
    ) { stats, streak ->
        com.nexus.tune.core.domain.analytics.SonicDnaCalculator.calculateSonicDna(stats, streak)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SonicDnaProfile())

    fun getShareableSonicDnaText(): String {
        return com.nexus.tune.core.domain.analytics.SonicDnaCalculator.formatShareableSummary(sonicDnaProfile.value)
    }

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

    fun setVisualizerPalette(palette: VisualizerPalette) {
        val current = themeConfig.value
        updateTheme(current.copy(visualizerPalette = palette))
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

    fun setAutoStashEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(autoStashConfig = current.autoStashConfig.copy(isEnabled = enabled)))
    }

    fun setAutoStashBudgetMb(budgetMb: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(autoStashConfig = current.autoStashConfig.copy(storageBudgetMb = budgetMb)))
    }

    fun setAutoStashStrategy(strategy: AutoStashStrategy) {
        val current = themeConfig.value
        updateTheme(current.copy(autoStashConfig = current.autoStashConfig.copy(strategy = strategy)))
    }

    fun setAutoStashWifiOnly(wifiOnly: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(autoStashConfig = current.autoStashConfig.copy(wifiOnly = wifiOnly)))
    }

    fun setHeadphoneCorrectionEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(headphoneCorrectionConfig = current.headphoneCorrectionConfig.copy(isEnabled = enabled)))
    }

    fun setHeadphoneTargetCurve(curve: HeadphoneTargetCurve) {
        val current = themeConfig.value
        updateTheme(current.copy(headphoneCorrectionConfig = current.headphoneCorrectionConfig.copy(targetCurve = curve)))
    }

    fun setHeadphoneCrossfeedEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(headphoneCorrectionConfig = current.headphoneCorrectionConfig.copy(crossfeedEnabled = enabled)))
    }

    fun setStereoWidthEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(stereoWidthConfig = current.stereoWidthConfig.copy(isEnabled = enabled)))
    }

    fun setStereoWidthMode(mode: StereoWidthMode) {
        val preset = com.nexus.tune.core.domain.audio.StereoWidenerEngine.getPresetConfig(mode)
        updateTheme(themeConfig.value.copy(stereoWidthConfig = preset))
    }

    fun setStereoWidthPercent(percent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(stereoWidthConfig = current.stereoWidthConfig.copy(customWidthPercent = percent.coerceIn(0, 200))))
    }

    fun setStereoHaasDelayMs(delayMs: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(stereoWidthConfig = current.stereoWidthConfig.copy(haasDelayMs = delayMs.coerceIn(0f, 25f))))
    }

    fun setStereoMidSideLevels(midDb: Float, sideDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(stereoWidthConfig = current.stereoWidthConfig.copy(
            midLevelDb = midDb.coerceIn(-12f, 6f),
            sideLevelDb = sideDb.coerceIn(-12f, 6f)
        )))
    }

    fun setSleepDriftProfile(profile: SleepDriftProfile) {
        updateTheme(themeConfig.value.copy(sleepDriftProfile = profile))
        sleepTimerManager?.setSleepDriftProfile(profile)
    }

    fun setDitherMode(mode: DitherMode) {
        updateTheme(themeConfig.value.copy(ditherMode = mode))
    }

    fun setDynamicLoudnessEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicLoudnessConfig = current.dynamicLoudnessConfig.copy(isEnabled = enabled)))
    }

    fun setDynamicLoudnessMode(mode: DynamicLoudnessMode) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicLoudnessConfig = current.dynamicLoudnessConfig.copy(mode = mode)))
    }

    fun setDynamicLoudnessIntensity(intensity: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicLoudnessConfig = current.dynamicLoudnessConfig.copy(intensityPercent = intensity.coerceIn(0, 100))))
    }

    fun setBeatCrossfadeEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(beatCrossfadeConfig = current.beatCrossfadeConfig.copy(isEnabled = enabled)))
    }

    fun setBeatCrossfadeMode(mode: BeatCrossfadeMode) {
        val current = themeConfig.value
        updateTheme(current.copy(beatCrossfadeConfig = current.beatCrossfadeConfig.copy(mode = mode)))
    }

    fun setBeatCrossfadeMaxBend(maxBend: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(beatCrossfadeConfig = current.beatCrossfadeConfig.copy(maxTempoBendPercent = maxBend.coerceIn(0, 15))))
    }

    fun setSilenceTrimEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(silenceTrimConfig = current.silenceTrimConfig.copy(isEnabled = enabled)))
    }

    fun setSilenceTrimMode(mode: SilenceTrimMode) {
        val current = themeConfig.value
        updateTheme(current.copy(silenceTrimConfig = current.silenceTrimConfig.copy(
            mode = mode,
            thresholdDb = mode.defaultThresholdDb,
            minDurationMs = mode.defaultMinDurationMs
        )))
    }

    fun setSilenceTrimThresholdDb(thresholdDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(silenceTrimConfig = current.silenceTrimConfig.copy(thresholdDb = thresholdDb.coerceIn(-60f, -20f))))
    }

    fun setResamplingFilterMode(mode: ResamplingFilterMode) {
        val current = themeConfig.value
        updateTheme(current.copy(resamplingFilterConfig = current.resamplingFilterConfig.copy(mode = mode)))
    }

    fun setResamplingFilterCutoff(cutoff: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(resamplingFilterConfig = current.resamplingFilterConfig.copy(cutoffRatio = cutoff.coerceIn(0.80f, 0.99f))))
    }

    fun setPhaseGuardEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(phaseMeterConfig = current.phaseMeterConfig.copy(isEnabled = enabled)))
    }

    fun setMonoGuardMode(mode: MonoGuardMode) {
        val current = themeConfig.value
        updateTheme(current.copy(phaseMeterConfig = current.phaseMeterConfig.copy(guardMode = mode, minCorrelationThreshold = mode.threshold)))
    }

    fun setPhaseGuardThreshold(threshold: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(phaseMeterConfig = current.phaseMeterConfig.copy(minCorrelationThreshold = threshold.coerceIn(-0.5f, 0.8f))))
    }

    fun setTiltEqEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tiltEqualizerConfig = current.tiltEqualizerConfig.copy(isEnabled = enabled)))
    }

    fun setTiltEqDb(tiltDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tiltEqualizerConfig = current.tiltEqualizerConfig.copy(tiltDb = tiltDb.coerceIn(-6.0f, 6.0f))))
    }

    fun setTiltEqPivotHz(pivotHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tiltEqualizerConfig = current.tiltEqualizerConfig.copy(pivotHz = pivotHz.coerceIn(100.0f, 10000.0f))))
    }

    fun setTiltEqLoudnessCompensate(compensate: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tiltEqualizerConfig = current.tiltEqualizerConfig.copy(loudnessCompensate = compensate)))
    }

    fun setDeClickerEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(deClickerConfig = current.deClickerConfig.copy(isEnabled = enabled)))
    }

    fun setDeClickerMode(mode: DeClickerMode) {
        val current = themeConfig.value
        updateTheme(current.copy(deClickerConfig = current.deClickerConfig.copy(mode = mode, sensitivityMultiplier = mode.defaultSensitivity)))
    }

    fun setDeClickerSensitivity(sensitivity: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(deClickerConfig = current.deClickerConfig.copy(sensitivityMultiplier = sensitivity.coerceIn(1.5f, 10.0f))))
    }

    fun setDeClickerMaxWidth(maxWidth: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(deClickerConfig = current.deClickerConfig.copy(maxClickWidthSamples = maxWidth.coerceIn(1, 16))))
    }

    fun setMidBassClarityEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(midBassClarityConfig = current.midBassClarityConfig.copy(isEnabled = enabled)))
    }

    fun setMidBassClarityMode(mode: MidBassClarityMode) {
        val current = themeConfig.value
        updateTheme(current.copy(midBassClarityConfig = current.midBassClarityConfig.copy(mode = mode)))
    }

    fun setMidBassClarityCenterHz(centerHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(midBassClarityConfig = current.midBassClarityConfig.copy(centerFrequencyHz = centerHz.coerceIn(200.0f, 500.0f))))
    }

    fun setMidBassClarityThreshold(thresholdPercent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(midBassClarityConfig = current.midBassClarityConfig.copy(dynamicThresholdPercent = thresholdPercent.coerceIn(20, 80))))
    }

    fun setCrosstalkCancellationEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(crosstalkCancellationConfig = current.crosstalkCancellationConfig.copy(isEnabled = enabled)))
    }

    fun setCrosstalkCancellationMode(mode: CrosstalkCancellationMode) {
        val current = themeConfig.value
        updateTheme(current.copy(crosstalkCancellationConfig = current.crosstalkCancellationConfig.copy(mode = mode, delaySamples = mode.defaultDelaySamples)))
    }

    fun setCrosstalkCancellationDelay(delaySamples: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(crosstalkCancellationConfig = current.crosstalkCancellationConfig.copy(delaySamples = delaySamples.coerceIn(1, 24))))
    }

    fun setCrosstalkCancellationIntensity(intensityPercent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(crosstalkCancellationConfig = current.crosstalkCancellationConfig.copy(cancellationIntensityPercent = intensityPercent.coerceIn(10, 100))))
    }

    fun setInfrasonicFilterEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(infrasonicFilterConfig = current.infrasonicFilterConfig.copy(isEnabled = enabled)))
    }

    fun setInfrasonicFilterCutoff(cutoffHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(infrasonicFilterConfig = current.infrasonicFilterConfig.copy(cutoffHz = cutoffHz.coerceIn(10.0f, 40.0f))))
    }

    fun setInfrasonicFilterSlope(slope: InfrasonicSlope) {
        val current = themeConfig.value
        updateTheme(current.copy(infrasonicFilterConfig = current.infrasonicFilterConfig.copy(slope = slope)))
    }

    fun setInfrasonicDcBlockerEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(infrasonicFilterConfig = current.infrasonicFilterConfig.copy(dcBlockerEnabled = enabled)))
    }

    fun setAirBandEnabled(enabled: Boolean) {
        val current = themeConfig.value
        val updated = current.copy(airBandExciterConfig = current.airBandExciterConfig.copy(isEnabled = enabled))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.AirBandExciterEngine.calculateHardwareOffsetMb(
            updated.airBandExciterConfig.boostDb,
            enabled
        )
        NexusAudioEffects.instance.applyAirBandOffset(offset)
    }

    fun setAirBandMode(mode: AirBandMode) {
        val current = themeConfig.value
        val isEnabled = mode != AirBandMode.OFF
        val updated = current.copy(
            airBandExciterConfig = current.airBandExciterConfig.copy(
                isEnabled = isEnabled,
                mode = mode,
                boostDb = mode.boostDb,
                harmonicExcitationPercent = mode.harmonicPercent
            )
        )
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.AirBandExciterEngine.calculateHardwareOffsetMb(
            updated.airBandExciterConfig.boostDb,
            isEnabled
        )
        NexusAudioEffects.instance.applyAirBandOffset(offset)
    }

    fun setAirBandFrequency(frequency: AirBandFrequency) {
        val current = themeConfig.value
        updateTheme(current.copy(airBandExciterConfig = current.airBandExciterConfig.copy(frequency = frequency)))
    }

    fun setAirBandBoostDb(boostDb: Float) {
        val current = themeConfig.value
        val clamped = boostDb.coerceIn(0.0f, 8.0f)
        val updated = current.copy(airBandExciterConfig = current.airBandExciterConfig.copy(boostDb = clamped))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.AirBandExciterEngine.calculateHardwareOffsetMb(
            clamped,
            updated.airBandExciterConfig.isEnabled
        )
        NexusAudioEffects.instance.applyAirBandOffset(offset)
    }

    fun setAirBandHarmonics(harmonicPercent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(airBandExciterConfig = current.airBandExciterConfig.copy(harmonicExcitationPercent = harmonicPercent.coerceIn(0, 100))))
    }

    fun setSubHarmonicSynthEnabled(enabled: Boolean) {
        val current = themeConfig.value
        val updated = current.copy(subHarmonicSynthConfig = current.subHarmonicSynthConfig.copy(isEnabled = enabled))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.SubHarmonicSynthEngine.calculateHardwareOffsetMb(
            updated.subHarmonicSynthConfig.intensityPercent,
            enabled,
            updated.subHarmonicSynthConfig.target
        )
        NexusAudioEffects.instance.applySubHarmonicOffset(offset)
    }

    fun setSubHarmonicSynthMode(mode: SubHarmonicMode) {
        val current = themeConfig.value
        val isEnabled = mode != SubHarmonicMode.OFF
        val updated = current.copy(
            subHarmonicSynthConfig = current.subHarmonicSynthConfig.copy(
                isEnabled = isEnabled,
                mode = mode,
                target = mode.target,
                intensityPercent = mode.intensityPercent,
                subOctaveBlendPercent = mode.subOctaveBlendPercent
            )
        )
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.SubHarmonicSynthEngine.calculateHardwareOffsetMb(
            updated.subHarmonicSynthConfig.intensityPercent,
            isEnabled,
            updated.subHarmonicSynthConfig.target
        )
        NexusAudioEffects.instance.applySubHarmonicOffset(offset)
    }

    fun setSubHarmonicSynthTarget(target: SubHarmonicTarget) {
        val current = themeConfig.value
        val updated = current.copy(subHarmonicSynthConfig = current.subHarmonicSynthConfig.copy(target = target))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.SubHarmonicSynthEngine.calculateHardwareOffsetMb(
            updated.subHarmonicSynthConfig.intensityPercent,
            updated.subHarmonicSynthConfig.isEnabled,
            target
        )
        NexusAudioEffects.instance.applySubHarmonicOffset(offset)
    }

    fun setSubHarmonicSynthIntensity(intensityPercent: Int) {
        val current = themeConfig.value
        val clamped = intensityPercent.coerceIn(0, 100)
        val updated = current.copy(subHarmonicSynthConfig = current.subHarmonicSynthConfig.copy(intensityPercent = clamped))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.SubHarmonicSynthEngine.calculateHardwareOffsetMb(
            clamped,
            updated.subHarmonicSynthConfig.isEnabled,
            updated.subHarmonicSynthConfig.target
        )
        NexusAudioEffects.instance.applySubHarmonicOffset(offset)
    }

    fun setSubHarmonicSynthBlend(blendPercent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(subHarmonicSynthConfig = current.subHarmonicSynthConfig.copy(subOctaveBlendPercent = blendPercent.coerceIn(0, 100))))
    }

    fun setDeEsserEnabled(enabled: Boolean) {
        val current = themeConfig.value
        val updated = current.copy(deEsserConfig = current.deEsserConfig.copy(isEnabled = enabled))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.DeEsserEngine.calculateHardwareOffsetMb(
            -updated.deEsserConfig.maxReductionDb * 0.7f,
            enabled
        )
        NexusAudioEffects.instance.applyDeEsserOffset(offset)
    }

    fun setDeEsserMode(mode: DeEsserMode) {
        val current = themeConfig.value
        val isEnabled = mode != DeEsserMode.OFF
        val updated = current.copy(
            deEsserConfig = current.deEsserConfig.copy(
                isEnabled = isEnabled,
                mode = mode,
                centerFrequencyHz = mode.centerFrequencyHz,
                thresholdPercent = mode.thresholdPercent,
                maxReductionDb = mode.maxReductionDb
            )
        )
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.DeEsserEngine.calculateHardwareOffsetMb(
            -updated.deEsserConfig.maxReductionDb * 0.7f,
            isEnabled
        )
        NexusAudioEffects.instance.applyDeEsserOffset(offset)
    }

    fun setDeEsserCenterFrequency(centerHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(deEsserConfig = current.deEsserConfig.copy(centerFrequencyHz = centerHz.coerceIn(3000.0f, 9000.0f))))
    }

    fun setDeEsserThreshold(thresholdPercent: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(deEsserConfig = current.deEsserConfig.copy(thresholdPercent = thresholdPercent.coerceIn(10, 80))))
    }

    fun setDeEsserMaxReduction(maxReductionDb: Float) {
        val current = themeConfig.value
        val clamped = maxReductionDb.coerceIn(0.0f, 12.0f)
        val updated = current.copy(deEsserConfig = current.deEsserConfig.copy(maxReductionDb = clamped))
        updateTheme(updated)
        val offset = com.nexus.tune.core.domain.audio.DeEsserEngine.calculateHardwareOffsetMb(
            -clamped * 0.7f,
            updated.deEsserConfig.isEnabled
        )
        NexusAudioEffects.instance.applyDeEsserOffset(offset)
    }

    fun setTruePeakGuardEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(truePeakGuardConfig = current.truePeakGuardConfig.copy(isEnabled = enabled)))
    }

    fun setTruePeakGuardMode(mode: TruePeakMode) {
        val current = themeConfig.value
        val isEnabled = mode != TruePeakMode.OFF
        updateTheme(
            current.copy(
                truePeakGuardConfig = current.truePeakGuardConfig.copy(
                    isEnabled = isEnabled,
                    mode = mode,
                    ceiling = mode.ceiling,
                    oversample4xEnabled = mode.oversampling4x
                )
            )
        )
    }

    fun setTruePeakGuardCeiling(ceiling: TruePeakCeiling) {
        val current = themeConfig.value
        updateTheme(current.copy(truePeakGuardConfig = current.truePeakGuardConfig.copy(ceiling = ceiling)))
    }

    fun setTruePeakOversample4x(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(truePeakGuardConfig = current.truePeakGuardConfig.copy(oversample4xEnabled = enabled)))
    }

    fun setUltrasonicFilterEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(ultrasonicFilterConfig = current.ultrasonicFilterConfig.copy(isEnabled = enabled)))
    }

    fun setUltrasonicFilterMode(mode: UltrasonicMode) {
        val current = themeConfig.value
        val isEnabled = mode != UltrasonicMode.OFF
        updateTheme(
            current.copy(
                ultrasonicFilterConfig = current.ultrasonicFilterConfig.copy(
                    isEnabled = isEnabled,
                    mode = mode,
                    topology = mode.topology,
                    cutoffHz = mode.cutoffHz
                )
            )
        )
    }

    fun setUltrasonicFilterTopology(topology: UltrasonicTopology) {
        val current = themeConfig.value
        updateTheme(current.copy(ultrasonicFilterConfig = current.ultrasonicFilterConfig.copy(topology = topology)))
    }

    fun setUltrasonicFilterCutoff(cutoffHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(ultrasonicFilterConfig = current.ultrasonicFilterConfig.copy(cutoffHz = cutoffHz.coerceIn(16000.0f, 48000.0f))))
    }

    fun setTubeSaturationEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tubeSaturationConfig = current.tubeSaturationConfig.copy(isEnabled = enabled)))
    }

    fun setTubeSaturationProfile(profile: TubeWarmthProfile) {
        val current = themeConfig.value
        val isEnabled = profile != TubeWarmthProfile.OFF
        updateTheme(
            current.copy(
                tubeSaturationConfig = current.tubeSaturationConfig.copy(
                    isEnabled = isEnabled,
                    profile = profile,
                    topology = profile.topology,
                    drive = if (profile != TubeWarmthProfile.OFF) profile.drive else current.tubeSaturationConfig.drive,
                    bias = if (profile != TubeWarmthProfile.OFF) profile.bias else current.tubeSaturationConfig.bias
                )
            )
        )
    }

    fun setTubeSaturationTopology(topology: TubeTopology) {
        val current = themeConfig.value
        updateTheme(current.copy(tubeSaturationConfig = current.tubeSaturationConfig.copy(topology = topology)))
    }

    fun setTubeSaturationDrive(drive: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tubeSaturationConfig = current.tubeSaturationConfig.copy(drive = drive.coerceIn(0.0f, 1.0f))))
    }

    fun setTubeSaturationBias(bias: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tubeSaturationConfig = current.tubeSaturationConfig.copy(bias = bias.coerceIn(-0.5f, 0.5f))))
    }

    fun setTubeSaturationAutoMakeupGain(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tubeSaturationConfig = current.tubeSaturationConfig.copy(autoMakeupGain = enabled)))
    }

    fun setTransientShaperEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(transientShaperConfig = current.transientShaperConfig.copy(isEnabled = enabled)))
    }

    fun setTransientShaperPreset(preset: TransientPreset) {
        val current = themeConfig.value
        val isEnabled = preset != TransientPreset.OFF
        updateTheme(
            current.copy(
                transientShaperConfig = current.transientShaperConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    bandFocus = preset.focus,
                    attackBoost = if (preset != TransientPreset.OFF) preset.attack else current.transientShaperConfig.attackBoost,
                    sustainBoost = if (preset != TransientPreset.OFF) preset.sustain else current.transientShaperConfig.sustainBoost
                )
            )
        )
    }

    fun setTransientShaperFocus(focus: TransientBandFocus) {
        val current = themeConfig.value
        updateTheme(current.copy(transientShaperConfig = current.transientShaperConfig.copy(bandFocus = focus)))
    }

    fun setTransientShaperAttack(attack: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(transientShaperConfig = current.transientShaperConfig.copy(attackBoost = attack.coerceIn(-1.0f, 1.0f))))
    }

    fun setTransientShaperSustain(sustain: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(transientShaperConfig = current.transientShaperConfig.copy(sustainBoost = sustain.coerceIn(-1.0f, 1.0f))))
    }

    fun setTransientShaperAutoLimit(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(transientShaperConfig = current.transientShaperConfig.copy(autoOutputLimit = enabled)))
    }

    fun setDynamicStereoImagerEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(isEnabled = enabled)))
    }

    fun setDynamicStereoImagerPreset(preset: StereoImagerPreset) {
        val current = themeConfig.value
        val isEnabled = preset != StereoImagerPreset.OFF
        updateTheme(
            current.copy(
                dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    stereoWidth = if (preset != StereoImagerPreset.OFF) preset.stereoWidth else current.dynamicStereoImagerConfig.stereoWidth,
                    monoBassCutoffHz = if (preset != StereoImagerPreset.OFF) preset.monoBassCutoffHz else current.dynamicStereoImagerConfig.monoBassCutoffHz,
                    highBandAirSpread = if (preset != StereoImagerPreset.OFF) preset.highBandAirSpread else current.dynamicStereoImagerConfig.highBandAirSpread
                )
            )
        )
    }

    fun setDynamicStereoImagerWidth(width: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(stereoWidth = width.coerceIn(0.0f, 2.0f))))
    }

    fun setDynamicStereoImagerBassCutoff(cutoffHz: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(monoBassCutoffHz = cutoffHz.coerceIn(40.0f, 400.0f))))
    }

    fun setDynamicStereoImagerAirSpread(airSpread: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(highBandAirSpread = airSpread.coerceIn(0.0f, 1.0f))))
    }

    fun setDynamicStereoImagerMonoGuard(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(dynamicStereoImagerConfig = current.dynamicStereoImagerConfig.copy(monoGuardActive = enabled)))
    }

    fun setTapeHysteresisEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(isEnabled = enabled)))
    }

    fun setTapeHysteresisPreset(preset: TapePreset) {
        val current = themeConfig.value
        val isEnabled = preset != TapePreset.OFF
        updateTheme(
            current.copy(
                tapeHysteresisConfig = current.tapeHysteresisConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    speed = preset.speed,
                    tapeDrive = if (preset != TapePreset.OFF) preset.drive else current.tapeHysteresisConfig.tapeDrive,
                    headBumpGainDb = if (preset != TapePreset.OFF) preset.headBumpGainDb else current.tapeHysteresisConfig.headBumpGainDb,
                    wowFlutterDepth = if (preset != TapePreset.OFF) preset.wowFlutter else current.tapeHysteresisConfig.wowFlutterDepth
                )
            )
        )
    }

    fun setTapeHysteresisSpeed(speed: TapeSpeed) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(speed = speed)))
    }

    fun setTapeHysteresisDrive(drive: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(tapeDrive = drive.coerceIn(0.0f, 1.0f))))
    }

    fun setTapeHysteresisHeadBump(gainDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(headBumpGainDb = gainDb.coerceIn(0.0f, 4.5f))))
    }

    fun setTapeHysteresisWowFlutter(depth: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(wowFlutterDepth = depth.coerceIn(0.0f, 1.0f))))
    }

    fun setTapeHysteresisNoiseReduction(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(tapeHysteresisConfig = current.tapeHysteresisConfig.copy(tapeHissReduction = enabled)))
    }

    fun setMainsHumIsolatorEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(isEnabled = enabled)))
    }

    fun setMainsHumIsolatorPreset(preset: HumIsolatorPreset) {
        val current = themeConfig.value
        val isEnabled = preset != HumIsolatorPreset.OFF
        updateTheme(
            current.copy(
                mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    frequency = if (preset != HumIsolatorPreset.OFF) preset.frequency else current.mainsHumIsolatorConfig.frequency,
                    harmonicDepth = if (preset != HumIsolatorPreset.OFF) preset.harmonicDepth else current.mainsHumIsolatorConfig.harmonicDepth,
                    notchQ = if (preset != HumIsolatorPreset.OFF) preset.notchQ else current.mainsHumIsolatorConfig.notchQ,
                    notchDepthDb = if (preset != HumIsolatorPreset.OFF) preset.notchDepthDb else current.mainsHumIsolatorConfig.notchDepthDb,
                    adaptiveMasking = if (preset != HumIsolatorPreset.OFF) preset.adaptiveMasking else current.mainsHumIsolatorConfig.adaptiveMasking,
                    dcBlockerEnabled = if (preset != HumIsolatorPreset.OFF) preset.dcBlocker else current.mainsHumIsolatorConfig.dcBlockerEnabled
                )
            )
        )
    }

    fun setMainsHumIsolatorFrequency(frequency: MainsFrequency) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(frequency = frequency)))
    }

    fun setMainsHumIsolatorHarmonicDepth(depth: HumHarmonicDepth) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(harmonicDepth = depth)))
    }

    fun setMainsHumIsolatorNotchQ(q: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(notchQ = q.coerceIn(5.0f, 50.0f))))
    }

    fun setMainsHumIsolatorNotchDepth(depthDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(notchDepthDb = depthDb.coerceIn(-48.0f, 0.0f))))
    }

    fun setMainsHumIsolatorAdaptiveMasking(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(adaptiveMasking = enabled)))
    }

    fun setMainsHumIsolatorDcBlocker(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(mainsHumIsolatorConfig = current.mainsHumIsolatorConfig.copy(dcBlockerEnabled = enabled)))
    }

    fun setAcousticFeedbackEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(isEnabled = enabled)))
    }

    fun setAcousticFeedbackPreset(preset: AcousticFeedbackPreset) {
        val current = themeConfig.value
        val isEnabled = preset != AcousticFeedbackPreset.OFF
        updateTheme(
            current.copy(
                acousticFeedbackConfig = current.acousticFeedbackConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    mode = if (preset != AcousticFeedbackPreset.OFF) preset.mode else current.acousticFeedbackConfig.mode,
                    sensitivityThresholdDb = if (preset != AcousticFeedbackPreset.OFF) preset.sensitivityDb else current.acousticFeedbackConfig.sensitivityThresholdDb,
                    activeNotchesCount = if (preset != AcousticFeedbackPreset.OFF) preset.activeNotches else current.acousticFeedbackConfig.activeNotchesCount,
                    notchSharpnessQ = if (preset != AcousticFeedbackPreset.OFF) preset.notchQ else current.acousticFeedbackConfig.notchSharpnessQ,
                    suppressionDepthDb = if (preset != AcousticFeedbackPreset.OFF) preset.suppressionDepthDb else current.acousticFeedbackConfig.suppressionDepthDb,
                    fastPanicClamp = if (preset != AcousticFeedbackPreset.OFF) preset.panicClamp else current.acousticFeedbackConfig.fastPanicClamp
                )
            )
        )
    }

    fun setAcousticFeedbackMode(mode: FeedbackHuntMode) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(mode = mode)))
    }

    fun setAcousticFeedbackSensitivity(sensitivityDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(sensitivityThresholdDb = sensitivityDb.coerceIn(-45.0f, -12.0f))))
    }

    fun setAcousticFeedbackNotchesCount(count: Int) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(activeNotchesCount = count.coerceIn(1, 4))))
    }

    fun setAcousticFeedbackNotchQ(q: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(notchSharpnessQ = q.coerceIn(15.0f, 60.0f))))
    }

    fun setAcousticFeedbackSuppressionDepth(depthDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(suppressionDepthDb = depthDb.coerceIn(-36.0f, -6.0f))))
    }

    fun setAcousticFeedbackPanicClamp(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(acousticFeedbackConfig = current.acousticFeedbackConfig.copy(fastPanicClamp = enabled)))
    }

    fun setSoundstageElevationEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(isEnabled = enabled)))
    }

    fun setSoundstageElevationPreset(preset: SoundstageElevationPreset) {
        val current = themeConfig.value
        val isEnabled = preset != SoundstageElevationPreset.OFF
        updateTheme(
            current.copy(
                soundstageElevationConfig = current.soundstageElevationConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    elevationAngle = if (preset != SoundstageElevationPreset.OFF) preset.angle else current.soundstageElevationConfig.elevationAngle,
                    elevationIntensity = if (preset != SoundstageElevationPreset.OFF) preset.intensity else current.soundstageElevationConfig.elevationIntensity,
                    ceilingReflectionMs = if (preset != SoundstageElevationPreset.OFF) preset.ceilingReflectionMs else current.soundstageElevationConfig.ceilingReflectionMs,
                    pinnaNotchDepthDb = if (preset != SoundstageElevationPreset.OFF) preset.pinnaDepthDb else current.soundstageElevationConfig.pinnaNotchDepthDb,
                    diffuseCeilingSpread = if (preset != SoundstageElevationPreset.OFF) preset.diffuseSpread else current.soundstageElevationConfig.diffuseCeilingSpread
                )
            )
        )
    }

    fun setSoundstageElevationAngle(angle: ElevationAngle) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(elevationAngle = angle)))
    }

    fun setSoundstageElevationIntensity(intensity: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(elevationIntensity = intensity.coerceIn(0.0f, 1.0f))))
    }

    fun setSoundstageElevationCeilingMs(ms: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(ceilingReflectionMs = ms.coerceIn(1.0f, 10.0f))))
    }

    fun setSoundstageElevationPinnaDepth(depthDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(pinnaNotchDepthDb = depthDb.coerceIn(-10.0f, -1.0f))))
    }

    fun setSoundstageElevationDiffuseSpread(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(soundstageElevationConfig = current.soundstageElevationConfig.copy(diffuseCeilingSpread = enabled)))
    }

    fun setMonoBassAlignEnabled(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(isEnabled = enabled)))
    }

    fun setMonoBassAlignPreset(preset: MonoBassAlignPreset) {
        val current = themeConfig.value
        val isEnabled = preset != MonoBassAlignPreset.OFF
        updateTheme(
            current.copy(
                monoBassAlignConfig = current.monoBassAlignConfig.copy(
                    isEnabled = isEnabled,
                    preset = preset,
                    crossover = if (preset != MonoBassAlignPreset.OFF) preset.crossover else current.monoBassAlignConfig.crossover,
                    slope = if (preset != MonoBassAlignPreset.OFF) preset.slope else current.monoBassAlignConfig.slope,
                    autoPhaseAlign = if (preset != MonoBassAlignPreset.OFF) preset.autoPhaseAlign else current.monoBassAlignConfig.autoPhaseAlign,
                    subPunchGainDb = if (preset != MonoBassAlignPreset.OFF) preset.punchGainDb else current.monoBassAlignConfig.subPunchGainDb
                )
            )
        )
    }

    fun setMonoBassAlignCrossover(crossover: MonoBassCrossover) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(crossover = crossover)))
    }

    fun setMonoBassAlignSlope(slope: MonoBassFilterSlope) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(slope = slope)))
    }

    fun setMonoBassAlignAutoPhase(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(autoPhaseAlign = enabled)))
    }

    fun setMonoBassAlignPunchGain(gainDb: Float) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(subPunchGainDb = gainDb.coerceIn(0.0f, 4.0f))))
    }

    fun setMonoBassAlignSideSolo(enabled: Boolean) {
        val current = themeConfig.value
        updateTheme(current.copy(monoBassAlignConfig = current.monoBassAlignConfig.copy(sideSoloAudition = enabled)))
    }
}
