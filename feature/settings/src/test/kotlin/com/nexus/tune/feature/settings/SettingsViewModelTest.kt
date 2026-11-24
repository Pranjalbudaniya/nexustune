package com.nexus.tune.feature.settings

import app.cash.turbine.test
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase
import com.nexus.tune.core.model.*
import com.nexus.tune.core.player.SleepTimerManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val getListeningStatsUseCase = mockk<GetListeningStatsUseCase>(relaxed = true)
    private val sleepTimerManager = mockk<SleepTimerManager>(relaxed = true)
    private val wakeAlarmManager = mockk<com.nexus.tune.core.player.alarm.WakeAlarmManager>(relaxed = true)
    private val themeFlow = MutableStateFlow(ThemeConfig())

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getThemeConfig() } returns themeFlow
        every { settingsRepository.getPreferredQuality() } returns flowOf("AUTO")
        every { getListeningStatsUseCase() } returns flowOf(
            ListeningStats(
                totalPlays = 42,
                topArtists = listOf("Artist A", "Artist B")
            )
        )
        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            getListeningStatsUseCase = getListeningStatsUseCase,
            sleepTimerManager = sleepTimerManager,
            wakeAlarmManager = wakeAlarmManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleDarkMode updates theme config`() = runTest {
        viewModel.toggleDarkMode(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.isDarkMode })
        }
    }

    @Test
    fun `toggleAmoled updates theme config`() = runTest {
        viewModel.toggleAmoled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.isAmoled })
        }
    }

    @Test
    fun `setShapePreset updates theme shape`() = runTest {
        viewModel.setShapePreset(ShapePreset.FULL_EXPRESSIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.shapePreset == ShapePreset.FULL_EXPRESSIVE })
        }
    }

    @Test
    fun `setPlayerLayout updates layout selection`() = runTest {
        viewModel.setPlayerLayout(PlayerLayout.VINYL_RECORD)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.playerLayout == PlayerLayout.VINYL_RECORD })
        }
    }

    @Test
    fun `startSleepTimer delegates to sleepTimerManager`() = runTest {
        viewModel.startSleepTimer(30)
        verify { sleepTimerManager.startTimer(30, any()) }
    }

    @Test
    fun `setSleepTimerFadeSeconds updates theme config and manager`() = runTest {
        viewModel.setSleepTimerFadeSeconds(60)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.sleepTimerFadeSeconds == 60 })
        }
        verify { sleepTimerManager.setFadeDuration(60) }
    }

    @Test
    fun `cancelSleepTimer delegates to sleepTimerManager`() = runTest {
        viewModel.cancelSleepTimer()
        verify { sleepTimerManager.cancelTimer() }
    }

    @Test
    fun `setEqualizerPreset updates state`() = runTest {
        viewModel.setEqualizerPreset(EqualizerPreset.BASS_BOOST)
        assertEquals(EqualizerPreset.BASS_BOOST, viewModel.equalizerPreset.value)
    }

    @Test
    fun `loadListeningStats exposes stats stream`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(42, viewModel.listeningStats.value.totalPlays)
    }

    @Test
    fun `setCrossfade updates theme crossfade seconds`() = runTest {
        viewModel.setCrossfade(6)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.crossfadeSeconds == 6 })
        }
    }

    @Test
    fun `toggleAutoSkipSegments updates config`() = runTest {
        viewModel.toggleAutoSkipSegments(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.autoSkipSegments })
        }
    }

    @Test
    fun `toggleVolumeNormalization updates config`() = runTest {
        viewModel.toggleVolumeNormalization(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.volumeNormalizationEnabled })
        }
    }

    @Test
    fun `setTargetGain updates config`() = runTest {
        viewModel.setTargetGain(300)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.targetGainmB == 300 })
        }
    }

    @Test
    fun `setCacheBudget updates config`() = runTest {
        viewModel.setCacheBudget(2000)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.cacheBudgetMb == 2000 })
        }
    }

    @Test
    fun `clearCache returns freed bytes`() {
        val freed = viewModel.clearCache()
        org.junit.Assert.assertEquals(0L, freed)
    }

    @Test
    fun `togglePerTrackGain updates config`() = runTest {
        viewModel.togglePerTrackGain(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.perTrackGainEnabled })
        }
    }

    @Test
    fun `setVisualizerMode updates config`() = runTest {
        viewModel.setVisualizerMode(VisualizerMode.CIRCULAR_WAVE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.visualizerMode == VisualizerMode.CIRCULAR_WAVE })
        }
    }

    @Test
    fun `toggleInfiniteAutoPlay updates config`() = runTest {
        viewModel.toggleInfiniteAutoPlay(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.infiniteAutoPlayEnabled })
        }
    }

    @Test
    fun `toggleBitPerfectUsbAudio updates theme config`() = runTest {
        viewModel.toggleBitPerfectUsbAudio(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.bitPerfectUsbAudioEnabled })
        }
    }

    @Test
    fun `setForceHiResSamplingRate updates theme config`() = runTest {
        viewModel.setForceHiResSamplingRate(96000)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.forceHiResSamplingRate == 96000 })
        }
    }

    @Test
    fun `toggleGaplessPreload updates theme config`() = runTest {
        viewModel.toggleGaplessPreload(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.gaplessPreloadEnabled })
        }
    }

    @Test
    fun `setGaplessPreloadSeconds updates theme config`() = runTest {
        viewModel.setGaplessPreloadSeconds(10)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.gaplessPreloadSeconds == 10 })
        }
    }

    @Test
    fun `clearLyricsCache invokes repository clear`() = runTest {
        val mockRepo = mockk<com.nexus.tune.core.domain.repository.MusicRepository>(relaxed = true)
        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            musicRepository = mockRepo
        )
        vm.clearLyricsCache()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.clearLyricsCache() }
    }

    @Test
    fun `toggleAutoGenreEq updates theme config`() = runTest {
        viewModel.toggleAutoGenreEq(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.autoGenreEqEnabled })
        }
    }

    @Test
    fun `setVolumeLimit updates theme config`() = runTest {
        viewModel.setVolumeLimit(80)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.volumeLimitPercent == 80 })
        }
    }

    @Test
    fun `toggleSafeListeningAlert updates theme config`() = runTest {
        viewModel.toggleSafeListeningAlert(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.safeListeningAlertEnabled })
        }
    }

    @Test
    fun `toggleSpatialAudio updates theme config`() = runTest {
        viewModel.toggleSpatialAudio(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.spatialAudioEnabled && it.spatialAudioStage == SpatialAudioStage.STUDIO })
        }
    }

    @Test
    fun `setSpatialAudioStage updates stage in theme config`() = runTest {
        viewModel.setSpatialAudioStage(SpatialAudioStage.CONCERT_HALL)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.spatialAudioEnabled && it.spatialAudioStage == SpatialAudioStage.CONCERT_HALL })
        }
    }

    @Test
    fun `setCrossfadeCurve updates theme config`() = runTest {
        viewModel.setCrossfadeCurve(CrossfadeCurve.EXPONENTIAL)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.crossfadeCurve == CrossfadeCurve.EXPONENTIAL })
        }
    }

    @Test
    fun `exportBackup invokes backupManager`() = runTest {
        val mockBackupManager = mockk<com.nexus.tune.core.data.backup.BackupManager>(relaxed = true)
        coEvery { mockBackupManager.exportBackupJson() } returns "{}"

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            backupManager = mockBackupManager
        )
        vm.exportBackup()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockBackupManager.exportBackupJson() }
        assertEquals("{}", vm.backupExportResult.value)
    }

    @Test
    fun `importFromNewPipe invokes backupManager`() = runTest {
        val mockBackupManager = mockk<com.nexus.tune.core.data.backup.BackupManager>(relaxed = true)
        coEvery { mockBackupManager.importFromNewPipeJson(any()) } returns com.nexus.tune.core.data.backup.NewPipeImportResult(1, 5, true)

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            backupManager = mockBackupManager
        )
        vm.importFromNewPipe("{\"playlists\":[]}")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockBackupManager.importFromNewPipeJson(any()) }
        assertTrue(vm.backupImportStatus.value?.contains("Imported 1 playlists") == true)
    }

    @Test
    fun `setPreferredQuality delegates to settingsRepository`() = runTest {
        viewModel.setPreferredQuality("LOW")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setPreferredQuality("LOW") }
    }

    @Test
    fun `isMeteredConnection reflects provider value`() = runTest {
        val meteredVm = SettingsViewModel(
            settingsRepository = settingsRepository,
            getListeningStatsUseCase = getListeningStatsUseCase,
            sleepTimerManager = sleepTimerManager,
            isMeteredConnectionProvider = { true }
        )
        assertTrue(meteredVm.isMeteredConnection.value)
    }

    @Test
    fun `exportLikedTracksToM3u exports liked tracks via backupManager`() = runTest {
        val mockBackupManager = mockk<com.nexus.tune.core.data.backup.BackupManager>(relaxed = true)
        val mockMusicRepo = mockk<com.nexus.tune.core.domain.repository.MusicRepository>(relaxed = true)
        val sampleLiked = listOf(Track("t1", "Title 1", "Artist 1"))
        every { mockMusicRepo.getLikedTracks() } returns flowOf(sampleLiked)
        every { mockBackupManager.exportPlaylistToM3u(any(), any()) } returns "#EXTM3U\n#EXTINF:100,Artist 1 - Title 1\nurl"

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            backupManager = mockBackupManager,
            musicRepository = mockMusicRepo
        )
        vm.exportLikedTracksToM3u()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { mockBackupManager.exportPlaylistToM3u("NexusTune Favorites", sampleLiked) }
        assertTrue(vm.m3uExportResult.value?.contains("#EXTM3U") == true)
    }

    @Test
    fun `importPlaylistFromM3u delegates to backupManager`() = runTest {
        val mockBackupManager = mockk<com.nexus.tune.core.data.backup.BackupManager>(relaxed = true)
        coEvery { mockBackupManager.importPlaylistFromM3u(any(), any()) } returns Pair("Road Trip", 4)

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            backupManager = mockBackupManager
        )
        vm.importPlaylistFromM3u("#EXTM3U\n#EXTINF:200,A - B\nurl", "Road Trip")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockBackupManager.importPlaylistFromM3u(any(), "Road Trip") }
        assertTrue(vm.backupImportStatus.value?.contains("Imported playlist 'Road Trip' (4 tracks)") == true)
    }

    @Test
    fun `toggleShakeToExtend updates themeConfig and sleepTimerManager`() = runTest {
        viewModel.toggleShakeToExtend(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.shakeToExtendSleepTimerEnabled })
        }
        verify { sleepTimerManager.setShakeToExtend(true, any()) }
    }

    @Test
    fun `setShakeExtendMinutes updates themeConfig and sleepTimerManager`() = runTest {
        viewModel.setShakeExtendMinutes(15)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.shakeExtendMinutes == 15 })
        }
        verify { sleepTimerManager.setShakeToExtend(any(), 15) }
    }

    @Test
    fun `setAmbientSound updates themeConfig and synthesizer sound type`() = runTest {
        viewModel.setAmbientSound(AmbientSoundType.BROWN_NOISE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.ambientSoundType == AmbientSoundType.BROWN_NOISE })
        }
        assertEquals(AmbientSoundType.BROWN_NOISE, viewModel.ambientState.value.soundType)
    }

    @Test
    fun `setAmbientVolume clamps and updates themeConfig`() = runTest {
        viewModel.setAmbientVolume(0.85f)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { kotlin.math.abs(it.ambientSoundVolume - 0.85f) < 0.001f })
        }
        assertEquals(0.85f, viewModel.ambientState.value.volume, 0.001f)
    }

    @Test
    fun `setCustomEqBandLevel updates band and switches preset to CUSTOM`() = runTest {
        viewModel.setCustomEqBandLevel(0, 400)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.customEqBands[0] == 400 })
        }
        assertEquals(EqualizerPreset.CUSTOM, viewModel.equalizerPreset.value)
    }

    @Test
    fun `resetCustomEqBands resets all bands to 0`() = runTest {
        viewModel.resetCustomEqBands()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.customEqBands.all { b -> b == 0 } })
        }
        assertEquals(EqualizerPreset.CUSTOM, viewModel.equalizerPreset.value)
    }

    @Test
    fun `toggleBedtimeHandoff updates themeConfig and sleepTimerManager`() = runTest {
        viewModel.toggleBedtimeHandoff(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.bedtimeHandoffEnabled })
        }
        verify { sleepTimerManager.setBedtimeHandoff(true, any(), any(), any()) }
    }

    @Test
    fun `setBedtimeHandoffSoundType updates sound profile`() = runTest {
        viewModel.setBedtimeHandoffSoundType(AmbientSoundType.PINK_NOISE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.bedtimeHandoffSoundType == AmbientSoundType.PINK_NOISE })
        }
    }

    @Test
    fun `setBedtimeHandoffVolume clamps and updates volume`() = runTest {
        viewModel.setBedtimeHandoffVolume(0.35f)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { kotlin.math.abs(it.bedtimeHandoffVolume - 0.35f) < 0.001f })
        }
    }

    @Test
    fun `setBedtimeHandoffDuration updates duration minutes`() = runTest {
        viewModel.setBedtimeHandoffDuration(120)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.bedtimeHandoffDurationMinutes == 120 })
        }
    }

    @Test
    fun `setSeekbarStyle updates theme config`() = runTest {
        viewModel.setSeekbarStyle(SeekbarStyle.WAVEFORM)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.seekbarStyle == SeekbarStyle.WAVEFORM })
        }
    }

    @Test
    fun `toggleResetPitchOnTrackChange updates theme config`() = runTest {
        viewModel.toggleResetPitchOnTrackChange(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.resetPitchOnTrackChange })
        }
    }

    @Test
    fun `toggleSmartShuffle updates theme config`() = runTest {
        viewModel.toggleSmartShuffle(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { !it.smartShuffleEnabled })
        }
    }

    @Test
    fun `updateWakeAlarmConfig updates config and schedules alarm`() = runTest {
        val newConfig = WakeAlarmConfig(
            isEnabled = true,
            hour = 7,
            minute = 15,
            fadeInSeconds = 120,
            targetVolume = 0.85f,
            source = WakeAlarmSource.AMBIENT_SOUND
        )
        viewModel.updateWakeAlarmConfig(newConfig)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.wakeAlarmConfig == newConfig })
        }
        verify {
            wakeAlarmManager.scheduleAlarm(newConfig)
        }
    }

    @Test
    fun `testTriggerWakeAlarm delegates to wakeAlarmManager`() = runTest {
        viewModel.testTriggerWakeAlarm()
        verify {
            wakeAlarmManager.triggerWakeAlarm(any())
        }
    }

    @Test
    fun `stopWakeAlarm and snoozeWakeAlarm delegate to wakeAlarmManager`() = runTest {
        viewModel.stopWakeAlarm()
        verify {
            wakeAlarmManager.stopAlarm()
        }

        viewModel.snoozeWakeAlarm(15)
        verify {
            wakeAlarmManager.snooze(15)
        }
    }

    @Test
    fun `setLoudnessMode, setTargetLufs, and setPreAmpGainDb update themeConfig`() = runTest {
        viewModel.setLoudnessMode(LoudnessNormalizationMode.DYNAMIC_PEAK_LIMITER)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.loudnessConfig.mode == LoudnessNormalizationMode.DYNAMIC_PEAK_LIMITER
            })
        }

        viewModel.setTargetLufs(-11.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.loudnessConfig.targetLufs == -11.0f
            })
        }

        viewModel.setPreAmpGainDb(2.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.loudnessConfig.preAmpGainDb == 2.5f
            })
        }
    }
}
