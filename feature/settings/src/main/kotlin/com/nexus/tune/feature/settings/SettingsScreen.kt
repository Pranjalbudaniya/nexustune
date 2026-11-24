package com.nexus.tune.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.tune.core.model.*
import com.nexus.tune.core.ui.EqCurveVisualizer

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
    val soundDoseInfo by viewModel.soundDoseInfo.collectAsState()
    val preferredQuality by viewModel.preferredQuality.collectAsState()
    val isMeteredConnection by viewModel.isMeteredConnection.collectAsState()
    val ambientState by viewModel.ambientState.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val alarmFadedVolume by viewModel.alarmFadedVolume.collectAsState()
    val listeningStreak by viewModel.listeningStreak.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val timeCapsuleRecap by viewModel.timeCapsuleRecap.collectAsState()
    val sonicDnaProfile by viewModel.sonicDnaProfile.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val customEqProfiles by viewModel.customEqProfiles.collectAsState()
    val closestPresetMatch by viewModel.closestPresetMatch.collectAsState()
    var showSaveProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisualizerMode.entries.forEach { mode ->
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
                                        VisualizerMode.STEREO_VU -> "Stereo VU"
                                        VisualizerMode.LIQUID_WAVE -> "Liquid Wave"
                                    }
                                )
                            }
                        )
                    }
                }

                if (themeConfig.visualizerMode != VisualizerMode.OFF) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Visualizer Color Palette",
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
                        VisualizerPalette.entries.forEach { palette ->
                            FilterChip(
                                selected = themeConfig.visualizerPalette == palette,
                                onClick = { viewModel.setVisualizerPalette(palette) },
                                label = { Text(palette.displayName) }
                            )
                        }
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

                if (equalizerEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "5-Band Frequency Tuning",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${closestPresetMatch.first.name} (${closestPresetMatch.second}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Interactive Bézier Frequency Curve
                            EqCurveVisualizer(
                                bands = themeConfig.customEqBands,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.nexus.tune.core.domain.audio.EqCurveCalculator.formatBandsSummary(themeConfig.customEqBands),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row {
                                    TextButton(onClick = { showSaveProfileDialog = true }) {
                                        Text("Save Curve")
                                    }
                                    TextButton(onClick = { viewModel.resetCustomEqBands() }) {
                                        Text("Reset Flat")
                                    }
                                }
                            }

                            // Custom Saved Profiles Row
                            if (customEqProfiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Custom Profiles",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    customEqProfiles.forEach { profile ->
                                        InputChip(
                                            selected = themeConfig.customEqBands == profile.bands,
                                            onClick = { viewModel.applyCustomEqProfile(profile) },
                                            label = { Text(profile.name, style = MaterialTheme.typography.labelSmall) },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = "Delete profile",
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { viewModel.deleteCustomEqProfile(profile.id) }
                                                )
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
                            val currentBands = themeConfig.customEqBands

                            bandFrequencies.forEachIndexed { index, freqName ->
                                val levelmB = currentBands.getOrElse(index) { 0 }
                                val gainDb = levelmB / 100f
                                val formattedGain = if (gainDb > 0) "+%.1f dB".format(gainDb) else "%.1f dB".format(gainDb)

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = freqName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formattedGain,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (levelmB != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Slider(
                                        value = levelmB.toFloat(),
                                        onValueChange = { viewModel.setCustomEqBandLevel(index, it.toInt()) },
                                        valueRange = -1000f..1000f,
                                        steps = 19,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Auto-Genre EQ Adaptation") },
                    supportingContent = { Text("Automatically adapt EQ profile based on track genre keywords") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.autoGenreEqEnabled,
                            onCheckedChange = { viewModel.toggleAutoGenreEq(it) }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Audio Loudness Normalization & Dynamic Peak Limiter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Auto-levels stream loudness to prevent volume jumps; soft-knee peak limiter prevents clipping distortion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LoudnessNormalizationMode.values().forEach { mode ->
                        FilterChip(
                            selected = themeConfig.loudnessConfig.mode == mode,
                            onClick = { viewModel.setLoudnessMode(mode) },
                            label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (themeConfig.loudnessConfig.mode != LoudnessNormalizationMode.OFF) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Target Loudness (LUFS)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            -11.0f to "-11 LUFS (Club)",
                            -14.0f to "-14 LUFS (Standard)",
                            -18.0f to "-18 LUFS (Dynamic)",
                            -23.0f to "-23 LUFS (EBU R128)"
                        ).forEach { (lufs, label) ->
                            FilterChip(
                                selected = kotlin.math.abs(themeConfig.loudnessConfig.targetLufs - lufs) < 0.1f,
                                onClick = { viewModel.setTargetLufs(lufs) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pre-Amp Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pre-Amp Gain: ${if (themeConfig.loudnessConfig.preAmpGainDb >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", themeConfig.loudnessConfig.preAmpGainDb)} dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (themeConfig.loudnessConfig.preAmpGainDb != 0f) {
                            TextButton(onClick = { viewModel.setPreAmpGainDb(0f) }) {
                                Text("Reset 0 dB", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Slider(
                        value = themeConfig.loudnessConfig.preAmpGainDb,
                        onValueChange = { viewModel.setPreAmpGainDb(it) },
                        valueRange = -6.0f..6.0f,
                        steps = 23,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Headroom & Mode Status Card
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Limiter Status: Soft-Knee Active (0.95 Knee)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = themeConfig.loudnessConfig.mode.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = themeConfig.loudnessConfig.mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DJ Sound Effects & Transformation Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "DJ Sound Effects & Transformation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time aesthetic pitch, tempo, and acoustic soundstage modulation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.nexus.tune.core.model.DjPreset.values().forEach { preset ->
                            val isSelected = themeConfig.djFxConfig.preset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDjPreset(preset) },
                                label = { Text(preset.displayName) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ListItem(
                        headlineContent = { Text("Remember DJ FX Across Tracks") },
                        supportingContent = { Text("Preserve active tempo, pitch, and soundstage between songs instead of resetting to standard") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.djRememberAcrossTracks,
                                onCheckedChange = { viewModel.setDjRememberAcrossTracks(it) }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Karaoke Sing-Along Studio Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Karaoke Sing-Along Studio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attenuates center-channel lead vocals and enables real-time scrolling lyric prompters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ListItem(
                        headlineContent = { Text("Lead Vocal Attenuation") },
                        supportingContent = { Text("Applies calibrated center-channel notch filter preserving stereo backing track") },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ListItem(
                    headlineContent = { Text("Spatial Audio (3D Virtualizer)") },
                    supportingContent = { Text("Simulate binaural room reflections and expansive soundstage") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.spatialAudioEnabled,
                            onCheckedChange = { viewModel.toggleSpatialAudio(it) }
                        )
                    }
                )

                if (themeConfig.spatialAudioEnabled) {
                    Text(
                        text = "Acoustic Stage Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpatialAudioStage.values().filter { it != SpatialAudioStage.OFF }.forEach { stage ->
                            FilterChip(
                                selected = themeConfig.spatialAudioStage == stage,
                                onClick = { viewModel.setSpatialAudioStage(stage) },
                                label = {
                                    Text(
                                        stage.name.replace("_", " ")
                                            .lowercase()
                                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    )
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- Headphone Soundstage & Target Calibration ---
                Text(
                    text = "Headphone Target & Crossfeed Compensation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Acoustic Target Correction") },
                    supportingContent = { Text("Apply studio Harman & Diffuse-Field curve compensation to eliminate headphone acoustic resonance") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.headphoneCorrectionConfig.isEnabled,
                            onCheckedChange = { viewModel.setHeadphoneCorrectionEnabled(it) }
                        )
                    }
                )

                if (themeConfig.headphoneCorrectionConfig.isEnabled) {
                    Text(
                        text = "Acoustic Target Curve",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeadphoneTargetCurve.entries.forEach { curve ->
                            FilterChip(
                                selected = themeConfig.headphoneCorrectionConfig.targetCurve == curve,
                                onClick = { viewModel.setHeadphoneTargetCurve(curve) },
                                label = { Text(curve.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = themeConfig.headphoneCorrectionConfig.targetCurve.tonalCharacter,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    ListItem(
                        headlineContent = { Text("Binaural Crossfeed Simulation") },
                        supportingContent = { Text("Subtle ITD/ILD ear-to-ear crossfeed to eliminate 'in-head' stereo fatigue") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.headphoneCorrectionConfig.crossfeedEnabled,
                                onCheckedChange = { viewModel.setHeadphoneCrossfeedEnabled(it) }
                            )
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- Stereo Field & Soundstage Studio ---
                Text(
                    text = "Stereo Field & Soundstage Studio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("M/S Stereo Soundstage Widener") },
                    supportingContent = { Text("Mid/Side matrix soundstage expansion and Haas psychoacoustic spatialization") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.stereoWidthConfig.isEnabled,
                            onCheckedChange = { viewModel.setStereoWidthEnabled(it) }
                        )
                    }
                )

                if (themeConfig.stereoWidthConfig.isEnabled) {
                    Text(
                        text = "Soundstage Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StereoWidthMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.stereoWidthConfig.mode == mode,
                                onClick = { viewModel.setStereoWidthMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = themeConfig.stereoWidthConfig.mode.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Width: ${themeConfig.stereoWidthConfig.customWidthPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (themeConfig.stereoWidthConfig.customWidthPercent > 100) "Expanded Field" else if (themeConfig.stereoWidthConfig.customWidthPercent < 100) "Focused Field" else "Neutral Stereo",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.stereoWidthConfig.customWidthPercent.toFloat(),
                        onValueChange = { viewModel.setStereoWidthPercent(it.toInt()) },
                        valueRange = 0f..200f,
                        steps = 19
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Haas Acoustic Delay: ${themeConfig.stereoWidthConfig.haasDelayMs} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0f to "0ms (Off)", 4.5f to "4.5ms (Subtle)", 10f to "10ms (Medium)", 18f to "18ms (Wide)").forEach { (ms, label) ->
                            FilterChip(
                                selected = themeConfig.stereoWidthConfig.haasDelayMs == ms,
                                onClick = { viewModel.setStereoHaasDelayMs(ms) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Hearing Protection & Safe Listening ---
                Text(
                    text = "Hearing Protection & Safe Listening",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Safe Sound Exposure Guard") },
                    supportingContent = {
                        val dose = soundDoseInfo.dosePercent.toInt()
                        Text("Weekly sound dose: $dose% (WHO 80 dBA standard)")
                    },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.safeListeningAlertEnabled,
                            onCheckedChange = { viewModel.toggleSafeListeningAlert(it) }
                        )
                    }
                )

                Text(
                    text = "Maximum Volume Limiter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        70 to "70% (Safe)",
                        80 to "80%",
                        90 to "90%",
                        100 to "100% (Off)"
                    ).forEach { (limit, label) ->
                        FilterChip(
                            selected = themeConfig.volumeLimitPercent == limit,
                            onClick = { viewModel.setVolumeLimit(limit) },
                            label = { Text(label) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Audio Stream Quality & Data Saver ---
                Text(
                    text = "Audio Stream Quality & Data Saver",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMeteredConnection) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isMeteredConnection) Icons.Rounded.SignalCellularAlt else Icons.Rounded.Wifi,
                            contentDescription = null,
                            tint = if (isMeteredConnection) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                text = if (isMeteredConnection) "Cellular Data Detected" else "Unmetered Network / Wi-Fi",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isMeteredConnection) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isMeteredConnection) {
                                    if (preferredQuality.equals("AUTO", ignoreCase = true)) "Adaptive Data Saver active (low-bandwidth streaming)" else "Streaming at $preferredQuality"
                                } else {
                                    if (preferredQuality.equals("AUTO", ignoreCase = true)) "Full high-fidelity streaming active" else "Streaming at $preferredQuality"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isMeteredConnection) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Streaming Quality Preference",
                    style = MaterialTheme.typography.bodySmall,
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
                        "AUTO" to "Auto (Adaptive)",
                        "HIGH" to "High (160-256k)",
                        "MEDIUM" to "Standard (128k)",
                        "LOW" to "Data Saver (48-70k)"
                    ).forEach { (qualityKey, label) ->
                        FilterChip(
                            selected = preferredQuality.equals(qualityKey, ignoreCase = true),
                            onClick = { viewModel.setPreferredQuality(qualityKey) },
                            label = { Text(label) }
                        )
                    }
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

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Shake to Extend") },
                    supportingContent = { Text("Gently shake phone to add +${themeConfig.shakeExtendMinutes}m when falling asleep") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.shakeToExtendSleepTimerEnabled,
                            onCheckedChange = { viewModel.toggleShakeToExtend(it) }
                        )
                    }
                )

                if (themeConfig.shakeToExtendSleepTimerEnabled) {
                    Text(
                        text = "Shake Extension Duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5 to "+5 min", 10 to "+10 min", 15 to "+15 min", 20 to "+20 min").forEach { (mins, label) ->
                            FilterChip(
                                selected = themeConfig.shakeExtendMinutes == mins,
                                onClick = { viewModel.setShakeExtendMinutes(mins) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Acoustic Sleep Drift & Induction Profile",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Progressively rolls off stimulating high frequencies and decelerates tempo during the session",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SleepDriftProfile.entries.forEach { profile ->
                        FilterChip(
                            selected = themeConfig.sleepDriftProfile == profile,
                            onClick = { viewModel.setSleepDriftProfile(profile) },
                            label = { Text(profile.displayName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = themeConfig.sleepDriftProfile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Bedtime Sound Handoff") },
                    supportingContent = { Text("Transition to ambient soundscapes when music timer expires") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.bedtimeHandoffEnabled,
                            onCheckedChange = { viewModel.toggleBedtimeHandoff(it) }
                        )
                    }
                )

                if (themeConfig.bedtimeHandoffEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Handoff Sound Profile",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    AmbientSoundType.BROWN_NOISE,
                                    AmbientSoundType.PINK_NOISE,
                                    AmbientSoundType.BINAURAL_DELTA,
                                    AmbientSoundType.BINAURAL_THETA,
                                    AmbientSoundType.SOLFEGGIO_432HZ
                                ).forEach { soundType ->
                                    FilterChip(
                                        selected = themeConfig.bedtimeHandoffSoundType == soundType,
                                        onClick = { viewModel.setBedtimeHandoffSoundType(soundType) },
                                        label = { Text(soundType.displayName) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = themeConfig.bedtimeHandoffVolume,
                                    onValueChange = { viewModel.setBedtimeHandoffVolume(it) },
                                    valueRange = 0.05f..0.8f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(themeConfig.bedtimeHandoffVolume * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Handoff Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(30 to "30m", 60 to "1h", 120 to "2h", 0 to "All Night").forEach { (duration, label) ->
                                    FilterChip(
                                        selected = themeConfig.bedtimeHandoffDurationMinutes == duration,
                                        onClick = { viewModel.setBedtimeHandoffDuration(duration) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Bedtime Ambient Soundscapes & Binaural Waves ---
            item {
                Text(
                    text = "Bedtime Ambient Soundscapes & Binaural Waves",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ambientState.isPlaying)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (ambientState.isPlaying) "Ambient Sound Active" else "Ambient Sound Generator",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (ambientState.isPlaying)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = ambientState.soundType.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (ambientState.isPlaying)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { viewModel.toggleAmbientPlay() },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (ambientState.isPlaying)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = if (ambientState.isPlaying)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = if (ambientState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (ambientState.isPlaying) "Pause Ambient" else "Play Ambient"
                                )
                            }
                        }

                        Text(
                            text = ambientState.soundType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                        )

                        // Ambient Volume Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = ambientState.volume,
                                onValueChange = { viewModel.setAmbientVolume(it) },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(ambientState.volume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sound Profile Chips
                        Text(
                            text = "Sound Profile",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmbientSoundType.values().filter { it != AmbientSoundType.OFF }.forEach { soundType ->
                                FilterChip(
                                    selected = ambientState.soundType == soundType,
                                    onClick = { viewModel.setAmbientSound(soundType) },
                                    label = { Text(soundType.displayName) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Gentle Morning Wake-Up Alarm ---
            item {
                Text(
                    text = "Gentle Morning Wake-Up Alarm",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                val alarmConfig = themeConfig.wakeAlarmConfig

                if (isAlarmRinging) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Alarm Ringing",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Fade-in Volume: ${(alarmFadedVolume * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.snoozeWakeAlarm(10) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Snooze")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.stopWakeAlarm() }
                                    ) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }
                    }
                }

                ListItem(
                    headlineContent = { Text("Morning Wake-Up Alarm") },
                    supportingContent = {
                        Text(
                            if (alarmConfig.isEnabled)
                                String.format("Scheduled for %02d:%02d with %ds fade-in", alarmConfig.hour, alarmConfig.minute, alarmConfig.fadeInSeconds)
                            else
                                "Gradually wake up with ascending audio volume fade-in"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = alarmConfig.isEnabled,
                            onCheckedChange = { isChecked ->
                                viewModel.updateWakeAlarmConfig(alarmConfig.copy(isEnabled = isChecked))
                            }
                        )
                    }
                )

                if (alarmConfig.isEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Alarm Time Selector
                            Text(
                                text = "Wake-Up Time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    6 to 0,
                                    6 to 30,
                                    7 to 0,
                                    7 to 30,
                                    8 to 0,
                                    8 to 30
                                ).forEach { (h, m) ->
                                    val isSelected = alarmConfig.hour == h && alarmConfig.minute == m
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.updateWakeAlarmConfig(alarmConfig.copy(hour = h, minute = m))
                                        },
                                        label = {
                                            Text(String.format("%02d:%02d", h, m))
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Rounded.Alarm, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fade-In Duration
                            Text(
                                text = "Volume Fade-In Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    30 to "30 sec",
                                    60 to "1 min",
                                    120 to "2 min",
                                    300 to "5 min"
                                ).forEach { (seconds, label) ->
                                    FilterChip(
                                        selected = alarmConfig.fadeInSeconds == seconds,
                                        onClick = {
                                            viewModel.updateWakeAlarmConfig(alarmConfig.copy(fadeInSeconds = seconds))
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Audio Source
                            Text(
                                text = "Audio Source",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    WakeAlarmSource.CURRENT_QUEUE to "Queue",
                                    WakeAlarmSource.LIKED_SONGS to "Favorites",
                                    WakeAlarmSource.AMBIENT_SOUND to "Ambient"
                                ).forEach { (src, label) ->
                                    FilterChip(
                                        selected = alarmConfig.source == src,
                                        onClick = {
                                            viewModel.updateWakeAlarmConfig(alarmConfig.copy(source = src))
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Target Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = alarmConfig.targetVolume,
                                    onValueChange = {
                                        viewModel.updateWakeAlarmConfig(alarmConfig.copy(targetVolume = it))
                                    },
                                    valueRange = 0.2f..1.0f,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(alarmConfig.targetVolume * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Preview Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isAlarmRinging) {
                                    FilledTonalButton(onClick = { viewModel.stopWakeAlarm() }) {
                                        Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Stop Preview")
                                    }
                                } else {
                                    OutlinedButton(onClick = { viewModel.testTriggerWakeAlarm() }) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Preview Wake-Up")
                                    }
                                }
                            }
                        }
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
                            text = "Peak: ${stats.peakPeriod} | Morning ${stats.morningPlays} | Afternoon ${stats.afternoonPlays} | Evening ${stats.eveningPlays} | Night ${stats.nightPlays}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (stats.topGenres.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Top Genres",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                stats.topGenres.forEach { genreShare ->
                                    FilterChip(
                                        selected = true,
                                        onClick = {},
                                        label = { Text("${genreShare.genre} ${genreShare.percentage}%") }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Daily Streak & Milestones ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔥 ${listeningStreak.currentStreakDays} Day Streak",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Best: ${listeningStreak.longestStreakDays} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Listening Milestones",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            milestones.forEach { milestone ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (milestone.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (milestone.isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                                    contentDescription = null,
                                                    tint = if (milestone.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = milestone.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (milestone.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (milestone.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "${milestone.currentCount}/${milestone.targetCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = milestone.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!milestone.isUnlocked) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { milestone.progressPercent },
                                                modifier = Modifier.fillMaxWidth().height(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time-Capsule Recap Preview
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✨ ${timeCapsuleRecap.periodName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "~${timeCapsuleRecap.totalMinutesListened} minutes listened • Peak period: ${timeCapsuleRecap.peakPeriod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                if (timeCapsuleRecap.topArtists.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Top Artist: ${timeCapsuleRecap.topArtists.first()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sonic DNA Card
                        com.nexus.tune.core.ui.SonicDnaCard(
                            profile = sonicDnaProfile,
                            onShareClick = {
                                val text = viewModel.getShareableSonicDnaText()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Sonic DNA Profile", text)
                                clipboard?.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Sonic DNA copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            }
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seekbar Style",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeConfig.seekbarStyle == SeekbarStyle.WAVEFORM,
                        onClick = { viewModel.setSeekbarStyle(SeekbarStyle.WAVEFORM) },
                        label = { Text("Dynamic Waveform") }
                    )
                    FilterChip(
                        selected = themeConfig.seekbarStyle == SeekbarStyle.CLASSIC_SLIDER,
                        onClick = { viewModel.setSeekbarStyle(SeekbarStyle.CLASSIC_SLIDER) },
                        label = { Text("Classic Slider") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Key on Track Change",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reset musical key and pitch transposition to original when song changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = themeConfig.resetPitchOnTrackChange,
                        onCheckedChange = { viewModel.toggleResetPitchOnTrackChange(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Artist-Spaced Shuffle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Evenly distributes tracks from the same artist across the queue to avoid clustering",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = themeConfig.smartShuffleEnabled,
                        onCheckedChange = { viewModel.toggleSmartShuffle(it) }
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

                if (themeConfig.crossfadeSeconds > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crossfade Curve",
                        style = MaterialTheme.typography.bodySmall,
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
                            CrossfadeCurve.EQUAL_POWER to "Equal Power (DJ)",
                            CrossfadeCurve.LINEAR to "Linear",
                            CrossfadeCurve.EXPONENTIAL to "Exponential"
                        ).forEach { (curve, label) ->
                            FilterChip(
                                selected = themeConfig.crossfadeCurve == curve,
                                onClick = { viewModel.setCrossfadeCurve(curve) },
                                label = { Text(label) }
                            )
                        }
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

                // Smart Offline Auto-Stash Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Smart Offline Auto-Stash",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Predictively auto-downloads favorite and high-rotation tracks within a dedicated offline storage quota",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ListItem(
                        headlineContent = { Text("Enable Auto-Stash") },
                        supportingContent = { Text("Automatically download top tracks for offline commutes and travel") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.autoStashConfig.isEnabled,
                                onCheckedChange = { viewModel.setAutoStashEnabled(it) }
                            )
                        }
                    )

                    if (themeConfig.autoStashConfig.isEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto-Stash Storage Quota",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500 to "500MB", 1000 to "1GB", 2000 to "2GB", 5000 to "5GB").forEach { (budget, label) ->
                                val isSelected = themeConfig.autoStashConfig.storageBudgetMb == budget
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setAutoStashBudgetMb(budget) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Stash Strategy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.nexus.tune.core.model.AutoStashStrategy.entries.forEach { strat ->
                                val isSelected = themeConfig.autoStashConfig.strategy == strat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setAutoStashStrategy(strat) },
                                    label = { Text(strat.displayName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ListItem(
                            headlineContent = { Text("Wi-Fi Only Sync") },
                            supportingContent = { Text("Only auto-stash tracks when connected to unmetered Wi-Fi") },
                            trailingContent = {
                                Switch(
                                    checked = themeConfig.autoStashConfig.wifiOnly,
                                    onCheckedChange = { viewModel.setAutoStashWifiOnly(it) }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                ListItem(
                    headlineContent = { Text("Clean Library Duplicates") },
                    supportingContent = { Text("Scan and remove duplicate entries from liked tracks") },
                    trailingContent = {
                        OutlinedButton(onClick = { viewModel.deduplicateLikedTracks() }) {
                            Text("Clean")
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

                val importStatus by viewModel.backupImportStatus.collectAsState()
                if (importStatus != null) {
                    Text(
                        text = importStatus ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                ListItem(
                    headlineContent = { Text("Backup to JSON") },
                    supportingContent = { Text("Export playlists, favorites, and settings") },
                    trailingContent = {
                        OutlinedButton(onClick = { viewModel.exportBackup() }) {
                            Text("Export")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Restore from JSON") },
                    supportingContent = { Text("Import previous NexusTune backup") },
                    trailingContent = {
                        OutlinedButton(onClick = {
                            viewModel.backupExportResult.value?.let { viewModel.importBackup(it) }
                                ?: viewModel.exportBackup()
                        }) {
                            Text("Restore")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Import from NewPipe") },
                    supportingContent = { Text("Import playlists and streams from NewPipe export") },
                    trailingContent = {
                        OutlinedButton(onClick = {
                            viewModel.importFromNewPipe("{\"playlists\":[]}")
                        }) {
                            Text("Import")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Export Favorites as M3U") },
                    supportingContent = { Text("Export liked tracks to standard Extended M3U8 format") },
                    trailingContent = {
                        OutlinedButton(onClick = { viewModel.exportLikedTracksToM3u() }) {
                            Text("Export M3U")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Import M3U / M3U8 Playlist") },
                    supportingContent = { Text("Import external playlists from M3U / M3U8 format") },
                    trailingContent = {
                        OutlinedButton(onClick = {
                            viewModel.importPlaylistFromM3u(
                                "#EXTM3U\n#PLAYLIST:Imported Mix\n#EXTINF:214,Synthetix - Midnight Odyssey\nhttps://www.youtube.com/watch?v=fake_1",
                                "Imported Mix"
                            )
                        }) {
                            Text("Import M3U")
                        }
                    }
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
                    headlineContent = { Text("NexusTune v6.0.0") },
                    supportingContent = { Text("Camelot Harmonic Mixing & BPM Intelligence | Dynamic Smart Playlists | 120Hz Ultra-Fluid M3 Expressive Audio Engine") }
                )
            }
        }

        if (showSaveProfileDialog) {
            AlertDialog(
                onDismissRequest = { showSaveProfileDialog = false },
                title = { Text("Save Custom EQ Profile") },
                text = {
                    Column {
                        Text(
                            text = "Current Bands: ${com.nexus.tune.core.domain.audio.EqCurveCalculator.formatBandsSummary(themeConfig.customEqBands)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newProfileName,
                            onValueChange = { newProfileName = it },
                            label = { Text("Profile Name") },
                            placeholder = { Text("e.g. Sony WH-1000XM5, Car Bass") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newProfileName.isNotBlank()) {
                                viewModel.saveCustomEqProfile(newProfileName, themeConfig.customEqBands)
                                newProfileName = ""
                            }
                            showSaveProfileDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
