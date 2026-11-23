package com.nexus.tune.feature.settings

import app.cash.turbine.test
import com.nexus.tune.core.domain.repository.SettingsRepository
import com.nexus.tune.core.model.*
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val themeFlow = MutableStateFlow(ThemeConfig())

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getThemeConfig() } returns themeFlow
        viewModel = SettingsViewModel(settingsRepository)
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
}

