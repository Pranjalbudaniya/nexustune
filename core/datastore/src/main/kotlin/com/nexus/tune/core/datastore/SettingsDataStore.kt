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
            crossfadeSeconds = prefs[PreferencesKeys.CROSSFADE_SECONDS] ?: 0
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
}

