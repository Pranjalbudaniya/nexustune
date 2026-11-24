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
    fun `setVisualizerPalette updates config`() = runTest {
        viewModel.setVisualizerPalette(VisualizerPalette.CYBERPUNK)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.visualizerPalette == VisualizerPalette.CYBERPUNK })
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

    @Test
    fun `listeningStreak and milestones evaluate properly`() = runTest {
        val streak = ListeningStreak(currentStreakDays = 5, longestStreakDays = 10)
        every { settingsRepository.getListeningStreak() } returns flowOf(streak)

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            getListeningStatsUseCase = getListeningStatsUseCase,
            sleepTimerManager = sleepTimerManager,
            wakeAlarmManager = wakeAlarmManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, vm.listeningStreak.value.currentStreakDays)
        assertEquals(10, vm.listeningStreak.value.longestStreakDays)

        assertTrue(vm.milestones.value.isNotEmpty())
        val firstBeat = vm.milestones.value.find { it.type == MilestoneType.FIRST_BEAT }
        assertNotNull(firstBeat)
        assertTrue(firstBeat!!.isUnlocked)

        assertEquals(42, vm.timeCapsuleRecap.value.totalPlays)
        assertEquals(5, vm.timeCapsuleRecap.value.streakDays)
    }

    @Test
    fun `saveCustomEqProfile and deleteCustomEqProfile delegate to repository`() = runTest {
        viewModel.saveCustomEqProfile("My Sony XM5", listOf(300, 200, 0, 100, 400))
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.saveCustomEqProfile(match {
                it.name == "My Sony XM5" && it.bands == listOf(300, 200, 0, 100, 400)
            })
        }

        viewModel.deleteCustomEqProfile("test-id-123")
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.deleteCustomEqProfile("test-id-123")
        }
    }

    @Test
    fun `applyCustomEqProfile updates themeConfig customEqBands and preset`() = runTest {
        val profile = CustomEqProfile(id = "p1", name = "Warm Bass", bands = listOf(500, 300, 0, 0, 200))
        viewModel.applyCustomEqProfile(profile)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.customEqBands == listOf(500, 300, 0, 0, 200)
            })
        }
        assertEquals(EqualizerPreset.CUSTOM, viewModel.equalizerPreset.value)
    }

    @Test
    fun `setDjPreset and setDjRememberAcrossTracks update themeConfig`() = runTest {
        viewModel.setDjPreset(DjPreset.NIGHTCORE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.djFxConfig.preset == DjPreset.NIGHTCORE && it.djFxConfig.speed == 1.25f && it.djFxConfig.semitones == 2
            })
        }

        viewModel.setDjRememberAcrossTracks(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.djRememberAcrossTracks
            })
        }
    }

    @Test
    fun `sonicDnaProfile calculates correctly and provides shareable text`() = runTest {
        val stats = ListeningStats(
            totalPlays = 60,
            morningPlays = 2,
            nightPlays = 50,
            topArtists = listOf("M83", "Daft Punk")
        )
        val streak = ListeningStreak(currentStreakDays = 4, longestStreakDays = 6)
        every { getListeningStatsUseCase.invoke() } returns flowOf(stats)
        every { settingsRepository.getListeningStreak() } returns flowOf(streak)

        val vm = SettingsViewModel(
            settingsRepository = settingsRepository,
            getListeningStatsUseCase = getListeningStatsUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SonicArchetype.NOCTURNAL_VOYAGER, vm.sonicDnaProfile.value.archetype)
        assertEquals(60, vm.sonicDnaProfile.value.totalTracksPlayed)
        val summary = vm.getShareableSonicDnaText()
        assertTrue(summary.contains("Nocturnal Voyager"))
        assertTrue(summary.contains("M83"))
    }

    @Test
    fun `autoStash configuration updates persist via settingsRepository`() = runTest {
        viewModel.setAutoStashEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.autoStashConfig.isEnabled })
        }

        viewModel.setAutoStashBudgetMb(2000)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.autoStashConfig.storageBudgetMb == 2000 })
        }

        viewModel.setAutoStashStrategy(com.nexus.tune.core.model.AutoStashStrategy.HEAVY_ROTATION)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.autoStashConfig.strategy == com.nexus.tune.core.model.AutoStashStrategy.HEAVY_ROTATION })
        }

        viewModel.setAutoStashWifiOnly(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.autoStashConfig.wifiOnly })
        }
    }

    @Test
    fun `headphone correction configuration updates persist via settingsRepository`() = runTest {
        viewModel.setHeadphoneCorrectionEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.headphoneCorrectionConfig.isEnabled })
        }

        viewModel.setHeadphoneTargetCurve(HeadphoneTargetCurve.HARMAN_IN_EAR)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.headphoneCorrectionConfig.targetCurve == HeadphoneTargetCurve.HARMAN_IN_EAR })
        }

        viewModel.setHeadphoneCrossfeedEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.headphoneCorrectionConfig.crossfeedEnabled })
        }
    }

    @Test
    fun `stereo width configuration updates persist via settingsRepository`() = runTest {
        viewModel.setStereoWidthEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.stereoWidthConfig.isEnabled })
        }

        viewModel.setStereoWidthMode(StereoWidthMode.SUPER_WIDE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.stereoWidthConfig.mode == StereoWidthMode.SUPER_WIDE && it.stereoWidthConfig.customWidthPercent == 175 })
        }

        viewModel.setStereoWidthPercent(150)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.stereoWidthConfig.customWidthPercent == 150 })
        }

        viewModel.setStereoHaasDelayMs(10.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.stereoWidthConfig.haasDelayMs == 10.0f })
        }

        viewModel.setStereoMidSideLevels(2.0f, -4.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.stereoWidthConfig.midLevelDb == 2.0f && it.stereoWidthConfig.sideLevelDb == -4.0f })
        }
    }

    @Test
    fun `setSleepDriftProfile updates theme and delegates to sleepTimerManager`() = runTest {
        viewModel.setSleepDriftProfile(SleepDriftProfile.WARM_MELLOW)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.sleepDriftProfile == SleepDriftProfile.WARM_MELLOW })
        }
        verify {
            sleepTimerManager.setSleepDriftProfile(SleepDriftProfile.WARM_MELLOW)
        }
    }

    @Test
    fun `setDitherMode updates theme config`() = runTest {
        viewModel.setDitherMode(DitherMode.PSYCHOACOUSTIC_LIPSHITZ)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingsRepository.updateThemeConfig(match { it.ditherMode == DitherMode.PSYCHOACOUSTIC_LIPSHITZ })
        }
    }

    @Test
    fun `dynamic loudness settings update theme config and apply offsets`() = runTest {
        viewModel.setDynamicLoudnessEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicLoudnessConfig.isEnabled })
        }

        viewModel.setDynamicLoudnessMode(DynamicLoudnessMode.DEEP_PUNCH)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicLoudnessConfig.mode == DynamicLoudnessMode.DEEP_PUNCH })
        }

        viewModel.setDynamicLoudnessIntensity(80)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicLoudnessConfig.intensityPercent == 80 })
        }
    }

    @Test
    fun `beat crossfade settings update theme config`() = runTest {
        viewModel.setBeatCrossfadeEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.beatCrossfadeConfig.isEnabled })
        }

        viewModel.setBeatCrossfadeMode(BeatCrossfadeMode.BASS_SWAP_CLUB)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.beatCrossfadeConfig.mode == BeatCrossfadeMode.BASS_SWAP_CLUB })
        }

        viewModel.setBeatCrossfadeMaxBend(8)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.beatCrossfadeConfig.maxTempoBendPercent == 8 })
        }
    }

    @Test
    fun `silence trim settings update theme config`() = runTest {
        viewModel.setSilenceTrimEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.silenceTrimConfig.isEnabled })
        }

        viewModel.setSilenceTrimMode(SilenceTrimMode.FAST_TALK)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.silenceTrimConfig.mode == SilenceTrimMode.FAST_TALK })
        }

        viewModel.setSilenceTrimThresholdDb(-38.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.silenceTrimConfig.thresholdDb == -38.0f })
        }
    }

    @Test
    fun `resampling filter settings update theme config`() = runTest {
        viewModel.setResamplingFilterMode(ResamplingFilterMode.SLOW_ROLL_OFF_WARM)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.resamplingFilterConfig.mode == ResamplingFilterMode.SLOW_ROLL_OFF_WARM })
        }

        viewModel.setResamplingFilterCutoff(0.92f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.resamplingFilterConfig.cutoffRatio == 0.92f })
        }
    }

    @Test
    fun `phase guard settings update theme config`() = runTest {
        viewModel.setPhaseGuardEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.phaseMeterConfig.isEnabled })
        }

        viewModel.setMonoGuardMode(MonoGuardMode.STRICT_MONO_COMPLIANCE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.phaseMeterConfig.guardMode == MonoGuardMode.STRICT_MONO_COMPLIANCE })
        }

        viewModel.setPhaseGuardThreshold(0.35f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.phaseMeterConfig.minCorrelationThreshold == 0.35f })
        }
    }

    @Test
    fun `tilt equalizer settings update theme config`() = runTest {
        viewModel.setTiltEqEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tiltEqualizerConfig.isEnabled })
        }

        viewModel.setTiltEqDb(2.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tiltEqualizerConfig.tiltDb == 2.5f })
        }

        viewModel.setTiltEqPivotHz(630.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tiltEqualizerConfig.pivotHz == 630.0f })
        }

        viewModel.setTiltEqLoudnessCompensate(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.tiltEqualizerConfig.loudnessCompensate })
        }
    }

    @Test
    fun `de-clicker settings update theme config`() = runTest {
        viewModel.setDeClickerEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deClickerConfig.isEnabled })
        }

        viewModel.setDeClickerMode(DeClickerMode.VINYL_RESTORATION)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deClickerConfig.mode == DeClickerMode.VINYL_RESTORATION })
        }

        viewModel.setDeClickerSensitivity(4.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deClickerConfig.sensitivityMultiplier == 4.5f })
        }

        viewModel.setDeClickerMaxWidth(6)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deClickerConfig.maxClickWidthSamples == 6 })
        }
    }

    @Test
    fun `mid-bass clarity settings update theme config`() = runTest {
        viewModel.setMidBassClarityEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.midBassClarityConfig.isEnabled })
        }

        viewModel.setMidBassClarityMode(MidBassClarityMode.AGGRESSIVE_DECONGEST)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.midBassClarityConfig.mode == MidBassClarityMode.AGGRESSIVE_DECONGEST })
        }

        viewModel.setMidBassClarityCenterHz(360.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.midBassClarityConfig.centerFrequencyHz == 360.0f })
        }

        viewModel.setMidBassClarityThreshold(45)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.midBassClarityConfig.dynamicThresholdPercent == 45 })
        }
    }

    @Test
    fun `crosstalk cancellation settings update theme config`() = runTest {
        viewModel.setCrosstalkCancellationEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.crosstalkCancellationConfig.isEnabled })
        }

        viewModel.setCrosstalkCancellationMode(CrosstalkCancellationMode.SOUNDBAR_LIVING_ROOM)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.crosstalkCancellationConfig.mode == CrosstalkCancellationMode.SOUNDBAR_LIVING_ROOM })
        }

        viewModel.setCrosstalkCancellationDelay(12)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.crosstalkCancellationConfig.delaySamples == 12 })
        }

        viewModel.setCrosstalkCancellationIntensity(85)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.crosstalkCancellationConfig.cancellationIntensityPercent == 85 })
        }
    }

    @Test
    fun `infrasonic filter settings update theme config`() = runTest {
        viewModel.setInfrasonicFilterEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.infrasonicFilterConfig.isEnabled })
        }

        viewModel.setInfrasonicFilterCutoff(25.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.infrasonicFilterConfig.cutoffHz == 25.0f })
        }

        viewModel.setInfrasonicFilterSlope(InfrasonicSlope.SLOPE_24DB_OCTAVE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.infrasonicFilterConfig.slope == InfrasonicSlope.SLOPE_24DB_OCTAVE })
        }

        viewModel.setInfrasonicDcBlockerEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.infrasonicFilterConfig.dcBlockerEnabled })
        }
    }

    @Test
    fun `air band exciter settings update theme config and audio effects`() = runTest {
        viewModel.setAirBandEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.airBandExciterConfig.isEnabled })
        }

        viewModel.setAirBandMode(AirBandMode.MASTERING_AIR)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.airBandExciterConfig.mode == AirBandMode.MASTERING_AIR })
        }

        viewModel.setAirBandFrequency(AirBandFrequency.HZ_20000)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.airBandExciterConfig.frequency == AirBandFrequency.HZ_20000 })
        }

        viewModel.setAirBandBoostDb(5.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.airBandExciterConfig.boostDb == 5.0f })
        }

        viewModel.setAirBandHarmonics(60)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.airBandExciterConfig.harmonicExcitationPercent == 60 })
        }
    }

    @Test
    fun `sub-harmonic synth settings update theme config and audio effects`() = runTest {
        viewModel.setSubHarmonicSynthEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.subHarmonicSynthConfig.isEnabled })
        }

        viewModel.setSubHarmonicSynthMode(SubHarmonicMode.CLUB_EARTHQUAKE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.subHarmonicSynthConfig.mode == SubHarmonicMode.CLUB_EARTHQUAKE })
        }

        viewModel.setSubHarmonicSynthTarget(SubHarmonicTarget.FULL_RANGE_SUBWOOFER)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.subHarmonicSynthConfig.target == SubHarmonicTarget.FULL_RANGE_SUBWOOFER })
        }

        viewModel.setSubHarmonicSynthIntensity(85)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.subHarmonicSynthConfig.intensityPercent == 85 })
        }

        viewModel.setSubHarmonicSynthBlend(75)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.subHarmonicSynthConfig.subOctaveBlendPercent == 75 })
        }
    }

    @Test
    fun `de-esser settings update theme config and audio effects`() = runTest {
        viewModel.setDeEsserEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deEsserConfig.isEnabled })
        }

        viewModel.setDeEsserMode(DeEsserMode.BRIGHT_HEADPHONE_TAMER)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deEsserConfig.mode == DeEsserMode.BRIGHT_HEADPHONE_TAMER })
        }

        viewModel.setDeEsserCenterFrequency(7000.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deEsserConfig.centerFrequencyHz == 7000.0f })
        }

        viewModel.setDeEsserThreshold(25)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deEsserConfig.thresholdPercent == 25 })
        }

        viewModel.setDeEsserMaxReduction(6.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.deEsserConfig.maxReductionDb == 6.0f })
        }
    }

    @Test
    fun `true peak guard settings update theme config`() = runTest {
        viewModel.setTruePeakGuardEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.truePeakGuardConfig.isEnabled })
        }

        viewModel.setTruePeakGuardMode(TruePeakMode.EBU_R128_STANDARD)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.truePeakGuardConfig.mode == TruePeakMode.EBU_R128_STANDARD })
        }

        viewModel.setTruePeakGuardCeiling(TruePeakCeiling.CEILING_MINUS_1_0_DBTP)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.truePeakGuardConfig.ceiling == TruePeakCeiling.CEILING_MINUS_1_0_DBTP })
        }

        viewModel.setTruePeakOversample4x(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.truePeakGuardConfig.oversample4xEnabled })
        }
    }

    @Test
    fun `ultrasonic filter settings update theme config`() = runTest {
        viewModel.setUltrasonicFilterEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.ultrasonicFilterConfig.isEnabled })
        }

        viewModel.setUltrasonicFilterMode(UltrasonicMode.HI_RES_TAMER_24K)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.ultrasonicFilterConfig.mode == UltrasonicMode.HI_RES_TAMER_24K })
        }

        viewModel.setUltrasonicFilterTopology(UltrasonicTopology.MINIMUM_PHASE_IIR)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.ultrasonicFilterConfig.topology == UltrasonicTopology.MINIMUM_PHASE_IIR })
        }

        viewModel.setUltrasonicFilterCutoff(24000.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.ultrasonicFilterConfig.cutoffHz == 24000.0f })
        }
    }

    @Test
    fun `tube saturation settings update theme config`() = runTest {
        viewModel.setTubeSaturationEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tubeSaturationConfig.isEnabled })
        }

        viewModel.setTubeSaturationProfile(TubeWarmthProfile.LUSH_TRIODE_WARMTH)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tubeSaturationConfig.profile == TubeWarmthProfile.LUSH_TRIODE_WARMTH })
        }

        viewModel.setTubeSaturationTopology(TubeTopology.PENTODE_EL34)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tubeSaturationConfig.topology == TubeTopology.PENTODE_EL34 })
        }

        viewModel.setTubeSaturationDrive(0.65f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tubeSaturationConfig.drive == 0.65f })
        }

        viewModel.setTubeSaturationBias(0.15f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tubeSaturationConfig.bias == 0.15f })
        }

        viewModel.setTubeSaturationAutoMakeupGain(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.tubeSaturationConfig.autoMakeupGain })
        }
    }

    @Test
    fun `transient shaper settings update theme config`() = runTest {
        viewModel.setTransientShaperEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.transientShaperConfig.isEnabled })
        }

        viewModel.setTransientShaperPreset(TransientPreset.CRISP_DRUM_PUNCH)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.transientShaperConfig.preset == TransientPreset.CRISP_DRUM_PUNCH })
        }

        viewModel.setTransientShaperFocus(TransientBandFocus.BASS_PUNCH_LOW)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.transientShaperConfig.bandFocus == TransientBandFocus.BASS_PUNCH_LOW })
        }

        viewModel.setTransientShaperAttack(0.55f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.transientShaperConfig.attackBoost == 0.55f })
        }

        viewModel.setTransientShaperSustain(-0.25f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.transientShaperConfig.sustainBoost == -0.25f })
        }

        viewModel.setTransientShaperAutoLimit(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.transientShaperConfig.autoOutputLimit })
        }
    }

    @Test
    fun `dynamic stereo imager settings update theme config`() = runTest {
        viewModel.setDynamicStereoImagerEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicStereoImagerConfig.isEnabled })
        }

        viewModel.setDynamicStereoImagerPreset(StereoImagerPreset.WIDE_CONCERT_HALL)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicStereoImagerConfig.preset == StereoImagerPreset.WIDE_CONCERT_HALL })
        }

        viewModel.setDynamicStereoImagerWidth(1.65f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicStereoImagerConfig.stereoWidth == 1.65f })
        }

        viewModel.setDynamicStereoImagerBassCutoff(180.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicStereoImagerConfig.monoBassCutoffHz == 180.0f })
        }

        viewModel.setDynamicStereoImagerAirSpread(0.65f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.dynamicStereoImagerConfig.highBandAirSpread == 0.65f })
        }

        viewModel.setDynamicStereoImagerMonoGuard(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.dynamicStereoImagerConfig.monoGuardActive })
        }
    }

    @Test
    fun `tape hysteresis settings update theme config`() = runTest {
        viewModel.setTapeHysteresisEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.isEnabled })
        }

        viewModel.setTapeHysteresisPreset(TapePreset.VINTAGE_WARM_15_IPS)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.preset == TapePreset.VINTAGE_WARM_15_IPS })
        }

        viewModel.setTapeHysteresisSpeed(TapeSpeed.IPS_7_5)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.speed == TapeSpeed.IPS_7_5 })
        }

        viewModel.setTapeHysteresisDrive(0.75f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.tapeDrive == 0.75f })
        }

        viewModel.setTapeHysteresisHeadBump(3.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.headBumpGainDb == 3.5f })
        }

        viewModel.setTapeHysteresisWowFlutter(0.45f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.tapeHysteresisConfig.wowFlutterDepth == 0.45f })
        }

        viewModel.setTapeHysteresisNoiseReduction(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.tapeHysteresisConfig.tapeHissReduction })
        }
    }

    @Test
    fun `mains hum isolator settings update theme config`() = runTest {
        viewModel.setMainsHumIsolatorEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.isEnabled })
        }

        viewModel.setMainsHumIsolatorPreset(HumIsolatorPreset.STUDIO_CLEAN_AUTO)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.preset == HumIsolatorPreset.STUDIO_CLEAN_AUTO })
        }

        viewModel.setMainsHumIsolatorFrequency(MainsFrequency.HZ_60)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.frequency == MainsFrequency.HZ_60 })
        }

        viewModel.setMainsHumIsolatorHarmonicDepth(HumHarmonicDepth.FULL_SPECTRUM)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.harmonicDepth == HumHarmonicDepth.FULL_SPECTRUM })
        }

        viewModel.setMainsHumIsolatorNotchQ(32.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.notchQ == 32.0f })
        }

        viewModel.setMainsHumIsolatorNotchDepth(-30.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.mainsHumIsolatorConfig.notchDepthDb == -30.0f })
        }

        viewModel.setMainsHumIsolatorAdaptiveMasking(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.mainsHumIsolatorConfig.adaptiveMasking })
        }

        viewModel.setMainsHumIsolatorDcBlocker(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.mainsHumIsolatorConfig.dcBlockerEnabled })
        }
    }

    @Test
    fun `acoustic feedback settings update theme config`() = runTest {
        viewModel.setAcousticFeedbackEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.isEnabled })
        }

        viewModel.setAcousticFeedbackPreset(AcousticFeedbackPreset.LIVE_CONCERT_DE_SQUEAL)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.preset == AcousticFeedbackPreset.LIVE_CONCERT_DE_SQUEAL })
        }

        viewModel.setAcousticFeedbackMode(FeedbackHuntMode.LIVE_CONCERT)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.mode == FeedbackHuntMode.LIVE_CONCERT })
        }

        viewModel.setAcousticFeedbackSensitivity(-32.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.sensitivityThresholdDb == -32.0f })
        }

        viewModel.setAcousticFeedbackNotchesCount(4)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.activeNotchesCount == 4 })
        }

        viewModel.setAcousticFeedbackNotchQ(45.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.notchSharpnessQ == 45.0f })
        }

        viewModel.setAcousticFeedbackSuppressionDepth(-28.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.acousticFeedbackConfig.suppressionDepthDb == -28.0f })
        }

        viewModel.setAcousticFeedbackPanicClamp(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.acousticFeedbackConfig.fastPanicClamp })
        }
    }

    @Test
    fun `soundstage elevation settings update theme config`() = runTest {
        viewModel.setSoundstageElevationEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.isEnabled })
        }

        viewModel.setSoundstageElevationPreset(SoundstageElevationPreset.CONCERT_HALL_BALCONY)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.preset == SoundstageElevationPreset.CONCERT_HALL_BALCONY })
        }

        viewModel.setSoundstageElevationAngle(ElevationAngle.CATHEDRAL_DOME_45)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.elevationAngle == ElevationAngle.CATHEDRAL_DOME_45 })
        }

        viewModel.setSoundstageElevationIntensity(0.85f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.elevationIntensity == 0.85f })
        }

        viewModel.setSoundstageElevationCeilingMs(5.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.ceilingReflectionMs == 5.5f })
        }

        viewModel.setSoundstageElevationPinnaDepth(-6.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.soundstageElevationConfig.pinnaNotchDepthDb == -6.5f })
        }

        viewModel.setSoundstageElevationDiffuseSpread(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.soundstageElevationConfig.diffuseCeilingSpread })
        }
    }

    @Test
    fun `mono bass align settings update theme config`() = runTest {
        viewModel.setMonoBassAlignEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.isEnabled })
        }

        viewModel.setMonoBassAlignPreset(MonoBassAlignPreset.SUBWOOFER_MONO_LOCK_80HZ)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.preset == MonoBassAlignPreset.SUBWOOFER_MONO_LOCK_80HZ })
        }

        viewModel.setMonoBassAlignCrossover(MonoBassCrossover.HZ_120)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.crossover == MonoBassCrossover.HZ_120 })
        }

        viewModel.setMonoBassAlignSlope(MonoBassFilterSlope.SLOPE_12DB_OCT)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.slope == MonoBassFilterSlope.SLOPE_12DB_OCT })
        }

        viewModel.setMonoBassAlignAutoPhase(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { !it.monoBassAlignConfig.autoPhaseAlign })
        }

        viewModel.setMonoBassAlignPunchGain(3.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.subPunchGainDb == 3.0f })
        }

        viewModel.setMonoBassAlignSideSolo(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.monoBassAlignConfig.sideSoloAudition })
        }
    }

    @Test
    fun `preamp noise flattener settings update theme config`() = runTest {
        viewModel.setPreampNoiseFlattenerEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.isEnabled })
        }

        viewModel.setPreampNoiseFlattenerPreset(PreampNoiseFlattenerPreset.STUDIO_SOLID_STATE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.preampNoiseFlattenerConfig.preset == PreampNoiseFlattenerPreset.STUDIO_SOLID_STATE &&
                        it.preampNoiseFlattenerConfig.profile == PreampNoiseProfile.THERMAL_WHITE
            })
        }

        viewModel.setPreampNoiseProfile(PreampNoiseProfile.SHOT_BLUE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.profile == PreampNoiseProfile.SHOT_BLUE })
        }

        viewModel.setPreampFlattenerMode(PreampFlattenerMode.DOWNWARD_EXPAND)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.mode == PreampFlattenerMode.DOWNWARD_EXPAND })
        }

        viewModel.setPreampFlattenDepth(0.9f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.flattenDepth == 0.9f })
        }

        viewModel.setPreampDownwardThreshold(-35.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.downwardThresholdDb == -35.0f })
        }

        viewModel.setPreampThermalDitherLevel(0.4f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.thermalDitherLevel == 0.4f })
        }

        viewModel.setPreampHfTiltDb(-2.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.preampNoiseFlattenerConfig.hfTiltDb == -2.5f })
        }
    }

    @Test
    fun `slew rate limiter settings update theme config`() = runTest {
        viewModel.setSlewRateLimiterEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.slewRateLimiterConfig.isEnabled })
        }

        viewModel.setSlewRateLimiterPreset(SlewRateLimiterPreset.ELECTROSTATIC_STAX_GUARD)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.slewRateLimiterConfig.preset == SlewRateLimiterPreset.ELECTROSTATIC_STAX_GUARD &&
                        it.slewRateLimiterConfig.speed == SlewRateSpeed.ELECTROSTATIC_25V_US
            })
        }

        viewModel.setSlewRateSpeed(SlewRateSpeed.PLANAR_15V_US)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.slewRateLimiterConfig.speed == SlewRateSpeed.PLANAR_15V_US })
        }

        viewModel.setSlewLimiterMode(SlewLimiterMode.STATOR_ARC_PROTECT)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.slewRateLimiterConfig.mode == SlewLimiterMode.STATOR_ARC_PROTECT })
        }

        viewModel.setSlewDampingRatio(0.75f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.slewRateLimiterConfig.dampingRatio == 0.75f })
        }

        viewModel.setSlewPeakThreshold(0.88f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.slewRateLimiterConfig.peakThreshold == 0.88f })
        }
    }

    @Test
    fun `horn diffuser settings update theme config`() = runTest {
        viewModel.setHornDiffuserEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.isEnabled })
        }

        viewModel.setHornDiffuserPreset(HornDiffuserPreset.KLIPSCHORN_HERITAGE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.hornDiffuserConfig.preset == HornDiffuserPreset.KLIPSCHORN_HERITAGE &&
                        it.hornDiffuserConfig.geometry == HornFlareGeometry.TRACTRIX
            })
        }

        viewModel.setHornFlareGeometry(HornFlareGeometry.EXPONENTIAL)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.geometry == HornFlareGeometry.EXPONENTIAL })
        }

        viewModel.setHornDiffuserMode(HornDiffuserMode.THROAT_ANTI_RESONANCE)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.mode == HornDiffuserMode.THROAT_ANTI_RESONANCE })
        }

        viewModel.setHornThroatCutoffHz(300.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.throatCutoffHz == 300.0f })
        }

        viewModel.setHornFlareIntensity(0.85f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.flareIntensity == 0.85f })
        }

        viewModel.setHornThroatAntiResonanceDb(-5.5f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.throatAntiResonanceDb == -5.5f })
        }

        viewModel.setHornHorizontalCoverageAngle(105)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.hornDiffuserConfig.horizontalCoverageAngle == 105 })
        }
    }

    @Test
    fun `bass attack reshaper controls update theme config properly`() = runTest {
        viewModel.setBassAttackReshaperEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.isEnabled })
        }

        viewModel.setBassAttackPreset(BassAttackPreset.TIGHT_KICK_IMPACT)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match {
                it.bassAttackReshaperConfig.preset == BassAttackPreset.TIGHT_KICK_IMPACT &&
                        it.bassAttackReshaperConfig.target == BassAttackTarget.SUB_KICK_60HZ
            })
        }

        viewModel.setBassAttackTarget(BassAttackTarget.PUNCHY_BASS_80HZ)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.target == BassAttackTarget.PUNCHY_BASS_80HZ })
        }

        viewModel.setBassReshaperMode(BassReshaperMode.SUSTAIN_TAIL_DAMP)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.mode == BassReshaperMode.SUSTAIN_TAIL_DAMP })
        }

        viewModel.setBassAttackPunchMultiplier(1.75f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.attackPunchMultiplier == 1.75f })
        }

        viewModel.setBassSustainDampingPercent(60)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.sustainDampingPercent == 60 })
        }

        viewModel.setBassCrossoverFrequencyHz(110.0f)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.crossoverFrequencyHz == 110.0f })
        }

        viewModel.setBassSoloAttackTransient(true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify {
            settingsRepository.updateThemeConfig(match { it.bassAttackReshaperConfig.soloAttackTransient })
        }
    }
}
