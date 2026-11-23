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

    lateinit var playerController: PlayerController
        private set
    lateinit var sleepTimerManager: com.nexus.tune.core.player.SleepTimerManager
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

        playerController = PlayerController(this)
        sleepTimerManager = com.nexus.tune.core.player.SleepTimerManager(playerController)
    }

    companion object {
        lateinit var instance: NexusTuneApplication
            private set
    }
}

