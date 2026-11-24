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

class NexusTuneApplication : Application() {

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
        resolveStreamUseCase = ResolveStreamUseCase(musicRepository)
        manageQueueUseCase = ManageQueueUseCase(queueRepository)
        getListeningStatsUseCase = com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase(musicRepository)
        getAutoPlayRecommendationsUseCase = com.nexus.tune.core.domain.usecase.GetAutoPlayRecommendationsUseCase(musicRepository)
        autoPlayQueueManager = com.nexus.tune.core.domain.queue.AutoPlayQueueManager(getAutoPlayRecommendationsUseCase, queueRepository)

        playerController = PlayerController(this)
        sleepTimerManager = com.nexus.tune.core.player.SleepTimerManager(playerController)
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
    }

    companion object {
        lateinit var instance: NexusTuneApplication
            private set
    }
}

