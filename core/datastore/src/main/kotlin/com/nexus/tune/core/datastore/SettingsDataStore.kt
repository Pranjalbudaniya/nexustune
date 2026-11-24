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

    suspend fun clearRecentSearches() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.RECENT_SEARCHES)
        }
    }
}

