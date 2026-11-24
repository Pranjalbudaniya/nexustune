package com.nexus.tune.feature.settings

import app.cash.turbine.test
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.domain.usecase.GetListeningStatsUseCase
import com.nexus.tune.core.model.*
import com.nexus.tune.core.player.SleepTimerManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val getListeningStatsUseCase = mockk<GetListeningStatsUseCase>(relaxed = true)
    private val sleepTimerManager = mockk<SleepTimerManager>(relaxed = true)
    private val themeFlow = MutableStateFlow(ThemeConfig())

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getThemeConfig() } returns themeFlow
        every { getListeningStatsUseCase() } returns flowOf(
            ListeningStats(
                totalPlays = 42,
                topArtists = listOf("Artist A", "Artist B")
            )
        )
        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            getListeningStatsUseCase = getListeningStatsUseCase,
            sleepTimerManager = sleepTimerManager
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
}
