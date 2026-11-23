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
                    headlineContent = { Text("NexusTune v1.8.0") },
                    supportingContent = { Text("NewPipeExtractor v0.27.1 | Material 3 Expressive | 120Hz Ultra-fluid | Adaptive Speed Engine") }
                )
            }
        }
    }
}
