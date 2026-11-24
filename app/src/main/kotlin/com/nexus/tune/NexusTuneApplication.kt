package com.nexus.tune

import android.app.Application
import com.nexus.tune.core.data.repository.MusicRepositoryImpl
import com.nexus.tune.core.data.repository.QueueRepositoryImpl
import com.nexus.tune.core.data.repository.SettingsRepositoryImpl
import com.nexus.tune.core.database.NexusTuneDatabase
import com.nexus.tune.core.datastore.SettingsDataStore
import com.nexus.tune.core.domain.repository.MusicRepository
import com.nexus.tune.core.domain.repository.QueueRepository
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.GetHomeFeedUseCase
import com.nexus.tune.core.domain.usecase.ManageQueueUseCase
import com.nexus.tune.core.domain.usecase.ResolveStreamUseCase
import com.nexus.tune.core.domain.usecase.SearchMusicUseCase
import com.nexus.tune.core.extractor.FakeMusicSource
import com.nexus.tune.core.extractor.MusicSource
import com.nexus.tune.core.extractor.NewPipeMusicSource
import com.nexus.tune.core.player.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NexusTuneApplication : Application(), com.nexus.tune.core.player.alarm.WakeAlarmTriggerable {

    lateinit var database: NexusTuneDatabase
        private set
    lateinit var settingsDataStore: SettingsDataStore
        private set
    lateinit var musicSource: MusicSource
        private set
    lateinit var musicRepository: MusicRepository
        private set
    lateinit var queueRepository: QueueRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    // Use cases
    lateinit var searchMusicUseCase: SearchMusicUseCase
        private set
    lateinit var getHomeFeedUseCase: GetHomeFeedUseCase
        private set
    lateinit var resolveStreamUseCase: ResolveStreamUseCase
        private set
    lateinit var manageQueueUseCase: ManageQueueUseCase
        private set
    lateinit var getListeningStatsUseCase: com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase
        private set
    lateinit var getAutoPlayRecommendationsUseCase: com.nexus.tune.core.domain.usecase.GetAutoPlayRecommendationsUseCase
        private set
    lateinit var autoPlayQueueManager: com.nexus.tune.core.domain.queue.AutoPlayQueueManager
        private set

    lateinit var playerController: PlayerController
        private set
    lateinit var abLoopManager: com.nexus.tune.core.player.ABLoopManager
        private set
    lateinit var sleepTimerManager: com.nexus.tune.core.player.SleepTimerManager
        private set
    lateinit var skipSegmentManager: com.nexus.tune.core.player.SkipSegmentManager
        private set
    lateinit var audioOutputRouteManager: com.nexus.tune.core.player.routing.AudioOutputRouteManager
        private set
    lateinit var gaplessPreloadManager: com.nexus.tune.core.domain.preload.GaplessPreloadManager
        private set
    lateinit var genreAdaptiveEqManager: com.nexus.tune.core.player.effects.GenreAdaptiveEqManager
        private set
    lateinit var safeListeningManager: com.nexus.tune.core.player.hearing.SafeListeningManager
        private set
    lateinit var backupManager: com.nexus.tune.core.data.backup.BackupManager
        private set
    lateinit var wakeAlarmManager: com.nexus.tune.core.player.alarm.WakeAlarmManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = NexusTuneDatabase.getInstance(this)
        settingsDataStore = SettingsDataStore(this)

        // Default to NewPipe with Fake fallback on errors
        musicSource = try {
            NewPipeMusicSource()
        } catch (e: Exception) {
            FakeMusicSource()
        }

        musicRepository = MusicRepositoryImpl(musicSource, database)
        queueRepository = QueueRepositoryImpl(database)
        settingsRepository = SettingsRepositoryImpl(settingsDataStore)

        searchMusicUseCase = SearchMusicUseCase(musicRepository)
        getHomeFeedUseCase = GetHomeFeedUseCase(musicRepository)
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        resolveStreamUseCase = ResolveStreamUseCase(
            musicRepository = musicRepository,
            settingsRepository = settingsRepository,
            isMeteredConnectionProvider = {
                connectivityManager?.isActiveNetworkMetered ?: false
            }
        )
        manageQueueUseCase = ManageQueueUseCase(queueRepository)
        getListeningStatsUseCase = com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase(musicRepository)
        getAutoPlayRecommendationsUseCase = com.nexus.tune.core.domain.usecase.GetAutoPlayRecommendationsUseCase(musicRepository)
        autoPlayQueueManager = com.nexus.tune.core.domain.queue.AutoPlayQueueManager(getAutoPlayRecommendationsUseCase, queueRepository)

        playerController = PlayerController(this)
        abLoopManager = com.nexus.tune.core.player.ABLoopManager(playerController)
        val sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        sleepTimerManager = com.nexus.tune.core.player.SleepTimerManager(
            playerController = playerController,
            sensorManager = sensorManager
        )
        skipSegmentManager = com.nexus.tune.core.player.SkipSegmentManager(playerController)
        audioOutputRouteManager = com.nexus.tune.core.player.routing.AudioOutputRouteManager(this)

        val audioPreloader = object : com.nexus.tune.core.domain.preload.AudioPreloader {
            override fun preloadTrack(track: com.nexus.tune.core.model.Track, resolvedStreamUrl: String) {
                playerController.preloadNextTrack(track, resolvedStreamUrl)
            }
            override fun clearPreload() {
                playerController.clearUpcomingPreload()
            }
        }
        gaplessPreloadManager = com.nexus.tune.core.domain.preload.GaplessPreloadManager(
            audioPreloader = audioPreloader,
            queueRepository = queueRepository,
            resolveStreamUseCase = resolveStreamUseCase
        )
        genreAdaptiveEqManager = com.nexus.tune.core.player.effects.GenreAdaptiveEqManager()
        safeListeningManager = com.nexus.tune.core.player.hearing.SafeListeningManager()
        backupManager = com.nexus.tune.core.data.backup.BackupManager(database, settingsDataStore)

        wakeAlarmManager = com.nexus.tune.core.player.alarm.WakeAlarmManager(
            context = this,
            playerController = playerController,
            ambientSynthesizer = com.nexus.tune.core.player.ambient.AmbientSoundSynthesizer.instance
        )
        val appScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob())
        appScope.launch {
            settingsDataStore.themeConfig.collect { config ->
                wakeAlarmManager.scheduleAlarm(config.wakeAlarmConfig)
            }
        }
    }

    override fun triggerWakeAlarm() {
        CoroutineScope(Dispatchers.Main).launch {
            val config = settingsDataStore.themeConfig.first().wakeAlarmConfig
            wakeAlarmManager.triggerWakeAlarm(config)
        }
    }

    companion object {
        lateinit var instance: NexusTuneApplication
            private set
    }
}

