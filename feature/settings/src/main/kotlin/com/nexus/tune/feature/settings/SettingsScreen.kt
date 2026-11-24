package com.nexus.tune.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.tune.core.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val equalizerPreset by viewModel.equalizerPreset.collectAsState()
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsState()
    val listeningStats by viewModel.listeningStats.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // --- Appearance & Theming ---
            item {
                Text(
                    text = "Appearance & Theming",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Dark Theme") },
                    supportingContent = { Text("Toggle light and dark color schemes") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("AMOLED Pure Black") },
                    supportingContent = { Text("Deep black background for OLED screens") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.isAmoled,
                            onCheckedChange = { viewModel.toggleAmoled(it) }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shape Scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeConfig.shapePreset == ShapePreset.SOFT_ROUNDED,
                        onClick = { viewModel.setShapePreset(ShapePreset.SOFT_ROUNDED) },
                        label = { Text("Rounded") }
                    )
                    FilterChip(
                        selected = themeConfig.shapePreset == ShapePreset.FULL_EXPRESSIVE,
                        onClick = { viewModel.setShapePreset(ShapePreset.FULL_EXPRESSIVE) },
                        label = { Text("Expressive") }
                    )
                    FilterChip(
                        selected = themeConfig.shapePreset == ShapePreset.SHARP,
                        onClick = { viewModel.setShapePreset(ShapePreset.SHARP) },
                        label = { Text("Sharp") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Audio Visualizer Style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisualizerMode.values().forEach { mode ->
                        FilterChip(
                            selected = themeConfig.visualizerMode == mode,
                            onClick = { viewModel.setVisualizerMode(mode) },
                            label = {
                                Text(
                                    when (mode) {
                                        VisualizerMode.OFF -> "Off"
                                        VisualizerMode.SPECTRUM_BARS -> "Spectrum"
                                        VisualizerMode.CIRCULAR_WAVE -> "Circular"
                                        VisualizerMode.GLOW_PULSE -> "Glow"
                                    }
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Equalizer & DSP Presets ---
            item {
                Text(
                    text = "Equalizer & Sound FX",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Hardware Equalizer & Bass Boost") },
                    supportingContent = { Text("5-band hardware DSP audio enhancement") },
                    trailingContent = {
                        Switch(
                            checked = equalizerEnabled,
                            onCheckedChange = { viewModel.toggleEqualizer(it) }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DSP Presets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EqualizerPreset.values().forEach { preset ->
                        FilterChip(
                            selected = equalizerPreset == preset,
                            onClick = { viewModel.setEqualizerPreset(preset) },
                            label = {
                                Text(
                                    preset.name.replace("_", " ")
                                        .lowercase()
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ListItem(
                    headlineContent = { Text("Volume Normalization (ReplayGain)") },
                    supportingContent = { Text("Normalize loudness across songs using DSP enhancement") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.volumeNormalizationEnabled,
                            onCheckedChange = { viewModel.toggleVolumeNormalization(it) }
                        )
                    }
                )

                if (themeConfig.volumeNormalizationEnabled) {
                    Text(
                        text = "Target Boost",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(200 to "Gentle (+2dB)", 400 to "Standard (+4dB)", 600 to "Max (+6dB)").forEach { (gain, label) ->
                            FilterChip(
                                selected = themeConfig.targetGainmB == gain,
                                onClick = { viewModel.setTargetGain(gain) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ListItem(
                        headlineContent = { Text("Per-Track LUFS Calibration") },
                        supportingContent = { Text("Automatically adjust gain according to track loudness metadata") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.perTrackGainEnabled,
                                onCheckedChange = { viewModel.togglePerTrackGain(it) }
                            )
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Hi-Res Audio & Bit-Perfect Mode ---
                Text(
                    text = "Hi-Res Audio & Bit-Perfect Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Bit-Perfect USB Output") },
                    supportingContent = { Text("Bypass Android system mixer for connected USB DACs") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.bitPerfectUsbAudioEnabled,
                            onCheckedChange = { viewModel.toggleBitPerfectUsbAudio(it) }
                        )
                    }
                )

                Text(
                    text = "Force Hi-Res Resampling",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Auto",
                        48000 to "48 kHz",
                        96000 to "96 kHz",
                        192000 to "192 kHz"
                    ).forEach { (rate, label) ->
                        FilterChip(
                            selected = themeConfig.forceHiResSamplingRate == rate,
                            onClick = { viewModel.setForceHiResSamplingRate(rate) },
                            label = { Text(label) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Gapless Buffer Pre-Loading ---
                Text(
                    text = "Gapless Buffer Pre-Loading",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Pre-Buffer Upcoming Track") },
                    supportingContent = { Text("Prime audio decoder pipeline before track end to eliminate gap latency") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.gaplessPreloadEnabled,
                            onCheckedChange = { viewModel.toggleGaplessPreload(it) }
                        )
                    }
                )

                if (themeConfig.gaplessPreloadEnabled) {
                    Text(
                        text = "Pre-Buffer Window",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            3 to "3 seconds",
                            5 to "5s (Default)",
                            10 to "10 seconds"
                        ).forEach { (seconds, label) ->
                            FilterChip(
                                selected = themeConfig.gaplessPreloadSeconds == seconds,
                                onClick = { viewModel.setGaplessPreloadSeconds(seconds) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Sleep Timer ---
            item {
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                if (sleepTimerState.isActive) {
                    val formattedTime = if (sleepTimerState.isEndOfTrack) {
                        "Pausing at end of current track"
                    } else if (sleepTimerState.isFadingOut) {
                        val minutes = sleepTimerState.remainingSeconds / 60
                        val seconds = sleepTimerState.remainingSeconds % 60
                        String.format("%02d:%02d (Fading out %d%%)", minutes, seconds, (sleepTimerState.fadeVolumeMultiplier * 100).toInt())
                    } else {
                        val minutes = sleepTimerState.remainingSeconds / 60
                        val seconds = sleepTimerState.remainingSeconds % 60
                        String.format("%02d:%02d remaining", minutes, seconds)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Timer Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelSleepTimer() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        AssistChip(
                            onClick = { viewModel.startSleepTimer(mins) },
                            label = { Text("${mins}m") },
                            leadingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    AssistChip(
                        onClick = { viewModel.startSleepTimerEndOfTrack() },
                        label = { Text("End of Track") },
                        leadingIcon = { Icon(Icons.Rounded.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gentle Fade-Out Duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "Instant", 10 to "10s", 30 to "30s", 60 to "60s").forEach { (sec, label) ->
                        FilterChip(
                            selected = themeConfig.sleepTimerFadeSeconds == sec,
                            onClick = { viewModel.setSleepTimerFadeSeconds(sec) },
                            label = { Text(label) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Listening Analytics ---
            item {
                Text(
                    text = "Listening Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                val stats = listeningStats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Plays", style = MaterialTheme.typography.bodySmall)
                        Text("${stats.totalPlays}", style = MaterialTheme.typography.headlineMedium)

                        if (stats.topArtists.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Top Artists: " + stats.topArtists.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Time-of-Day: Morning ${stats.morningPlays} | Afternoon ${stats.afternoonPlays} | Evening ${stats.eveningPlays} | Night ${stats.nightPlays}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Player Layout ---
            item {
                Text(
                    text = "Player Layout",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeConfig.playerLayout == PlayerLayout.ART_FORWARD,
                        onClick = { viewModel.setPlayerLayout(PlayerLayout.ART_FORWARD) },
                        label = { Text("Art Forward") }
                    )
                    FilterChip(
                        selected = themeConfig.playerLayout == PlayerLayout.MINIMALIST,
                        onClick = { viewModel.setPlayerLayout(PlayerLayout.MINIMALIST) },
                        label = { Text("Minimalist") }
                    )
                    FilterChip(
                        selected = themeConfig.playerLayout == PlayerLayout.VINYL_RECORD,
                        onClick = { viewModel.setPlayerLayout(PlayerLayout.VINYL_RECORD) },
                        label = { Text("Vinyl") }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Playback Transitions & Crossfade ---
            item {
                Text(
                    text = "Audio Crossfade & Transitions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = "Dynamic Crossfade Duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0 to "Off (Gapless)",
                        2 to "2s",
                        4 to "4s",
                        6 to "6s",
                        8 to "8s",
                        12 to "12s"
                    ).forEach { (sec, label) ->
                        FilterChip(
                            selected = themeConfig.crossfadeSeconds == sec,
                            onClick = { viewModel.setCrossfade(sec) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Auto-Skip Non-Music & Silence") },
                    supportingContent = { Text("Skip sponsor segments, intros, and silent breaks") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.autoSkipSegments,
                            onCheckedChange = { viewModel.toggleAutoSkipSegments(it) }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Infinite Auto-Play (Endless Mix)") },
                    supportingContent = { Text("Automatically discover and append similar tracks when queue reaches the end") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.infiniteAutoPlayEnabled,
                            onCheckedChange = { viewModel.toggleInfiniteAutoPlay(it) }
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Storage & Cache Management ---
            item {
                Text(
                    text = "Storage & Cache Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Cache Size Budget") },
                    supportingContent = { Text("Automatically prune oldest stream cache when exceeding budget") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500 to "500MB", 1000 to "1GB", 2000 to "2GB", 5000 to "5GB", 0 to "Unlimited").forEach { (budget, label) ->
                        FilterChip(
                            selected = themeConfig.cacheBudgetMb == budget,
                            onClick = { viewModel.setCacheBudget(budget) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Clear Stream Cache") },
                    supportingContent = { Text("Reclaim local storage without removing downloaded tracks") },
                    trailingContent = {
                        OutlinedButton(onClick = { viewModel.clearCache() }) {
                            Text("Clear")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Clear Lyrics Cache") },
                    supportingContent = { Text("Reclaim space used by offline cached song lyrics") },
                    trailingContent = {
                        OutlinedButton(onClick = { viewModel.clearLyricsCache() }) {
                            Text("Clear")
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Data & Backup ---
            item {
                Text(
                    text = "Data & Backup",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Backup to JSON") },
                    supportingContent = { Text("Export playlists, favorites, and settings") }
                )

                ListItem(
                    headlineContent = { Text("Restore from JSON") },
                    supportingContent = { Text("Import previous NexusTune backup") }
                )

                ListItem(
                    headlineContent = { Text("Import from NewPipe") },
                    supportingContent = { Text("Import playlists and history from NewPipe export") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- About ---
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("NexusTune v2.7.0") },
                    supportingContent = { Text("NewPipeExtractor v0.27.1 | Material 3 Expressive | 120Hz Ultra-fluid | Offline Lyrics Cache & Calibration Memory") }
                )
            }
        }
    }
}
