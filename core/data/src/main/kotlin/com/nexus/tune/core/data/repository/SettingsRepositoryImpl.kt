package com.nexus.tune.core.data.repository

import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.model.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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

    override fun getListeningStreak(): Flow<com.nexus.tune.core.model.ListeningStreak> = dataStore.listeningStreak

    override suspend fun recordActiveDay(epochDay: Long) {
        dataStore.recordActiveDay(epochDay)
    }

    override fun getCustomEqProfiles(): Flow<List<com.nexus.tune.core.model.CustomEqProfile>> =
        dataStore.customEqProfilesRaw.map { raw ->
            com.nexus.tune.core.domain.audio.EqCurveCalculator.deserializeProfilesList(raw)
        }

    override suspend fun saveCustomEqProfile(profile: com.nexus.tune.core.model.CustomEqProfile) {
        val raw = dataStore.customEqProfilesRaw.first()
        val existing = com.nexus.tune.core.domain.audio.EqCurveCalculator.deserializeProfilesList(raw)
            .filter { it.id != profile.id }
        val updated = listOf(profile) + existing
        dataStore.saveCustomEqProfilesRaw(com.nexus.tune.core.domain.audio.EqCurveCalculator.serializeProfilesList(updated))
    }

    override suspend fun deleteCustomEqProfile(profileId: String) {
        val raw = dataStore.customEqProfilesRaw.first()
        val existing = com.nexus.tune.core.domain.audio.EqCurveCalculator.deserializeProfilesList(raw)
            .filter { it.id != profileId }
        dataStore.saveCustomEqProfilesRaw(com.nexus.tune.core.domain.audio.EqCurveCalculator.serializeProfilesList(existing))
    }

    override fun getDjFxConfig(): Flow<com.nexus.tune.core.model.DjFxConfig> = dataStore.djFxConfig

    override suspend fun updateDjFxConfig(config: com.nexus.tune.core.model.DjFxConfig) {
        dataStore.updateDjFxConfig(config)
    }

    override fun getDjRememberAcrossTracks(): Flow<Boolean> = dataStore.djRememberAcrossTracks

    override suspend fun setDjRememberAcrossTracks(remember: Boolean) {
        dataStore.setDjRememberAcrossTracks(remember)
    }
}

