package com.nexus.tune

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.nexus.tune.core.common.MusicResult
import com.nexus.tune.core.designsystem.theme.NexusTuneTheme
import com.nexus.tune.core.model.ThemeConfig
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.ui.MiniPlayerBar
import com.nexus.tune.feature.home.HomeScreen
import com.nexus.tune.feature.home.HomeViewModel
import com.nexus.tune.feature.library.LibraryScreen
import com.nexus.tune.feature.library.LibraryViewModel
import com.nexus.tune.feature.player.PlayerScreen
import com.nexus.tune.feature.player.PlayerViewModel
import com.nexus.tune.feature.search.SearchScreen
import com.nexus.tune.feature.search.SearchViewModel
import com.nexus.tune.feature.settings.SettingsScreen
import com.nexus.tune.feature.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: NexusTuneApplication
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        app = application as NexusTuneApplication
        negotiate120HzDisplayMode()

        homeViewModel = HomeViewModel(app.getHomeFeedUseCase, app.manageQueueUseCase)
        searchViewModel = SearchViewModel(app.searchMusicUseCase, app.manageQueueUseCase)
        libraryViewModel = LibraryViewModel(app.musicRepository, app.manageQueueUseCase)
        playerViewModel = PlayerViewModel(
            musicRepository = app.musicRepository,
            manageQueueUseCase = app.manageQueueUseCase,
            abLoopManager = app.abLoopManager,
            playerController = app.playerController
        )
        settingsViewModel = SettingsViewModel(
            settingsRepository = app.settingsRepository,
            getListeningStatsUseCase = app.getListeningStatsUseCase,
            sleepTimerManager = app.sleepTimerManager,
            audioOutputRouteManager = app.audioOutputRouteManager,
            musicRepository = app.musicRepository,
            safeListeningManager = app.safeListeningManager,
            backupManager = app.backupManager,
            isMeteredConnectionProvider = {
                val cm = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                cm?.isActiveNetworkMetered ?: false
            }
        )

        lifecycleScope.launch {
            app.playerController.playbackState.collect {
                com.nexus.tune.widget.NexusTuneWidgetProvider.notifyPlaybackChanged(this@MainActivity)
                com.nexus.tune.widget.NexusTuneQueueWidgetProvider.notifyPlaybackChanged(this@MainActivity)
            }
        }

        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                app.playerController.playbackState,
                app.queueRepository.getQueue(),
                app.queueRepository.getQueueIndex()
            ) { state, queue, idx ->
                Triple(state, queue, idx)
            }.collect { (state, queue, idx) ->
                app.autoPlayQueueManager.checkAndReplenish(state.currentTrack, queue, idx)
            }
        }

        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                app.playerController.playbackState,
                settingsViewModel.themeConfig
            ) { state, config ->
                Pair(state, config)
            }.collect { (state, config) ->
                app.gaplessPreloadManager.checkAndPreload(
                    currentTrack = state.currentTrack,
                    positionMs = state.positionMs,
                    durationMs = state.durationMs,
                    enabled = config.gaplessPreloadEnabled,
                    thresholdSeconds = config.gaplessPreloadSeconds
                )
            }
        }

        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(
                app.playerController.playbackState,
                settingsViewModel.themeConfig
            ) { state, config ->
                Pair(state.currentTrack, config.autoGenreEqEnabled)
            }.collect { (track, autoGenre) ->
                if (autoGenre && track != null) {
                    val preset = app.genreAdaptiveEqManager.applyAdaptivePreset(track)
                    settingsViewModel.setEqualizerPreset(preset)
                }
            }
        }

        lifecycleScope.launch {
            settingsViewModel.themeConfig.collect { config ->
                app.playerController.setVolumeLimit(config.volumeLimitPercent)
                app.playerController.resetPitchOnTrackChange = config.resetPitchOnTrackChange
                com.nexus.tune.core.player.effects.NexusAudioEffects.instance.setSpatialAudioEnabled(config.spatialAudioEnabled)
                com.nexus.tune.core.player.effects.NexusAudioEffects.instance.setSpatialAudioStage(config.spatialAudioStage)
            }
        }

        lifecycleScope.launch {
            while (isActive) {
                delay(5000L)
                val state = app.playerController.playbackState.value
                if (state.status == com.nexus.tune.core.model.PlaybackStatus.PLAYING) {
                    app.safeListeningManager.recordExposure(5L, 0.85f)
                }
            }
        }

        setContent {
            val themeConfig by settingsViewModel.themeConfig.collectAsState()
            val playbackState by app.playerController.playbackState.collectAsState()
            var selectedTab by remember { mutableIntStateOf(0) }
            var isPlayerExpanded by remember { mutableStateOf(false) }

            NexusTuneTheme(themeConfig = themeConfig) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            bottomBar = {
                                Column {
                                    // Docked Mini-Player above navigation
                                    if (playbackState.currentTrack != null && !isPlayerExpanded) {
                                        MiniPlayerBar(
                                            playbackState = playbackState,
                                            onExpandClick = { isPlayerExpanded = true },
                                            onPlayPauseClick = { app.playerController.togglePlayPause() },
                                            style = themeConfig.miniPlayerStyle
                                        )
                                    }

                                    NavigationBar {
                                        NavigationBarItem(
                                            selected = selectedTab == 0,
                                            onClick = { selectedTab = 0 },
                                            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                                            label = { Text("Home") }
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 1,
                                            onClick = { selectedTab = 1 },
                                            icon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                                            label = { Text("Search") }
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 2,
                                            onClick = { selectedTab = 2 },
                                            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "Library") },
                                            label = { Text("Library") }
                                        )
                                        NavigationBarItem(
                                            selected = selectedTab == 3,
                                            onClick = { selectedTab = 3 },
                                            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                                            label = { Text("Settings") }
                                        )
                                    }
                                }
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                when (selectedTab) {
                                    0 -> HomeScreen(
                                        viewModel = homeViewModel,
                                        onTrackClick = { track, playlist ->
                                            playTrack(track)
                                        }
                                    )
                                    1 -> SearchScreen(
                                        viewModel = searchViewModel,
                                        onTrackClick = { track ->
                                            playTrack(track)
                                        }
                                    )
                                    2 -> LibraryScreen(
                                        viewModel = libraryViewModel,
                                        onTrackClick = { track, list ->
                                            playTrack(track)
                                        }
                                    )
                                    3 -> SettingsScreen(
                                        viewModel = settingsViewModel
                                    )
                                }
                            }
                        }

                        // Full Screen Player Sheet
                        AnimatedVisibility(
                            visible = isPlayerExpanded,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val lyrics by playerViewModel.lyrics.collectAsState()
                            val lyricsOffsetMs by playerViewModel.lyricsOffsetMs.collectAsState()
                            val abLoopState by playerViewModel.abLoopState.collectAsState()
                            val audioOutputDevice by app.audioOutputRouteManager.currentOutputDevice.collectAsState()
                            val visualizerData by com.nexus.tune.core.player.visualizer.VisualizerManager.instance.visualizerData.collectAsState()
                            LaunchedEffect(playbackState.currentTrack) {
                                playbackState.currentTrack?.let { playerViewModel.loadLyrics(it) }
                            }
                            PlayerScreen(
                                playbackState = playbackState,
                                onPlayPauseClick = { app.playerController.togglePlayPause() },
                                onSeek = { pos -> app.playerController.seekTo(pos) },
                                onDismiss = { isPlayerExpanded = false },
                                layout = themeConfig.playerLayout,
                                lyrics = lyrics,
                                lyricsOffsetMs = lyricsOffsetMs,
                                audioOutputDevice = audioOutputDevice,
                                visualizerData = visualizerData,
                                visualizerMode = themeConfig.visualizerMode,
                                onAdjustLyricsOffset = { playerViewModel.adjustLyricsOffset(it) },
                                onResetLyricsOffset = { playerViewModel.resetLyricsOffset() },
                                onSetPlaybackSpeed = { app.playerController.setPlaybackSpeed(it) },
                                onSetVisualizerMode = { settingsViewModel.setVisualizerMode(it) },
                                abLoopState = abLoopState,
                                onSetABPointA = { playerViewModel.setABLoopPointA() },
                                onSetABPointB = { playerViewModel.setABLoopPointB() },
                                onToggleABLoop = { playerViewModel.toggleABLoop() },
                                onClearABLoop = { playerViewModel.clearABLoop() },
                                seekbarStyle = themeConfig.seekbarStyle,
                                onSetSemitones = { app.playerController.setPlaybackSemitones(it) },
                                onTransposeUp = { app.playerController.transposeUp() },
                                onTransposeDown = { app.playerController.transposeDown() },
                                onResetPitch = { app.playerController.resetPitch() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun playTrack(track: Track) {
        app.gaplessPreloadManager.clear()
        lifecycleScope.launch {
            when (val streamResult = app.resolveStreamUseCase(track.id)) {
                is MusicResult.Success -> {
                    app.playerController.playTrack(track, streamResult.data.url)
                    app.musicRepository.addToHistory(track)
                }
                is MusicResult.Failure -> {
                    // Fallback to fake or direct URL if available
                    if (track.streamUrl != null) {
                        app.playerController.playTrack(track)
                    }
                }
            }
        }
    }

    private fun negotiate120HzDisplayMode() {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        } ?: return

        val highestRefreshMode = display.supportedModes.maxByOrNull { it.refreshRate }
        if (highestRefreshMode != null) {
            val params = window.attributes
            params.preferredDisplayModeId = highestRefreshMode.modeId
            window.attributes = params
        }
    }
}

