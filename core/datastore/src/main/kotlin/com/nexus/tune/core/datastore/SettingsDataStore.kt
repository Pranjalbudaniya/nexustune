package com.nexus.tune.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nexus.tune.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexus_tune_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val SEED_MODE = stringPreferencesKey("theme_seed_mode")
        val CUSTOM_SEED_HEX = stringPreferencesKey("theme_custom_seed_hex")
        val IS_DARK_MODE = booleanPreferencesKey("theme_is_dark_mode")
        val IS_AMOLED = booleanPreferencesKey("theme_is_amoled")
        val SHAPE_PRESET = stringPreferencesKey("theme_shape_preset")
        val PLAYER_LAYOUT = stringPreferencesKey("theme_player_layout")
        val MINI_PLAYER_STYLE = stringPreferencesKey("theme_mini_player_style")
        val REFRESH_RATE = intPreferencesKey("display_refresh_rate")
        val CROSSFADE_SECONDS = intPreferencesKey("theme_crossfade_seconds")
        val CONTENT_COUNTRY = stringPreferencesKey("content_country")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val SPATIAL_AUDIO_ENABLED = booleanPreferencesKey("spatial_audio_enabled")
        val SPATIAL_AUDIO_STAGE = stringPreferencesKey("spatial_audio_stage")
        val SPATIAL_AUDIO_STRENGTH = intPreferencesKey("spatial_audio_strength")
        val CROSSFADE_CURVE = stringPreferencesKey("crossfade_curve")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
        val SHAKE_TO_EXTEND = booleanPreferencesKey("sleep_timer_shake_to_extend")
        val SHAKE_EXTEND_MINUTES = intPreferencesKey("sleep_timer_shake_extend_minutes")
        val AMBIENT_SOUND_TYPE = stringPreferencesKey("ambient_sound_type")
        val AMBIENT_SOUND_VOLUME = floatPreferencesKey("ambient_sound_volume")
        val CUSTOM_EQ_BANDS = stringPreferencesKey("custom_eq_bands_csv")
        val BEDTIME_HANDOFF_ENABLED = booleanPreferencesKey("bedtime_handoff_enabled")
        val BEDTIME_HANDOFF_SOUND_TYPE = stringPreferencesKey("bedtime_handoff_sound_type")
        val BEDTIME_HANDOFF_VOLUME = floatPreferencesKey("bedtime_handoff_volume")
        val BEDTIME_HANDOFF_DURATION = intPreferencesKey("bedtime_handoff_duration_minutes")
        val SEEKBAR_STYLE = stringPreferencesKey("theme_seekbar_style")
        val RESET_PITCH_ON_TRACK_CHANGE = booleanPreferencesKey("reset_pitch_on_track_change")
        val SMART_SHUFFLE_ENABLED = booleanPreferencesKey("smart_shuffle_enabled")
        val WAKE_ALARM_ENABLED = booleanPreferencesKey("wake_alarm_enabled")
        val WAKE_ALARM_HOUR = intPreferencesKey("wake_alarm_hour")
        val WAKE_ALARM_MINUTE = intPreferencesKey("wake_alarm_minute")
        val WAKE_ALARM_FADE_SECONDS = intPreferencesKey("wake_alarm_fade_seconds")
        val WAKE_ALARM_SOURCE = stringPreferencesKey("wake_alarm_source")
        val WAKE_ALARM_VOLUME = floatPreferencesKey("wake_alarm_volume")
        val LOUDNESS_MODE = stringPreferencesKey("loudness_normalization_mode")
        val TARGET_LUFS = floatPreferencesKey("loudness_target_lufs")
        val PRE_AMP_GAIN_DB = floatPreferencesKey("loudness_preamp_gain_db")
        val STREAK_CURRENT_DAYS = intPreferencesKey("streak_current_days")
        val STREAK_LONGEST_DAYS = intPreferencesKey("streak_longest_days")
        val STREAK_LAST_ACTIVE_EPOCH_DAY = longPreferencesKey("streak_last_active_epoch_day")
        val CUSTOM_EQ_PROFILES = stringPreferencesKey("custom_eq_profiles_serialized")
        val DJ_PRESET = stringPreferencesKey("dj_preset")
        val DJ_SPEED = floatPreferencesKey("dj_speed")
        val DJ_SEMITONES = intPreferencesKey("dj_semitones")
        val DJ_SOUNDSTAGE_WIDTH = floatPreferencesKey("dj_soundstage_width")
        val DJ_LOFI_FILTER = booleanPreferencesKey("dj_lofi_filter")
        val DJ_BASS_BOOST = intPreferencesKey("dj_bass_boost")
        val DJ_REMEMBER_ACROSS_TRACKS = booleanPreferencesKey("dj_remember_across_tracks")
        val CUSTOM_SMART_PLAYLISTS = stringPreferencesKey("custom_smart_playlists_json")
        val VISUALIZER_MODE = stringPreferencesKey("theme_visualizer_mode")
        val VISUALIZER_PALETTE = stringPreferencesKey("theme_visualizer_palette")
        val AUTO_STASH_ENABLED = booleanPreferencesKey("auto_stash_enabled")
        val AUTO_STASH_BUDGET_MB = intPreferencesKey("auto_stash_budget_mb")
        val AUTO_STASH_STRATEGY = stringPreferencesKey("auto_stash_strategy")
        val AUTO_STASH_WIFI_ONLY = booleanPreferencesKey("auto_stash_wifi_only")
        val HEADPHONE_CORRECTION_ENABLED = booleanPreferencesKey("headphone_correction_enabled")
        val HEADPHONE_TARGET_CURVE = stringPreferencesKey("headphone_target_curve")
        val HEADPHONE_CROSSFEED_ENABLED = booleanPreferencesKey("headphone_crossfeed_enabled")
        val STEREO_WIDTH_ENABLED = booleanPreferencesKey("stereo_width_enabled")
        val STEREO_WIDTH_MODE = stringPreferencesKey("stereo_width_mode")
        val STEREO_WIDTH_PERCENT = intPreferencesKey("stereo_width_percent")
        val STEREO_HAAS_DELAY_MS = floatPreferencesKey("stereo_haas_delay_ms")
        val STEREO_MID_LEVEL_DB = floatPreferencesKey("stereo_mid_level_db")
        val STEREO_SIDE_LEVEL_DB = floatPreferencesKey("stereo_side_level_db")
        val SLEEP_DRIFT_PROFILE = stringPreferencesKey("sleep_drift_profile")
        val DITHER_MODE = stringPreferencesKey("dither_mode")
        val DYNAMIC_LOUDNESS_ENABLED = booleanPreferencesKey("dynamic_loudness_enabled")
        val DYNAMIC_LOUDNESS_MODE = stringPreferencesKey("dynamic_loudness_mode")
        val DYNAMIC_LOUDNESS_INTENSITY = intPreferencesKey("dynamic_loudness_intensity")
        val BEAT_CROSSFADE_ENABLED = booleanPreferencesKey("beat_crossfade_enabled")
        val BEAT_CROSSFADE_MODE = stringPreferencesKey("beat_crossfade_mode")
        val BEAT_CROSSFADE_MAX_BEND = intPreferencesKey("beat_crossfade_max_bend")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data.map { prefs ->
        ThemeConfig(
            seedMode = ThemeSeedMode.valueOf(prefs[PreferencesKeys.SEED_MODE] ?: ThemeSeedMode.DYNAMIC.name),
            customSeedColorHex = prefs[PreferencesKeys.CUSTOM_SEED_HEX] ?: "#6750A4",
            isDarkMode = prefs[PreferencesKeys.IS_DARK_MODE] ?: true,
            isAmoled = prefs[PreferencesKeys.IS_AMOLED] ?: false,
            shapePreset = ShapePreset.valueOf(prefs[PreferencesKeys.SHAPE_PRESET] ?: ShapePreset.SOFT_ROUNDED.name),
            playerLayout = PlayerLayout.valueOf(prefs[PreferencesKeys.PLAYER_LAYOUT] ?: PlayerLayout.ART_FORWARD.name),
            miniPlayerStyle = MiniPlayerStyle.valueOf(prefs[PreferencesKeys.MINI_PLAYER_STYLE] ?: MiniPlayerStyle.DOCKED_BAR.name),
            refreshRatePreferred = prefs[PreferencesKeys.REFRESH_RATE] ?: 120,
            crossfadeSeconds = prefs[PreferencesKeys.CROSSFADE_SECONDS] ?: 0,
            spatialAudioEnabled = prefs[PreferencesKeys.SPATIAL_AUDIO_ENABLED] ?: false,
            spatialAudioStage = runCatching {
                SpatialAudioStage.valueOf(prefs[PreferencesKeys.SPATIAL_AUDIO_STAGE] ?: SpatialAudioStage.OFF.name)
            }.getOrDefault(SpatialAudioStage.OFF),
            spatialAudioStrength = prefs[PreferencesKeys.SPATIAL_AUDIO_STRENGTH] ?: 500,
            crossfadeCurve = runCatching {
                CrossfadeCurve.valueOf(prefs[PreferencesKeys.CROSSFADE_CURVE] ?: CrossfadeCurve.EQUAL_POWER.name)
            }.getOrDefault(CrossfadeCurve.EQUAL_POWER),
            shakeToExtendSleepTimerEnabled = prefs[PreferencesKeys.SHAKE_TO_EXTEND] ?: false,
            shakeExtendMinutes = prefs[PreferencesKeys.SHAKE_EXTEND_MINUTES] ?: 10,
            ambientSoundType = runCatching {
                AmbientSoundType.valueOf(prefs[PreferencesKeys.AMBIENT_SOUND_TYPE] ?: AmbientSoundType.OFF.name)
            }.getOrDefault(AmbientSoundType.OFF),
            ambientSoundVolume = prefs[PreferencesKeys.AMBIENT_SOUND_VOLUME] ?: 0.5f,
            customEqBands = prefs[PreferencesKeys.CUSTOM_EQ_BANDS]?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.takeIf { it.size == 5 } ?: listOf(0, 0, 0, 0, 0),
            bedtimeHandoffEnabled = prefs[PreferencesKeys.BEDTIME_HANDOFF_ENABLED] ?: false,
            bedtimeHandoffSoundType = runCatching {
                AmbientSoundType.valueOf(prefs[PreferencesKeys.BEDTIME_HANDOFF_SOUND_TYPE] ?: AmbientSoundType.BROWN_NOISE.name)
            }.getOrDefault(AmbientSoundType.BROWN_NOISE),
            bedtimeHandoffVolume = prefs[PreferencesKeys.BEDTIME_HANDOFF_VOLUME] ?: 0.3f,
            bedtimeHandoffDurationMinutes = prefs[PreferencesKeys.BEDTIME_HANDOFF_DURATION] ?: 60,
            seekbarStyle = runCatching {
                SeekbarStyle.valueOf(prefs[PreferencesKeys.SEEKBAR_STYLE] ?: SeekbarStyle.WAVEFORM.name)
            }.getOrDefault(SeekbarStyle.WAVEFORM),
            resetPitchOnTrackChange = prefs[PreferencesKeys.RESET_PITCH_ON_TRACK_CHANGE] ?: true,
            smartShuffleEnabled = prefs[PreferencesKeys.SMART_SHUFFLE_ENABLED] ?: true,
            wakeAlarmConfig = WakeAlarmConfig(
                isEnabled = prefs[PreferencesKeys.WAKE_ALARM_ENABLED] ?: false,
                hour = prefs[PreferencesKeys.WAKE_ALARM_HOUR] ?: 7,
                minute = prefs[PreferencesKeys.WAKE_ALARM_MINUTE] ?: 0,
                fadeInSeconds = prefs[PreferencesKeys.WAKE_ALARM_FADE_SECONDS] ?: 120,
                source = runCatching {
                    WakeAlarmSource.valueOf(prefs[PreferencesKeys.WAKE_ALARM_SOURCE] ?: WakeAlarmSource.LIKED_SONGS.name)
                }.getOrDefault(WakeAlarmSource.LIKED_SONGS),
                targetVolume = prefs[PreferencesKeys.WAKE_ALARM_VOLUME] ?: 0.8f
            ),
            loudnessConfig = LoudnessConfig(
                mode = runCatching {
                    LoudnessNormalizationMode.valueOf(prefs[PreferencesKeys.LOUDNESS_MODE] ?: LoudnessNormalizationMode.REPLAYGAIN_TRACK.name)
                }.getOrDefault(LoudnessNormalizationMode.REPLAYGAIN_TRACK),
                targetLufs = prefs[PreferencesKeys.TARGET_LUFS] ?: -14.0f,
                preAmpGainDb = prefs[PreferencesKeys.PRE_AMP_GAIN_DB] ?: 0.0f
            ),
            djFxConfig = DjFxConfig(
                preset = runCatching {
                    DjPreset.valueOf(prefs[PreferencesKeys.DJ_PRESET] ?: DjPreset.NORMAL.name)
                }.getOrDefault(DjPreset.NORMAL),
                speed = prefs[PreferencesKeys.DJ_SPEED] ?: 1.0f,
                semitones = prefs[PreferencesKeys.DJ_SEMITONES] ?: 0,
                soundstageWidth = prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] ?: 1.0f,
                loFiFilterEnabled = prefs[PreferencesKeys.DJ_LOFI_FILTER] ?: false,
                bassBoostDb = prefs[PreferencesKeys.DJ_BASS_BOOST] ?: 0
            ),
            djRememberAcrossTracks = prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] ?: false,
            visualizerMode = runCatching {
                VisualizerMode.valueOf(prefs[PreferencesKeys.VISUALIZER_MODE] ?: VisualizerMode.SPECTRUM_BARS.name)
            }.getOrDefault(VisualizerMode.SPECTRUM_BARS),
            visualizerPalette = runCatching {
                VisualizerPalette.valueOf(prefs[PreferencesKeys.VISUALIZER_PALETTE] ?: VisualizerPalette.DYNAMIC.name)
            }.getOrDefault(VisualizerPalette.DYNAMIC),
            autoStashConfig = AutoStashConfig(
                isEnabled = prefs[PreferencesKeys.AUTO_STASH_ENABLED] ?: false,
                storageBudgetMb = prefs[PreferencesKeys.AUTO_STASH_BUDGET_MB] ?: 1000,
                strategy = runCatching {
                    AutoStashStrategy.valueOf(prefs[PreferencesKeys.AUTO_STASH_STRATEGY] ?: AutoStashStrategy.BALANCED_HYBRID.name)
                }.getOrDefault(AutoStashStrategy.BALANCED_HYBRID),
                wifiOnly = prefs[PreferencesKeys.AUTO_STASH_WIFI_ONLY] ?: true
            ),
            headphoneCorrectionConfig = HeadphoneCorrectionConfig(
                isEnabled = prefs[PreferencesKeys.HEADPHONE_CORRECTION_ENABLED] ?: false,
                targetCurve = runCatching {
                    HeadphoneTargetCurve.valueOf(prefs[PreferencesKeys.HEADPHONE_TARGET_CURVE] ?: HeadphoneTargetCurve.HARMAN_OVER_EAR.name)
                }.getOrDefault(HeadphoneTargetCurve.HARMAN_OVER_EAR),
                crossfeedEnabled = prefs[PreferencesKeys.HEADPHONE_CROSSFEED_ENABLED] ?: true
            ),
            stereoWidthConfig = StereoWidthConfig(
                isEnabled = prefs[PreferencesKeys.STEREO_WIDTH_ENABLED] ?: false,
                mode = runCatching {
                    StereoWidthMode.valueOf(prefs[PreferencesKeys.STEREO_WIDTH_MODE] ?: StereoWidthMode.NATURAL.name)
                }.getOrDefault(StereoWidthMode.NATURAL),
                customWidthPercent = prefs[PreferencesKeys.STEREO_WIDTH_PERCENT] ?: 100,
                haasDelayMs = prefs[PreferencesKeys.STEREO_HAAS_DELAY_MS] ?: 0.0f,
                midLevelDb = prefs[PreferencesKeys.STEREO_MID_LEVEL_DB] ?: 0.0f,
                sideLevelDb = prefs[PreferencesKeys.STEREO_SIDE_LEVEL_DB] ?: 0.0f
            ),
            sleepDriftProfile = runCatching {
                SleepDriftProfile.valueOf(prefs[PreferencesKeys.SLEEP_DRIFT_PROFILE] ?: SleepDriftProfile.OFF.name)
            }.getOrDefault(SleepDriftProfile.OFF),
            ditherMode = runCatching {
                DitherMode.valueOf(prefs[PreferencesKeys.DITHER_MODE] ?: DitherMode.TRIANGULAR_TPDF.name)
            }.getOrDefault(DitherMode.TRIANGULAR_TPDF),
            dynamicLoudnessConfig = DynamicLoudnessConfig(
                isEnabled = prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] ?: false,
                mode = runCatching {
                    DynamicLoudnessMode.valueOf(prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] ?: DynamicLoudnessMode.ISO_226_REFERENCE.name)
                }.getOrDefault(DynamicLoudnessMode.ISO_226_REFERENCE),
                intensityPercent = prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] ?: 100
            ),
            beatCrossfadeConfig = BeatCrossfadeConfig(
                isEnabled = prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] ?: false,
                mode = runCatching {
                    BeatCrossfadeMode.valueOf(prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] ?: BeatCrossfadeMode.SMART_BEAT_SYNC.name)
                }.getOrDefault(BeatCrossfadeMode.SMART_BEAT_SYNC),
                maxTempoBendPercent = prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] ?: 6
            )
        )
    }

    suspend fun updateThemeConfig(config: ThemeConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEED_MODE] = config.seedMode.name
            prefs[PreferencesKeys.CUSTOM_SEED_HEX] = config.customSeedColorHex
            prefs[PreferencesKeys.IS_DARK_MODE] = config.isDarkMode
            prefs[PreferencesKeys.IS_AMOLED] = config.isAmoled
            prefs[PreferencesKeys.SHAPE_PRESET] = config.shapePreset.name
            prefs[PreferencesKeys.PLAYER_LAYOUT] = config.playerLayout.name
            prefs[PreferencesKeys.MINI_PLAYER_STYLE] = config.miniPlayerStyle.name
            prefs[PreferencesKeys.REFRESH_RATE] = config.refreshRatePreferred
            prefs[PreferencesKeys.CROSSFADE_SECONDS] = config.crossfadeSeconds
            prefs[PreferencesKeys.SPATIAL_AUDIO_ENABLED] = config.spatialAudioEnabled
            prefs[PreferencesKeys.SPATIAL_AUDIO_STAGE] = config.spatialAudioStage.name
            prefs[PreferencesKeys.SPATIAL_AUDIO_STRENGTH] = config.spatialAudioStrength
            prefs[PreferencesKeys.CROSSFADE_CURVE] = config.crossfadeCurve.name
            prefs[PreferencesKeys.SHAKE_TO_EXTEND] = config.shakeToExtendSleepTimerEnabled
            prefs[PreferencesKeys.SHAKE_EXTEND_MINUTES] = config.shakeExtendMinutes
            prefs[PreferencesKeys.AMBIENT_SOUND_TYPE] = config.ambientSoundType.name
            prefs[PreferencesKeys.AMBIENT_SOUND_VOLUME] = config.ambientSoundVolume
            prefs[PreferencesKeys.CUSTOM_EQ_BANDS] = config.customEqBands.joinToString(",")
            prefs[PreferencesKeys.BEDTIME_HANDOFF_ENABLED] = config.bedtimeHandoffEnabled
            prefs[PreferencesKeys.BEDTIME_HANDOFF_SOUND_TYPE] = config.bedtimeHandoffSoundType.name
            prefs[PreferencesKeys.BEDTIME_HANDOFF_VOLUME] = config.bedtimeHandoffVolume
            prefs[PreferencesKeys.BEDTIME_HANDOFF_DURATION] = config.bedtimeHandoffDurationMinutes
            prefs[PreferencesKeys.SEEKBAR_STYLE] = config.seekbarStyle.name
            prefs[PreferencesKeys.RESET_PITCH_ON_TRACK_CHANGE] = config.resetPitchOnTrackChange
            prefs[PreferencesKeys.SMART_SHUFFLE_ENABLED] = config.smartShuffleEnabled
            prefs[PreferencesKeys.WAKE_ALARM_ENABLED] = config.wakeAlarmConfig.isEnabled
            prefs[PreferencesKeys.WAKE_ALARM_HOUR] = config.wakeAlarmConfig.hour
            prefs[PreferencesKeys.WAKE_ALARM_MINUTE] = config.wakeAlarmConfig.minute
            prefs[PreferencesKeys.WAKE_ALARM_FADE_SECONDS] = config.wakeAlarmConfig.fadeInSeconds
            prefs[PreferencesKeys.WAKE_ALARM_SOURCE] = config.wakeAlarmConfig.source.name
            prefs[PreferencesKeys.WAKE_ALARM_VOLUME] = config.wakeAlarmConfig.targetVolume
            prefs[PreferencesKeys.LOUDNESS_MODE] = config.loudnessConfig.mode.name
            prefs[PreferencesKeys.TARGET_LUFS] = config.loudnessConfig.targetLufs
            prefs[PreferencesKeys.PRE_AMP_GAIN_DB] = config.loudnessConfig.preAmpGainDb
            prefs[PreferencesKeys.DJ_PRESET] = config.djFxConfig.preset.name
            prefs[PreferencesKeys.DJ_SPEED] = config.djFxConfig.speed
            prefs[PreferencesKeys.DJ_SEMITONES] = config.djFxConfig.semitones
            prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] = config.djFxConfig.soundstageWidth
            prefs[PreferencesKeys.DJ_LOFI_FILTER] = config.djFxConfig.loFiFilterEnabled
            prefs[PreferencesKeys.DJ_BASS_BOOST] = config.djFxConfig.bassBoostDb
            prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] = config.djRememberAcrossTracks
            prefs[PreferencesKeys.VISUALIZER_MODE] = config.visualizerMode.name
            prefs[PreferencesKeys.VISUALIZER_PALETTE] = config.visualizerPalette.name
            prefs[PreferencesKeys.AUTO_STASH_ENABLED] = config.autoStashConfig.isEnabled
            prefs[PreferencesKeys.AUTO_STASH_BUDGET_MB] = config.autoStashConfig.storageBudgetMb
            prefs[PreferencesKeys.AUTO_STASH_STRATEGY] = config.autoStashConfig.strategy.name
            prefs[PreferencesKeys.AUTO_STASH_WIFI_ONLY] = config.autoStashConfig.wifiOnly
            prefs[PreferencesKeys.HEADPHONE_CORRECTION_ENABLED] = config.headphoneCorrectionConfig.isEnabled
            prefs[PreferencesKeys.HEADPHONE_TARGET_CURVE] = config.headphoneCorrectionConfig.targetCurve.name
            prefs[PreferencesKeys.HEADPHONE_CROSSFEED_ENABLED] = config.headphoneCorrectionConfig.crossfeedEnabled
            prefs[PreferencesKeys.STEREO_WIDTH_ENABLED] = config.stereoWidthConfig.isEnabled
            prefs[PreferencesKeys.STEREO_WIDTH_MODE] = config.stereoWidthConfig.mode.name
            prefs[PreferencesKeys.STEREO_WIDTH_PERCENT] = config.stereoWidthConfig.customWidthPercent
            prefs[PreferencesKeys.STEREO_HAAS_DELAY_MS] = config.stereoWidthConfig.haasDelayMs
            prefs[PreferencesKeys.STEREO_MID_LEVEL_DB] = config.stereoWidthConfig.midLevelDb
            prefs[PreferencesKeys.STEREO_SIDE_LEVEL_DB] = config.stereoWidthConfig.sideLevelDb
            prefs[PreferencesKeys.SLEEP_DRIFT_PROFILE] = config.sleepDriftProfile.name
            prefs[PreferencesKeys.DITHER_MODE] = config.ditherMode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] = config.dynamicLoudnessConfig.isEnabled
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] = config.dynamicLoudnessConfig.mode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] = config.dynamicLoudnessConfig.intensityPercent
            prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] = config.beatCrossfadeConfig.isEnabled
            prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] = config.beatCrossfadeConfig.mode.name
            prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] = config.beatCrossfadeConfig.maxTempoBendPercent
        }
    }

    suspend fun setDitherMode(mode: DitherMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DITHER_MODE] = mode.name
        }
    }

    suspend fun updateDynamicLoudnessConfig(config: DynamicLoudnessConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_MODE] = config.mode.name
            prefs[PreferencesKeys.DYNAMIC_LOUDNESS_INTENSITY] = config.intensityPercent
        }
    }

    suspend fun updateBeatCrossfadeConfig(config: BeatCrossfadeConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BEAT_CROSSFADE_ENABLED] = config.isEnabled
            prefs[PreferencesKeys.BEAT_CROSSFADE_MODE] = config.mode.name
            prefs[PreferencesKeys.BEAT_CROSSFADE_MAX_BEND] = config.maxTempoBendPercent
        }
    }

    val preferredContentCountry: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CONTENT_COUNTRY] ?: "US"
    }

    suspend fun setPreferredContentCountry(country: String) {
        context.dataStore.edit { it[PreferencesKeys.CONTENT_COUNTRY] = country }
    }

    val preferredQuality: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUDIO_QUALITY] ?: "HIGH"
    }

    suspend fun setPreferredQuality(quality: String) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_QUALITY] = quality }
    }

    val equalizerEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.EQUALIZER_ENABLED] ?: true
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_ENABLED] = enabled }
    }

    val equalizerPreset: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.EQUALIZER_PRESET] ?: 0
    }

    suspend fun setEqualizerPreset(preset: Int) {
        context.dataStore.edit { it[PreferencesKeys.EQUALIZER_PRESET] = preset }
    }

    val recentSearches: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[PreferencesKeys.RECENT_SEARCHES] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("|||")
    }

    suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val existing = (prefs[PreferencesKeys.RECENT_SEARCHES] ?: "")
                .split("|||")
                .filter { it.isNotBlank() && it != trimmed }
            val updated = (listOf(trimmed) + existing).take(10)
            prefs[PreferencesKeys.RECENT_SEARCHES] = updated.joinToString("|||")
        }
    }

    suspend fun removeRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[PreferencesKeys.RECENT_SEARCHES] ?: "")
                .split("|||")
                .filter { it.isNotBlank() && it != query }
            prefs[PreferencesKeys.RECENT_SEARCHES] = existing.joinToString("|||")
        }
    }

    val listeningStreak: Flow<ListeningStreak> = context.dataStore.data.map { prefs ->
        ListeningStreak(
            currentStreakDays = prefs[PreferencesKeys.STREAK_CURRENT_DAYS] ?: 0,
            longestStreakDays = prefs[PreferencesKeys.STREAK_LONGEST_DAYS] ?: 0,
            lastActiveDateEpochDay = prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] ?: 0L
        )
    }

    suspend fun recordActiveDay(epochDay: Long) {
        context.dataStore.edit { prefs ->
            val lastDay = prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] ?: 0L
            val currentStreak = prefs[PreferencesKeys.STREAK_CURRENT_DAYS] ?: 0
            val longestStreak = prefs[PreferencesKeys.STREAK_LONGEST_DAYS] ?: 0

            if (epochDay == lastDay) {
                return@edit
            }

            val newCurrent = if (epochDay == lastDay + 1L) {
                currentStreak + 1
            } else {
                1
            }
            val newLongest = maxOf(longestStreak, newCurrent)

            prefs[PreferencesKeys.STREAK_CURRENT_DAYS] = newCurrent
            prefs[PreferencesKeys.STREAK_LONGEST_DAYS] = newLongest
            prefs[PreferencesKeys.STREAK_LAST_ACTIVE_EPOCH_DAY] = epochDay
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.RECENT_SEARCHES)
        }
    }

    val customEqProfilesRaw: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CUSTOM_EQ_PROFILES] ?: ""
    }

    suspend fun saveCustomEqProfilesRaw(raw: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_EQ_PROFILES] = raw
        }
    }

    val djFxConfig: Flow<DjFxConfig> = context.dataStore.data.map { prefs ->
        DjFxConfig(
            preset = runCatching {
                DjPreset.valueOf(prefs[PreferencesKeys.DJ_PRESET] ?: DjPreset.NORMAL.name)
            }.getOrDefault(DjPreset.NORMAL),
            speed = prefs[PreferencesKeys.DJ_SPEED] ?: 1.0f,
            semitones = prefs[PreferencesKeys.DJ_SEMITONES] ?: 0,
            soundstageWidth = prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] ?: 1.0f,
            loFiFilterEnabled = prefs[PreferencesKeys.DJ_LOFI_FILTER] ?: false,
            bassBoostDb = prefs[PreferencesKeys.DJ_BASS_BOOST] ?: 0
        )
    }

    suspend fun updateDjFxConfig(config: DjFxConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DJ_PRESET] = config.preset.name
            prefs[PreferencesKeys.DJ_SPEED] = config.speed
            prefs[PreferencesKeys.DJ_SEMITONES] = config.semitones
            prefs[PreferencesKeys.DJ_SOUNDSTAGE_WIDTH] = config.soundstageWidth
            prefs[PreferencesKeys.DJ_LOFI_FILTER] = config.loFiFilterEnabled
            prefs[PreferencesKeys.DJ_BASS_BOOST] = config.bassBoostDb
        }
    }

    val djRememberAcrossTracks: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] ?: false
    }

    suspend fun setDjRememberAcrossTracks(remember: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DJ_REMEMBER_ACROSS_TRACKS] = remember
        }
    }

    val customSmartPlaylistsRaw: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CUSTOM_SMART_PLAYLISTS] ?: ""
    }

    suspend fun saveCustomSmartPlaylistsRaw(raw: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_SMART_PLAYLISTS] = raw
        }
    }
}

