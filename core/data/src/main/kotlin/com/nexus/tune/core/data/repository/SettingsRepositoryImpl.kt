package com.nexus.tune.core.data.repository

import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.model.ThemeConfig
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override fun getThemeConfig(): Flow<ThemeConfig> = dataStore.themeConfig

    override suspend fun updateThemeConfig(config: ThemeConfig) {
        dataStore.updateThemeConfig(config)
    }

    override fun getPreferredContentCountry(): Flow<String> = dataStore.preferredContentCountry

    override suspend fun setPreferredContentCountry(country: String) {
        dataStore.setPreferredContentCountry(country)
    }

    override fun getPreferredQuality(): Flow<String> = dataStore.preferredQuality

    override suspend fun setPreferredQuality(quality: String) {
        dataStore.setPreferredQuality(quality)
    }

    override fun getEqualizerEnabled(): Flow<Boolean> = dataStore.equalizerEnabled

    override suspend fun setEqualizerEnabled(enabled: Boolean) {
        dataStore.setEqualizerEnabled(enabled)
    }

    override fun getEqualizerPreset(): Flow<Int> = dataStore.equalizerPreset

    override suspend fun setEqualizerPreset(preset: Int) {
        dataStore.setEqualizerPreset(preset)
    }
}

