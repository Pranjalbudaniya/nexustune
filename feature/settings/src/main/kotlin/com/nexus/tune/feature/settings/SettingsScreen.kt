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
import com.nexus.tune.core.domain.audio.InfrasonicFilterEngine
import com.nexus.tune.core.domain.audio.AirBandExciterEngine
import com.nexus.tune.core.domain.audio.SubHarmonicSynthEngine
import com.nexus.tune.core.domain.audio.DeEsserEngine
import com.nexus.tune.core.domain.audio.TruePeakGuardEngine
import com.nexus.tune.core.domain.audio.UltrasonicFilterEngine
import com.nexus.tune.core.domain.audio.TubeSaturationEngine
import com.nexus.tune.core.domain.audio.TransientShaperEngine
import com.nexus.tune.core.domain.audio.DynamicStereoImagerEngine
import com.nexus.tune.core.domain.audio.TapeHysteresisEngine
import com.nexus.tune.core.domain.audio.MainsHumIsolatorEngine
import com.nexus.tune.core.domain.audio.AcousticFeedbackEngine
import com.nexus.tune.core.domain.audio.SoundstageElevationEngine
import com.nexus.tune.core.domain.audio.MonoBassAlignEngine
import com.nexus.tune.core.domain.audio.PreampNoiseFlattenerEngine
import com.nexus.tune.core.domain.audio.SlewRateLimiterEngine
import com.nexus.tune.core.domain.audio.HornDiffuserEngine
import com.nexus.tune.core.domain.audio.BassAttackReshaperEngine
import com.nexus.tune.core.domain.audio.SpatialDecorrelatorEngine
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
            }

            item {
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

                    Spacer(modifier = Modifier.height(10.dp))
                    ListItem(
                        headlineContent = { Text("Mono-Guard Anti-Cancellation") },
                        supportingContent = { Text("Safeguards vocals and punch from phase cancellation on phone/Bluetooth speakers") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.phaseMeterConfig.isEnabled,
                                onCheckedChange = { viewModel.setPhaseGuardEnabled(it) }
                            )
                        }
                    )

                    if (themeConfig.phaseMeterConfig.isEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mono-Guard Protection Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MonoGuardMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeConfig.phaseMeterConfig.guardMode == mode,
                                    onClick = { viewModel.setMonoGuardMode(mode) },
                                    label = { Text(mode.displayName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        val demoMetrics = com.nexus.tune.core.domain.audio.PhaseMeterEngine.calculatePhaseMetrics(
                            left = floatArrayOf(0.7f, 0.4f, 0.9f, 0.6f),
                            right = floatArrayOf(0.65f, 0.45f, 0.85f, 0.55f),
                            guardMode = themeConfig.phaseMeterConfig.guardMode
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = themeConfig.phaseMeterConfig.guardMode.description,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Coeff: +${String.format(java.util.Locale.US, "%.2f", demoMetrics.correlationCoeff)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = demoMetrics.rating.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ListItem(
                        headlineContent = { Text("Audiophile Tilt Equalizer") },
                        supportingContent = { Text("Smooth tonal tilt rotating the entire spectrum around a center pivot") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.tiltEqualizerConfig.isEnabled,
                                onCheckedChange = { viewModel.setTiltEqEnabled(it) }
                            )
                        }
                    )

                    if (themeConfig.tiltEqualizerConfig.isEnabled) {
                        val tiltMetrics = com.nexus.tune.core.domain.audio.TiltEqualizerEngine.calculateSpectralMetrics(themeConfig.tiltEqualizerConfig)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tonal Balance: ${if (themeConfig.tiltEqualizerConfig.tiltDb < 0) "Warm" else if (themeConfig.tiltEqualizerConfig.tiltDb > 0) "Bright" else "Flat"} (${String.format(java.util.Locale.US, "%+.1f dB", themeConfig.tiltEqualizerConfig.tiltDb)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = tiltMetrics.tonalBalanceDescription,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = themeConfig.tiltEqualizerConfig.tiltDb,
                            onValueChange = { viewModel.setTiltEqDb(it) },
                            valueRange = -6.0f..6.0f,
                            steps = 23,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "◄ Warmer (-6 dB)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Pivot: ${themeConfig.tiltEqualizerConfig.pivotHz.toInt()} Hz",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Brighter (+6 dB) ►",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pivot Frequency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                630.0f to "630 Hz (Vocal)",
                                1000.0f to "1.0 kHz (Standard)",
                                1600.0f to "1.6 kHz (Upper Mid)"
                            ).forEach { (freq, label) ->
                                FilterChip(
                                    selected = themeConfig.tiltEqualizerConfig.pivotHz == freq,
                                    onClick = { viewModel.setTiltEqPivotHz(freq) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        ListItem(
                            headlineContent = { Text("Loudness Energy Compensation") },
                            supportingContent = { Text("Automatically offsets volume so perceived SPL remains constant") },
                            trailingContent = {
                                Switch(
                                    checked = themeConfig.tiltEqualizerConfig.loudnessCompensate,
                                    onCheckedChange = { viewModel.setTiltEqLoudnessCompensate(it) }
                                )
                            }
                        )

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bass (100Hz): ${String.format(java.util.Locale.US, "%+.1f dB", tiltMetrics.bassGainDb)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Treble (10kHz): ${String.format(java.util.Locale.US, "%+.1f dB", tiltMetrics.trebleGainDb)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Loudness: ${String.format(java.util.Locale.US, "%+.1f dB", tiltMetrics.apparentLoudnessOffsetDb)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ListItem(
                        headlineContent = { Text("Dynamic Mid-Bass Mud Tamer") },
                        supportingContent = { Text("Detects and attenuates spectral congestion in the 200-450 Hz boxy mud zone") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.midBassClarityConfig.isEnabled,
                                onCheckedChange = { viewModel.setMidBassClarityEnabled(it) }
                            )
                        }
                    )

                    if (themeConfig.midBassClarityConfig.isEnabled) {
                        val clarityMetrics = com.nexus.tune.core.domain.audio.MidBassClarityEngine.calculateMetrics(
                            midBassEnergy = 35.0f,
                            totalEnergy = 100.0f,
                            config = themeConfig.midBassClarityConfig
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "De-Mudding Profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MidBassClarityMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeConfig.midBassClarityConfig.mode == mode,
                                    onClick = { viewModel.setMidBassClarityMode(mode) },
                                    label = { Text(mode.displayName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Center Frequency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                250.0f to "250 Hz (Low Mid)",
                                300.0f to "300 Hz (Standard Boxiness)",
                                360.0f to "360 Hz (Upper Mud)"
                            ).forEach { (freq, label) ->
                                FilterChip(
                                    selected = themeConfig.midBassClarityConfig.centerFrequencyHz == freq,
                                    onClick = { viewModel.setMidBassClarityCenterHz(freq) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dynamic Trigger Threshold: ${themeConfig.midBassClarityConfig.dynamicThresholdPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Max Dip: ${themeConfig.midBassClarityConfig.mode.maxDipDb} dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Slider(
                            value = themeConfig.midBassClarityConfig.dynamicThresholdPercent.toFloat(),
                            onValueChange = { viewModel.setMidBassClarityThreshold(it.toInt()) },
                            valueRange = 20.0f..70.0f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = themeConfig.midBassClarityConfig.mode.description,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rating: ${clarityMetrics.clarityRating}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Q: 1.4 RBJ Dip",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ListItem(
                        headlineContent = { Text("Loudspeaker Holographic 3D (XTC)") },
                        supportingContent = { Text("Acoustic crosstalk cancellation expanding stereo phone & speaker soundstage to 160°") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.crosstalkCancellationConfig.isEnabled,
                                onCheckedChange = { viewModel.setCrosstalkCancellationEnabled(it) }
                            )
                        }
                    )

                    if (themeConfig.crosstalkCancellationConfig.isEnabled) {
                        val xtcMetrics = com.nexus.tune.core.domain.audio.CrosstalkCancellationEngine.calculateMetrics(themeConfig.crosstalkCancellationConfig)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Speaker Acoustic Geometry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CrosstalkCancellationMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeConfig.crosstalkCancellationConfig.mode == mode,
                                    onClick = { viewModel.setCrosstalkCancellationMode(mode) },
                                    label = { Text(mode.displayName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Interaural Delay: ${themeConfig.crosstalkCancellationConfig.delaySamples} smp (${String.format(java.util.Locale.US, "%.0f µs", xtcMetrics.delayMicroseconds)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Span: ${xtcMetrics.virtualStageSpanDegrees}° Arc",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = themeConfig.crosstalkCancellationConfig.cancellationIntensityPercent.toFloat(),
                            onValueChange = { viewModel.setCrosstalkCancellationIntensity(it.toInt()) },
                            valueRange = 20.0f..100.0f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = themeConfig.crosstalkCancellationConfig.mode.description,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = xtcMetrics.binauralRating,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "XTC Depth: ${String.format(java.util.Locale.US, "%.1f dB", xtcMetrics.effectiveCancellationDb)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // --- Dynamic Fletcher-Munson Equal-Loudness Studio ---
                Text(
                    text = "Dynamic Equal-Loudness (Fletcher-Munson)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Compensates for human ear sensitivity loss at lower listening volumes by dynamically restoring bass punch and treble brilliance (ISO 226 calibrated).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                ListItem(
                    headlineContent = { Text("Dynamic Equal-Loudness") },
                    supportingContent = { Text("Volume-adaptive frequency contouring") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.dynamicLoudnessConfig.isEnabled,
                            onCheckedChange = { viewModel.setDynamicLoudnessEnabled(it) }
                        )
                    }
                )

                if (themeConfig.dynamicLoudnessConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contour Curve Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DynamicLoudnessMode.entries.filter { it != DynamicLoudnessMode.OFF }.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.dynamicLoudnessConfig.mode == mode,
                                onClick = { viewModel.setDynamicLoudnessMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compensation Intensity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${themeConfig.dynamicLoudnessConfig.intensityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.dynamicLoudnessConfig.intensityPercent.toFloat(),
                        onValueChange = { viewModel.setDynamicLoudnessIntensity(it.toInt()) },
                        valueRange = 0f..100f,
                        steps = 19
                    )

                    val dynamicLoudnessCurve = com.nexus.tune.core.domain.audio.DynamicLoudnessEngine.calculateLoudnessCurve(
                        mode = themeConfig.dynamicLoudnessConfig.mode,
                        currentVolume = 0.3f,
                        intensityPercent = themeConfig.dynamicLoudnessConfig.intensityPercent
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.dynamicLoudnessConfig.mode.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Low-Vol Bass: +${String.format(java.util.Locale.US, "%.1f", dynamicLoudnessCurve.effectiveBassGainDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Treble Sparkle: +${String.format(java.util.Locale.US, "%.1f", dynamicLoudnessCurve.effectiveTrebleGainDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
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
            }

            item {
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Audiophile Dither & Noise Shaping",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Eliminates harmonic quantization distortion when rendering 24/32-bit audio to 16-bit output. Psychoacoustic shaping yields >116 dB apparent dynamic range.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DitherMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeConfig.ditherMode == mode,
                            onClick = { viewModel.setDitherMode(mode) },
                            label = { Text(mode.displayName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val ditherMetrics = com.nexus.tune.core.domain.audio.DitherShapingEngine.calculateNoiseMetrics(16, themeConfig.ditherMode)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = themeConfig.ditherMode.description,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Apparent DR: ${String.format(java.util.Locale.US, "%.1f", ditherMetrics.effectiveSnrDb)} dB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (ditherMetrics.noiseShapingGainDb > 0f) {
                                Text(
                                    text = "Shaping Bonus: +${String.format(java.util.Locale.US, "%.1f", ditherMetrics.noiseShapingGainDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "DAC Reconstruction Filter Topology",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Configures anti-aliasing sinc reconstruction filter phase and ringing characteristics. Minimum-phase removes pre-ringing for crisp percussive punch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResamplingFilterMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeConfig.resamplingFilterConfig.mode == mode,
                            onClick = { viewModel.setResamplingFilterMode(mode) },
                            label = { Text(mode.displayName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val impulseMetrics = com.nexus.tune.core.domain.audio.ResamplingFilterEngine.calculateImpulseMetrics(themeConfig.resamplingFilterConfig.mode)
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = themeConfig.resamplingFilterConfig.mode.description,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pre-Ring: ${impulseMetrics.preRingingPercent}% • Post: ${impulseMetrics.postRingingPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Stopband: ${impulseMetrics.stopbandRejectionDb.toInt()} dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Audiophile Transient De-Clicker") },
                    supportingContent = { Text("Suppresses micro-ticks, buffer dropouts, and vinyl scratch pops using cubic Hermite splines") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.deClickerConfig.isEnabled,
                            onCheckedChange = { viewModel.setDeClickerEnabled(it) }
                        )
                    }
                )

                if (themeConfig.deClickerConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restoration Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeClickerMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.deClickerConfig.mode == mode,
                                onClick = { viewModel.setDeClickerMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detection Sensitivity: ${String.format(java.util.Locale.US, "%.1fx RMS", themeConfig.deClickerConfig.sensitivityMultiplier)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Max Width: ${themeConfig.deClickerConfig.maxClickWidthSamples} smp",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Slider(
                        value = themeConfig.deClickerConfig.sensitivityMultiplier,
                        onValueChange = { viewModel.setDeClickerSensitivity(it) },
                        valueRange = 1.5f..8.0f,
                        steps = 12,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.deClickerConfig.mode.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: Active Guard",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Spline: C¹ Cubic Hermite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Infrasonic Sub-Bass & Warp Guard") },
                    supportingContent = { Text("Attenuates inaudible rumble below 20-30 Hz to protect drivers and reclaim dynamic amplifier headroom") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.infrasonicFilterConfig.isEnabled,
                            onCheckedChange = { viewModel.setInfrasonicFilterEnabled(it) }
                        )
                    }
                )

                if (themeConfig.infrasonicFilterConfig.isEnabled) {
                    val infrasonicMetrics = remember(themeConfig.infrasonicFilterConfig) {
                        InfrasonicFilterEngine.calculateMetrics(themeConfig.infrasonicFilterConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "High-Pass Cutoff Frequency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15.0f, 18.0f, 20.0f, 25.0f, 30.0f).forEach { hz ->
                            FilterChip(
                                selected = kotlin.math.abs(themeConfig.infrasonicFilterConfig.cutoffHz - hz) < 0.1f,
                                onClick = { viewModel.setInfrasonicFilterCutoff(hz) },
                                label = { Text("${hz.toInt()} Hz") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Filter Attenuation Slope",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfrasonicSlope.entries.forEach { slope ->
                            FilterChip(
                                selected = themeConfig.infrasonicFilterConfig.slope == slope,
                                onClick = { viewModel.setInfrasonicFilterSlope(slope) },
                                label = { Text(slope.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    ListItem(
                        headlineContent = { Text("Recursive DC Offset Blocker") },
                        supportingContent = { Text("Rejects 0 Hz DC bias drift with recursive single-pole high-pass (R = 0.9995)") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.infrasonicFilterConfig.dcBlockerEnabled,
                                onCheckedChange = { viewModel.setInfrasonicDcBlockerEnabled(it) }
                            )
                        }
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.infrasonicFilterConfig.slope.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rumble @ 10Hz: ${String.format(java.util.Locale.US, "%.1f", infrasonicMetrics.rumbleAttenuationAt10HzDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Headroom: +${String.format(java.util.Locale.US, "%.2f", infrasonicMetrics.preservedHeadroomDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = infrasonicMetrics.protectionRating,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("High-Frequency 'Air Band' & Silk Sheen Exciter") },
                    supportingContent = { Text("Pultec / Maag-style high shelf with psychoacoustic even-order harmonic sheen for lossy streams") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.airBandExciterConfig.isEnabled,
                            onCheckedChange = { viewModel.setAirBandEnabled(it) }
                        )
                    }
                )

                if (themeConfig.airBandExciterConfig.isEnabled) {
                    val airBandMetrics = remember(themeConfig.airBandExciterConfig) {
                        AirBandExciterEngine.calculateMetrics(themeConfig.airBandExciterConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Exciter Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AirBandMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.airBandExciterConfig.mode == mode,
                                onClick = { viewModel.setAirBandMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Air Shelf Center Frequency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AirBandFrequency.entries.forEach { freq ->
                            FilterChip(
                                selected = themeConfig.airBandExciterConfig.frequency == freq,
                                onClick = { viewModel.setAirBandFrequency(freq) },
                                label = { Text(freq.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Air Shelf Boost: +${String.format(java.util.Locale.US, "%.1f", themeConfig.airBandExciterConfig.boostDb)} dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Harmonics: ${themeConfig.airBandExciterConfig.harmonicExcitationPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Slider(
                        value = themeConfig.airBandExciterConfig.boostDb,
                        onValueChange = { viewModel.setAirBandBoostDb(it) },
                        valueRange = 0.0f..8.0f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Harmonic Excitation Intensity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = themeConfig.airBandExciterConfig.harmonicExcitationPercent.toFloat(),
                        onValueChange = { viewModel.setAirBandHarmonics(it.toInt()) },
                        valueRange = 0.0f..100.0f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.airBandExciterConfig.frequency.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sparkle: ${airBandMetrics.airEnergyIndexPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "THD: +${String.format(java.util.Locale.US, "%.2f", airBandMetrics.harmonicDistortionPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = airBandMetrics.sheenDescriptor,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ListItem(
                    headlineContent = { Text("Psychoacoustic Sub-Harmonic Bass Studio") },
                    supportingContent = { Text("Synthesizes missing fundamental harmonics (MaxxBass) for compact drivers or sub-octave rumble (30-60 Hz) for subwoofers") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.subHarmonicSynthConfig.isEnabled,
                            onCheckedChange = { viewModel.setSubHarmonicSynthEnabled(it) }
                        )
                    }
                )

                if (themeConfig.subHarmonicSynthConfig.isEnabled) {
                    val subBassMetrics = remember(themeConfig.subHarmonicSynthConfig) {
                        SubHarmonicSynthEngine.calculateMetrics(themeConfig.subHarmonicSynthConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sub-Bass Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubHarmonicMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.subHarmonicSynthConfig.mode == mode,
                                onClick = { viewModel.setSubHarmonicSynthMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transducer Acoustic Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubHarmonicTarget.entries.forEach { target ->
                            FilterChip(
                                selected = themeConfig.subHarmonicSynthConfig.target == target,
                                onClick = { viewModel.setSubHarmonicSynthTarget(target) },
                                label = { Text(target.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Synthesis Intensity: ${themeConfig.subHarmonicSynthConfig.intensityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Perceived Gain: +${String.format(java.util.Locale.US, "%.1f", subBassMetrics.perceivedBassGainDb)} dB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = themeConfig.subHarmonicSynthConfig.intensityPercent.toFloat(),
                        onValueChange = { viewModel.setSubHarmonicSynthIntensity(it.toInt()) },
                        valueRange = 0.0f..100.0f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Sub-Octave Frequency Divider Mix: ${themeConfig.subHarmonicSynthConfig.subOctaveBlendPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = themeConfig.subHarmonicSynthConfig.subOctaveBlendPercent.toFloat(),
                        onValueChange = { viewModel.setSubHarmonicSynthBlend(it.toInt()) },
                        valueRange = 0.0f..100.0f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = subBassMetrics.syntheticBandwidthHz,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subBassMetrics.phantomFundamentalRating,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (subBassMetrics.excursionSafeguardActive) {
                                    Text(
                                        text = "Driver Excursion Safeguard",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Dynamic Spectral De-Esser Studio") },
                    supportingContent = { Text("Tames piercing vocal sibilance ('s', 'sh', 't') and bright headphone resonances without muffling presence") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.deEsserConfig.isEnabled,
                            onCheckedChange = { viewModel.setDeEsserEnabled(it) }
                        )
                    }
                )

                if (themeConfig.deEsserConfig.isEnabled) {
                    val deEsserMetrics = remember(themeConfig.deEsserConfig) {
                        DeEsserEngine.calculateMetrics(themeConfig.deEsserConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "De-Esser Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeEsserMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.deEsserConfig.mode == mode,
                                onClick = { viewModel.setDeEsserMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sibilance Center Frequency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5000.0f, 5500.0f, 6000.0f, 6500.0f, 7000.0f, 8000.0f).forEach { freq ->
                            FilterChip(
                                selected = kotlin.math.abs(themeConfig.deEsserConfig.centerFrequencyHz - freq) < 1.0f,
                                onClick = { viewModel.setDeEsserCenterFrequency(freq) },
                                label = { Text("${(freq / 1000.0f).toInt()}k Hz") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detection Threshold: ${themeConfig.deEsserConfig.thresholdPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Max Ducking: -${String.format(java.util.Locale.US, "%.1f", themeConfig.deEsserConfig.maxReductionDb)} dB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = themeConfig.deEsserConfig.thresholdPercent.toFloat(),
                        onValueChange = { viewModel.setDeEsserThreshold(it.toInt()) },
                        valueRange = 10.0f..60.0f,
                        steps = 10,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Maximum Sibilant Attenuation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = themeConfig.deEsserConfig.maxReductionDb,
                        onValueChange = { viewModel.setDeEsserMaxReduction(it) },
                        valueRange = 1.0f..10.0f,
                        steps = 18,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = deEsserMetrics.vocalSmoothnessRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Dynamic Reduction: ${String.format(java.util.Locale.US, "%.1f", deEsserMetrics.dynamicReductionDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Fatigue Shield: ${deEsserMetrics.fatigueReductionPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Activity: ${deEsserMetrics.sibilanceActivityPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("True Peak Inter-Sample Clipping Guard") },
                    supportingContent = { Text("4x oversampled sinc reconstruction limiter protecting DACs and lossy Bluetooth encoders from inter-sample overshoots") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.truePeakGuardConfig.isEnabled,
                            onCheckedChange = { viewModel.setTruePeakGuardEnabled(it) }
                        )
                    }
                )

                if (themeConfig.truePeakGuardConfig.isEnabled) {
                    val truePeakMetrics = remember(themeConfig.truePeakGuardConfig) {
                        TruePeakGuardEngine.calculateMetrics(themeConfig.truePeakGuardConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "True Peak Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TruePeakMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.truePeakGuardConfig.mode == mode,
                                onClick = { viewModel.setTruePeakGuardMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "True Peak Ceiling",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TruePeakCeiling.entries.forEach { ceiling ->
                            FilterChip(
                                selected = themeConfig.truePeakGuardConfig.ceiling == ceiling,
                                onClick = { viewModel.setTruePeakGuardCeiling(ceiling) },
                                label = { Text(ceiling.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    ListItem(
                        headlineContent = { Text("4x Polyphase Oversampling Detection") },
                        supportingContent = { Text("ITU-R BS.1770 compliant sinc interpolation to reveal hidden inter-sample excursions") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.truePeakGuardConfig.oversample4xEnabled,
                                onCheckedChange = { viewModel.setTruePeakOversample4x(it) }
                            )
                        }
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.truePeakGuardConfig.ceiling.description,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "True Peak: ${String.format(java.util.Locale.US, "%.2f", truePeakMetrics.maxEstimatedTruePeakDbTp)} dBTP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (truePeakMetrics.maxEstimatedTruePeakDbTp > 0.0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "ISP Delta: +${String.format(java.util.Locale.US, "%.2f", truePeakMetrics.interSampleOvershootDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Guard Gain: ${String.format(java.util.Locale.US, "%.2f", truePeakMetrics.appliedGuardAttenuationDb)} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Ultrasonic Anti-Aliasing & Nyquist Image Guard") },
                    supportingContent = { Text("Supersonic low-pass reconstruction filter eliminating DAC imaging hash, analog IMD distortion, and amplifier thermal stress") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.ultrasonicFilterConfig.isEnabled,
                            onCheckedChange = { viewModel.setUltrasonicFilterEnabled(it) }
                        )
                    }
                )

                if (themeConfig.ultrasonicFilterConfig.isEnabled) {
                    val ultrasonicMetrics = remember(themeConfig.ultrasonicFilterConfig) {
                        UltrasonicFilterEngine.calculateMetrics(themeConfig.ultrasonicFilterConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ultrasonic Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UltrasonicMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.ultrasonicFilterConfig.mode == mode,
                                onClick = { viewModel.setUltrasonicFilterMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Filter Topology",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UltrasonicTopology.entries.forEach { topology ->
                            FilterChip(
                                selected = themeConfig.ultrasonicFilterConfig.topology == topology,
                                onClick = { viewModel.setUltrasonicFilterTopology(topology) },
                                label = { Text(topology.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Supersonic Cutoff Frequency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20000.0f, 22050.0f, 24000.0f, 28000.0f, 32000.0f).forEach { cutoff ->
                            FilterChip(
                                selected = kotlin.math.abs(themeConfig.ultrasonicFilterConfig.cutoffHz - cutoff) < 1.0f,
                                onClick = { viewModel.setUltrasonicFilterCutoff(cutoff) },
                                label = { Text("${(cutoff / 1000.0f).toInt()}k Hz") }
                            )
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.ultrasonicFilterConfig.topology.description,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rejection: -${ultrasonicMetrics.stopbandRejectionDb.toInt()} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Delay: ${String.format(java.util.Locale.US, "%.1f", ultrasonicMetrics.groupDelayMicros)} µs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = ultrasonicMetrics.imdShieldRating,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ListItem(
                    headlineContent = { Text("Analog Vacuum Tube Saturation & Warmth Studio") },
                    supportingContent = { Text("Physical triode/pentode tube modeling injecting euphonic 2nd/3rd harmonics, dynamic analog warmth, and tape glue") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.tubeSaturationConfig.isEnabled,
                            onCheckedChange = { viewModel.setTubeSaturationEnabled(it) }
                        )
                    }
                )

                if (themeConfig.tubeSaturationConfig.isEnabled) {
                    val tubeMetrics = remember(themeConfig.tubeSaturationConfig) {
                        TubeSaturationEngine.calculateMetrics(themeConfig.tubeSaturationConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Warmth Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TubeWarmthProfile.entries.forEach { profile ->
                            FilterChip(
                                selected = themeConfig.tubeSaturationConfig.profile == profile,
                                onClick = { viewModel.setTubeSaturationProfile(profile) },
                                label = { Text(profile.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tube & Saturation Topology",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TubeTopology.entries.forEach { topology ->
                            FilterChip(
                                selected = themeConfig.tubeSaturationConfig.topology == topology,
                                onClick = { viewModel.setTubeSaturationTopology(topology) },
                                label = { Text(topology.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tube Drive / Saturation: ${(themeConfig.tubeSaturationConfig.drive * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Auto Makeup: ${if (themeConfig.tubeSaturationConfig.autoMakeupGain) "Active (${tubeMetrics.headroomCompensationDb} dB)" else "Off"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.tubeSaturationConfig.drive,
                        onValueChange = { viewModel.setTubeSaturationDrive(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cathode Bias Offset (Even vs Odd Balance): ${String.format(java.util.Locale.US, "%+.2f", themeConfig.tubeSaturationConfig.bias)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = themeConfig.tubeSaturationConfig.bias,
                        onValueChange = { viewModel.setTubeSaturationBias(it) },
                        valueRange = -0.5f..0.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto Headroom Makeup Gain",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.tubeSaturationConfig.autoMakeupGain,
                            onCheckedChange = { viewModel.setTubeSaturationAutoMakeupGain(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = tubeMetrics.characterRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2nd (2f): ${tubeMetrics.secondHarmonicDb} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "3rd (3f): ${tubeMetrics.thirdHarmonicDb} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "THD: ${tubeMetrics.totalHarmonicDistortionPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = "Warmth: ${tubeMetrics.warmthScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Multi-Band Transient Shaper & Dynamic Punch Studio") },
                    supportingContent = { Text("Dual ballistic envelope followers isolating attack impulses from sustain body for surgical drum punch, pick snap, and fatigue softening") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.transientShaperConfig.isEnabled,
                            onCheckedChange = { viewModel.setTransientShaperEnabled(it) }
                        )
                    }
                )

                if (themeConfig.transientShaperConfig.isEnabled) {
                    val transientMetrics = remember(themeConfig.transientShaperConfig) {
                        TransientShaperEngine.calculateMetrics(themeConfig.transientShaperConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dynamic Punch Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransientPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.transientShaperConfig.preset == preset,
                                onClick = { viewModel.setTransientShaperPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Frequency Band Focus",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransientBandFocus.entries.forEach { focus ->
                            FilterChip(
                                selected = themeConfig.transientShaperConfig.bandFocus == focus,
                                onClick = { viewModel.setTransientShaperFocus(focus) },
                                label = { Text(focus.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attack Punch (Initial Impulse): ${String.format(java.util.Locale.US, "%+.0f%%", themeConfig.transientShaperConfig.attackBoost * 100)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Delta: ${String.format(java.util.Locale.US, "%+.1f dB", transientMetrics.attackDeltaDb)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.transientShaperConfig.attackBoost,
                        onValueChange = { viewModel.setTransientShaperAttack(it) },
                        valueRange = -1.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sustain Body (Tail Decay): ${String.format(java.util.Locale.US, "%+.0f%%", themeConfig.transientShaperConfig.sustainBoost * 100)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Delta: ${String.format(java.util.Locale.US, "%+.1f dB", transientMetrics.sustainDeltaDb)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.transientShaperConfig.sustainBoost,
                        onValueChange = { viewModel.setTransientShaperSustain(it) },
                        valueRange = -1.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto Peak Limiting Guard",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.transientShaperConfig.autoOutputLimit,
                            onCheckedChange = { viewModel.setTransientShaperAutoLimit(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = transientMetrics.punchRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Crest Factor: ${transientMetrics.crestFactorDb} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Punch Score: ${transientMetrics.dynamicPunchScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = themeConfig.transientShaperConfig.bandFocus.displayName.substringBefore(" ("),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                ListItem(
                    headlineContent = { Text("Multi-Band Dynamic Stereo Imager & Ambience Studio") },
                    supportingContent = { Text("Orthogonal Mid/Side spatial processing with mono-bass foldback, high-frequency air decorrelation, and Pearson phase guard") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.dynamicStereoImagerConfig.isEnabled,
                            onCheckedChange = { viewModel.setDynamicStereoImagerEnabled(it) }
                        )
                    }
                )

                if (themeConfig.dynamicStereoImagerConfig.isEnabled) {
                    val imagerMetrics = remember(themeConfig.dynamicStereoImagerConfig) {
                        DynamicStereoImagerEngine.calculateMetrics(themeConfig.dynamicStereoImagerConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spatial Imager Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StereoImagerPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.dynamicStereoImagerConfig.preset == preset,
                                onClick = { viewModel.setDynamicStereoImagerPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stereo Soundstage Width: ${(themeConfig.dynamicStereoImagerConfig.stereoWidth * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Phase Corr: ${imagerMetrics.correlationCoefficient}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.dynamicStereoImagerConfig.stereoWidth,
                        onValueChange = { viewModel.setDynamicStereoImagerWidth(it) },
                        valueRange = 0.0f..2.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mono-Bass Cutoff Frequency (Sub Foldback): ${themeConfig.dynamicStereoImagerConfig.monoBassCutoffHz.toInt()} Hz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(80.0f, 120.0f, 160.0f, 200.0f, 250.0f).forEach { cutoff ->
                            FilterChip(
                                selected = kotlin.math.abs(themeConfig.dynamicStereoImagerConfig.monoBassCutoffHz - cutoff) < 1.0f,
                                onClick = { viewModel.setDynamicStereoImagerBassCutoff(cutoff) },
                                label = { Text("${cutoff.toInt()} Hz") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "High-Band Diffuse Air Spread: ${(themeConfig.dynamicStereoImagerConfig.highBandAirSpread * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = themeConfig.dynamicStereoImagerConfig.highBandAirSpread,
                        onValueChange = { viewModel.setDynamicStereoImagerAirSpread(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mono Compatibility Phase Guard",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.dynamicStereoImagerConfig.monoGuardActive,
                            onCheckedChange = { viewModel.setDynamicStereoImagerMonoGuard(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = imagerMetrics.spatialDepthRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Width: ${imagerMetrics.perceivedWidthPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mono Bass: ${imagerMetrics.monoBassCutoffHz.toInt()} Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Mono Safety: ${imagerMetrics.monoCompatibilityScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                ListItem(
                    headlineContent = { Text("Analog Tape Flutter, Wow & Hysteresis Warmer Studio") },
                    supportingContent = { Text("Reel-to-reel magnetic tape modeling with Langevin hysteresis saturation, physical head bump bass resonance, and capstan wow/flutter") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.tapeHysteresisConfig.isEnabled,
                            onCheckedChange = { viewModel.setTapeHysteresisEnabled(it) }
                        )
                    }
                )

                if (themeConfig.tapeHysteresisConfig.isEnabled) {
                    val tapeMetrics = remember(themeConfig.tapeHysteresisConfig) {
                        TapeHysteresisEngine.calculateMetrics(themeConfig.tapeHysteresisConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tape Machine Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TapePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.tapeHysteresisConfig.preset == preset,
                                onClick = { viewModel.setTapeHysteresisPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tape Speed (Formulation & Mechanics)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TapeSpeed.entries.forEach { speed ->
                            FilterChip(
                                selected = themeConfig.tapeHysteresisConfig.speed == speed,
                                onClick = { viewModel.setTapeHysteresisSpeed(speed) },
                                label = { Text(speed.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tape Drive / Magnetic Saturation: ${(themeConfig.tapeHysteresisConfig.tapeDrive * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Comp: ${tapeMetrics.saturationCompressionDb} dB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.tapeHysteresisConfig.tapeDrive,
                        onValueChange = { viewModel.setTapeHysteresisDrive(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Head Bump Resonance (+Bass Foundation): ${String.format(java.util.Locale.US, "+%.1f dB", themeConfig.tapeHysteresisConfig.headBumpGainDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@ ${tapeMetrics.headBumpFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.tapeHysteresisConfig.headBumpGainDb,
                        onValueChange = { viewModel.setTapeHysteresisHeadBump(it) },
                        valueRange = 0.0f..4.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wow & Flutter Modulation (Pitch Drift): ${(themeConfig.tapeHysteresisConfig.wowFlutterDepth * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${tapeMetrics.wowFlutterPpm} PPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Slider(
                        value = themeConfig.tapeHysteresisConfig.wowFlutterDepth,
                        onValueChange = { viewModel.setTapeHysteresisWowFlutter(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Residual Tape Hiss Suppression",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.tapeHysteresisConfig.tapeHissReduction,
                            onCheckedChange = { viewModel.setTapeHysteresisNoiseReduction(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = tapeMetrics.tapeCharacterRating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Warmth: ${tapeMetrics.analogWarmthScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Speed: ${themeConfig.tapeHysteresisConfig.speed.displayName.substringBefore(" (")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Resonance: ${tapeMetrics.headBumpFrequencyHz.toInt()} Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Audiophile Mains Ground Loop & Hum Isolator Studio ---
                ListItem(
                    headlineContent = { Text("Mains Ground Loop & 50/60Hz Hum Isolator Studio") },
                    supportingContent = { Text("Surgical multi-order notch filter cascade targeting 50/60 Hz powerline hum, charging buzz, and ground loops with adaptive masking") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.mainsHumIsolatorConfig.isEnabled,
                            onCheckedChange = { viewModel.setMainsHumIsolatorEnabled(it) }
                        )
                    }
                )

                if (themeConfig.mainsHumIsolatorConfig.isEnabled) {
                    val humMetrics = remember(themeConfig.mainsHumIsolatorConfig) {
                        MainsHumIsolatorEngine.calculateMetrics(themeConfig.mainsHumIsolatorConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hum Isolator Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HumIsolatorPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.mainsHumIsolatorConfig.preset == preset,
                                onClick = { viewModel.setMainsHumIsolatorPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AC Grid Base Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MainsFrequency.entries.forEach { freq ->
                            FilterChip(
                                selected = themeConfig.mainsHumIsolatorConfig.frequency == freq,
                                onClick = { viewModel.setMainsHumIsolatorFrequency(freq) },
                                label = { Text(freq.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Harmonic Notch Order",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HumHarmonicDepth.entries.forEach { depth ->
                            FilterChip(
                                selected = themeConfig.mainsHumIsolatorConfig.harmonicDepth == depth,
                                onClick = { viewModel.setMainsHumIsolatorHarmonicDepth(depth) },
                                label = { Text(depth.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notch Q Factor (Filter Sharpness): ${String.format(java.util.Locale.US, "Q = %.1f", themeConfig.mainsHumIsolatorConfig.notchQ)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Pass-Band: ${String.format(java.util.Locale.US, "%.1f%%", humMetrics.subBassPreservationRatio * 100)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.mainsHumIsolatorConfig.notchQ,
                        onValueChange = { viewModel.setMainsHumIsolatorNotchQ(it) },
                        valueRange = 10.0f..45.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notch Attenuation Depth: ${String.format(java.util.Locale.US, "%.0f dB", themeConfig.mainsHumIsolatorConfig.notchDepthDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Target f₀: ${humMetrics.detectedGridFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.mainsHumIsolatorConfig.notchDepthDb,
                        onValueChange = { viewModel.setMainsHumIsolatorNotchDepth(it) },
                        valueRange = -48.0f..-6.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Adaptive Psychoacoustic Masking",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.mainsHumIsolatorConfig.adaptiveMasking,
                            onCheckedChange = { viewModel.setMainsHumIsolatorAdaptiveMasking(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recursive DC Offset Bias Blocker",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.mainsHumIsolatorConfig.dcBlockerEnabled,
                            onCheckedChange = { viewModel.setMainsHumIsolatorDcBlocker(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "GROUND LOOP DIAGNOSTICS & NOISE HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Isolation: ${humMetrics.isolationRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Floor: ${String.format(java.util.Locale.US, "%.1f dB", humMetrics.estimatedHumLevelDb)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "DC: ${String.format(java.util.Locale.US, "%.1f mV", humMetrics.dcOffsetMv)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Acoustic Feedback & Larsen Ringing Suppressor Studio ---
                ListItem(
                    headlineContent = { Text("Dynamic Acoustic Feedback & Ringing Suppressor Studio") },
                    supportingContent = { Text("Real-time hunting notch filter bank detecting acoustic feedback, mic Larsen squeals, and phone speaker cavity resonances") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.acousticFeedbackConfig.isEnabled,
                            onCheckedChange = { viewModel.setAcousticFeedbackEnabled(it) }
                        )
                    }
                )

                if (themeConfig.acousticFeedbackConfig.isEnabled) {
                    val feedbackMetrics = remember(themeConfig.acousticFeedbackConfig) {
                        AcousticFeedbackEngine.calculateMetrics(themeConfig.acousticFeedbackConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feedback Suppressor Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AcousticFeedbackPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.acousticFeedbackConfig.preset == preset,
                                onClick = { viewModel.setAcousticFeedbackPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Feedback Hunt Spectral Target",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeedbackHuntMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.acousticFeedbackConfig.mode == mode,
                                onClick = { viewModel.setAcousticFeedbackMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detection Sensitivity: ${String.format(java.util.Locale.US, "%.0f dBFS", themeConfig.acousticFeedbackConfig.sensitivityThresholdDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Target f₀: ${feedbackMetrics.detectedRingingFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.acousticFeedbackConfig.sensitivityThresholdDb,
                        onValueChange = { viewModel.setAcousticFeedbackSensitivity(it) },
                        valueRange = -45.0f..-12.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Tracking Notches: ${themeConfig.acousticFeedbackConfig.activeNotchesCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sharpness: Q = ${themeConfig.acousticFeedbackConfig.notchSharpnessQ.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.acousticFeedbackConfig.activeNotchesCount.toFloat(),
                        onValueChange = { viewModel.setAcousticFeedbackNotchesCount(it.toInt()) },
                        valueRange = 1.0f..4.0f,
                        steps = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suppression Attenuation Depth: ${String.format(java.util.Locale.US, "%.0f dB", themeConfig.acousticFeedbackConfig.suppressionDepthDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rejection: -${feedbackMetrics.totalSuppressionDb.toInt()} dB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Slider(
                        value = themeConfig.acousticFeedbackConfig.suppressionDepthDb,
                        onValueChange = { viewModel.setAcousticFeedbackSuppressionDepth(it) },
                        valueRange = -36.0f..-6.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Emergency Runaway Panic Clamp (Hearing Guard)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.acousticFeedbackConfig.fastPanicClamp,
                            onCheckedChange = { viewModel.setAcousticFeedbackPanicClamp(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ACOUSTIC FEEDBACK & LARSEN HUNTING HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${feedbackMetrics.safetyStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Notches: ${feedbackMetrics.activeNotchCount} Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Lock: ${feedbackMetrics.detectedRingingFrequencyHz.toInt()} Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Psychoacoustic Soundstage Elevation & Height Virtualizer Studio ---
                ListItem(
                    headlineContent = { Text("Psychoacoustic Soundstage Elevation & Height Studio") },
                    supportingContent = { Text("Median plane pinna HRTF modeling (7.8-9.2 kHz) and micro-reflection ceiling lines elevating perceived soundstage to forehead height") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.soundstageElevationConfig.isEnabled,
                            onCheckedChange = { viewModel.setSoundstageElevationEnabled(it) }
                        )
                    }
                )

                if (themeConfig.soundstageElevationConfig.isEnabled) {
                    val elevationMetrics = remember(themeConfig.soundstageElevationConfig) {
                        SoundstageElevationEngine.calculateMetrics(themeConfig.soundstageElevationConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Elevation Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SoundstageElevationPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.soundstageElevationConfig.preset == preset,
                                onClick = { viewModel.setSoundstageElevationPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vertical Elevation Angle",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ElevationAngle.entries.forEach { angle ->
                            FilterChip(
                                selected = themeConfig.soundstageElevationConfig.elevationAngle == angle,
                                onClick = { viewModel.setSoundstageElevationAngle(angle) },
                                label = { Text(angle.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Elevation Intensity: ${(themeConfig.soundstageElevationConfig.elevationIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Apparent Angle: ${String.format(java.util.Locale.US, "%.1f°", elevationMetrics.virtualElevationDegrees)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.soundstageElevationConfig.elevationIntensity,
                        onValueChange = { viewModel.setSoundstageElevationIntensity(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ceiling Reflection Delay: ${String.format(java.util.Locale.US, "%.1f ms", themeConfig.soundstageElevationConfig.ceilingReflectionMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${elevationMetrics.ceilingDelaySamples} Samples",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.soundstageElevationConfig.ceilingReflectionMs,
                        onValueChange = { viewModel.setSoundstageElevationCeilingMs(it) },
                        valueRange = 1.5f..8.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pinna Concha Notch Depth: ${String.format(java.util.Locale.US, "%.1f dB", themeConfig.soundstageElevationConfig.pinnaNotchDepthDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Notch: ${elevationMetrics.pinnaNotchFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Slider(
                        value = themeConfig.soundstageElevationConfig.pinnaNotchDepthDb,
                        onValueChange = { viewModel.setSoundstageElevationPinnaDepth(it) },
                        valueRange = -9.0f..-1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cross-Channel Diffuse Ceiling Spread",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.soundstageElevationConfig.diffuseCeilingSpread,
                            onCheckedChange = { viewModel.setSoundstageElevationDiffuseSpread(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "3D SOUNDSTAGE ELEVATION & VERTICAL HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Perspective: ${elevationMetrics.elevationRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Height Score: ${elevationMetrics.spatialHeightScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Angle: ${String.format(java.util.Locale.US, "%.1f°", elevationMetrics.virtualElevationDegrees)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Audiophile Dynamic Sub-Bass Phase Alignment & Mono Studio ---
                ListItem(
                    headlineContent = { Text("Dynamic Sub-Bass Phase Alignment & Mono Studio") },
                    supportingContent = { Text("Elliptical Linkwitz-Riley equalizer collapsing out-of-phase low-end into monolithic mono punch while keeping wide stereo highs") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.monoBassAlignConfig.isEnabled,
                            onCheckedChange = { viewModel.setMonoBassAlignEnabled(it) }
                        )
                    }
                )

                if (themeConfig.monoBassAlignConfig.isEnabled) {
                    val monoMetrics = remember(themeConfig.monoBassAlignConfig) {
                        MonoBassAlignEngine.calculateMetrics(themeConfig.monoBassAlignConfig)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sub-Bass Mono Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoBassAlignPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.monoBassAlignConfig.preset == preset,
                                onClick = { viewModel.setMonoBassAlignPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mono Crossover Cutoff Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoBassCrossover.entries.forEach { cutoff ->
                            FilterChip(
                                selected = themeConfig.monoBassAlignConfig.crossover == cutoff,
                                onClick = { viewModel.setMonoBassAlignCrossover(cutoff) },
                                label = { Text(cutoff.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Crossover Filter Slope",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoBassFilterSlope.entries.forEach { slope ->
                            FilterChip(
                                selected = themeConfig.monoBassAlignConfig.slope == slope,
                                onClick = { viewModel.setMonoBassAlignSlope(slope) },
                                label = { Text(slope.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restored Sub-Punch Makeup Gain: ${String.format(java.util.Locale.US, "+%.1f dB", themeConfig.monoBassAlignConfig.subPunchGainDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Restored: +${String.format(java.util.Locale.US, "%.1f dB", monoMetrics.restoredEnergyDb)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.monoBassAlignConfig.subPunchGainDb,
                        onValueChange = { viewModel.setMonoBassAlignPunchGain(it) },
                        valueRange = 0.0f..4.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inter-Channel Low-End Phase Alignment",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.monoBassAlignConfig.autoPhaseAlign,
                            onCheckedChange = { viewModel.setMonoBassAlignAutoPhase(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Audition Removed Side Sub-Bass (Solo Pruned Mud)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = themeConfig.monoBassAlignConfig.sideSoloAudition,
                            onCheckedChange = { viewModel.setMonoBassAlignSideSolo(it) }
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUB-BASS PHASE COHERENCE & ELLIPTICAL HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${monoMetrics.coherenceRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Cutoff: ${monoMetrics.crossoverFrequencyHz.toInt()} Hz",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "ρ: ${String.format(java.util.Locale.US, "%.2f", monoMetrics.subBassCorrelation)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Preamp Thermal Noise Profiler & Spectral Flattener Studio ---
                Text(
                    text = "Dynamic Preamp Noise Profiler & Spectral Flattener Studio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Preamp Noise Profiler & Spectral Flattener") },
                    supportingContent = { Text("Johnson-Nyquist thermal noise profiler, dynamic spectral tilt flattener, soft-knee downward expander, and warm Fletcher-Munson dither") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.preampNoiseFlattenerConfig.isEnabled,
                            onCheckedChange = { viewModel.setPreampNoiseFlattenerEnabled(it) }
                        )
                    }
                )

                if (themeConfig.preampNoiseFlattenerConfig.isEnabled) {
                    val preampMetrics = remember(themeConfig.preampNoiseFlattenerConfig) {
                        PreampNoiseFlattenerEngine.calculateMetrics(
                            themeConfig.preampNoiseFlattenerConfig,
                            floatArrayOf(0.02f, 0.02f, 0.01f, 0.01f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preamp Restoration Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreampNoiseFlattenerPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.preampNoiseFlattenerConfig.preset == preset,
                                onClick = { viewModel.setPreampNoiseFlattenerPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thermal Noise Profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreampNoiseProfile.entries.forEach { profile ->
                            FilterChip(
                                selected = themeConfig.preampNoiseFlattenerConfig.profile == profile,
                                onClick = { viewModel.setPreampNoiseProfile(profile) },
                                label = { Text(profile.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Restoration Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreampFlattenerMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.preampNoiseFlattenerConfig.mode == mode,
                                onClick = { viewModel.setPreampFlattenerMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spectral Flatten Depth: ${(themeConfig.preampNoiseFlattenerConfig.flattenDepth * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tilt: ${String.format(java.util.Locale.US, "%.1f dB/oct", preampMetrics.spectralTiltSlope)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.preampNoiseFlattenerConfig.flattenDepth,
                        onValueChange = { viewModel.setPreampFlattenDepth(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downward Expansion Threshold: ${themeConfig.preampNoiseFlattenerConfig.downwardThresholdDb.toInt()} dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Floor: ${preampMetrics.estimatedNoiseFloorDb.toInt()} dB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.preampNoiseFlattenerConfig.downwardThresholdDb,
                        onValueChange = { viewModel.setPreampDownwardThreshold(it) },
                        valueRange = -60.0f..-20.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HF Tilt Offset: ${String.format(java.util.Locale.US, "%+.1f dB", themeConfig.preampNoiseFlattenerConfig.hfTiltDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = themeConfig.preampNoiseFlattenerConfig.hfTiltDb,
                        onValueChange = { viewModel.setPreampHfTiltDb(it) },
                        valueRange = -6.0f..6.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fletcher-Munson Dither Level: ${(themeConfig.preampNoiseFlattenerConfig.thermalDitherLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (preampMetrics.ditherActive) "Dither Active" else "Dither Off",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (preampMetrics.ditherActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Slider(
                        value = themeConfig.preampNoiseFlattenerConfig.thermalDitherLevel,
                        onValueChange = { viewModel.setPreampThermalDitherLevel(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ANALOG PREAMP THERMAL NOISE & SPECTRAL HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${preampMetrics.noiseFloorRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Reduct: -${String.format(java.util.Locale.US, "%.1f dB", preampMetrics.dynamicGainReductionDb)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Transient Peak Slew-Rate Limiter & Electrostatic Protector Studio ---
                Text(
                    text = "Dynamic Slew-Rate Limiter & Electrostatic Protector Studio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Transient Slew-Rate Limiter & Diaphragm Guard") },
                    supportingContent = { Text("Precision dV/dt slew limiting, TIM distortion suppression, and electrostatic stator flashover prevention") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.slewRateLimiterConfig.isEnabled,
                            onCheckedChange = { viewModel.setSlewRateLimiterEnabled(it) }
                        )
                    }
                )

                if (themeConfig.slewRateLimiterConfig.isEnabled) {
                    val slewMetrics = remember(themeConfig.slewRateLimiterConfig) {
                        SlewRateLimiterEngine.calculateMetrics(
                            themeConfig.slewRateLimiterConfig,
                            floatArrayOf(0.0f, 0.0f, 0.4f, 0.4f, -0.2f, -0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Slew Limiter Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlewRateLimiterPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.slewRateLimiterConfig.preset == preset,
                                onClick = { viewModel.setSlewRateLimiterPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nominal Slew Velocity (dV/dt)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlewRateSpeed.entries.forEach { speed ->
                            FilterChip(
                                selected = themeConfig.slewRateLimiterConfig.speed == speed,
                                onClick = { viewModel.setSlewRateSpeed(speed) },
                                label = { Text(speed.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Limiter Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlewLimiterMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.slewRateLimiterConfig.mode == mode,
                                onClick = { viewModel.setSlewLimiterMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transient Damping Intensity: ${(themeConfig.slewRateLimiterConfig.dampingRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Peak: ${String.format(java.util.Locale.US, "%.1f V/µs", slewMetrics.peakSlewRateVUs)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.slewRateLimiterConfig.dampingRatio,
                        onValueChange = { viewModel.setSlewDampingRatio(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diaphragm Arc Protection Ceiling: ${(themeConfig.slewRateLimiterConfig.peakThreshold * 100).toInt()}% FS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "TIM: ${String.format(java.util.Locale.US, "%.1f", slewMetrics.timDistortionIndex)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.slewRateLimiterConfig.peakThreshold,
                        onValueChange = { viewModel.setSlewPeakThreshold(it) },
                        valueRange = 0.5f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TRANSDUCER SLEW RATE & DIAPHRAGM SAFETY HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${slewMetrics.membraneSafetyRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Clamped: ${slewMetrics.clampedTransientsCount} transients",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Acoustic Resonator & Cabinet Horn Diffuser Studio ---
                Text(
                    text = "Dynamic Acoustic Resonator & Cabinet Horn Diffuser Studio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Acoustic Horn Loading & QRD Diffuser") },
                    supportingContent = { Text("Tractrix flare impedance matching, throat cavity anti-resonance notch, and horizontal QRD dispersion") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.hornDiffuserConfig.isEnabled,
                            onCheckedChange = { viewModel.setHornDiffuserEnabled(it) }
                        )
                    }
                )

                if (themeConfig.hornDiffuserConfig.isEnabled) {
                    val hornMetrics = remember(themeConfig.hornDiffuserConfig) {
                        HornDiffuserEngine.calculateMetrics(
                            themeConfig.hornDiffuserConfig,
                            floatArrayOf(0.1f, 0.1f, 0.05f, 0.05f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Horn Diffuser Preset",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HornDiffuserPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.hornDiffuserConfig.preset == preset,
                                onClick = { viewModel.setHornDiffuserPreset(preset) },
                                label = { Text(preset.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Horn Flare Geometry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HornFlareGeometry.entries.forEach { geo ->
                            FilterChip(
                                selected = themeConfig.hornDiffuserConfig.geometry == geo,
                                onClick = { viewModel.setHornFlareGeometry(geo) },
                                label = { Text(geo.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Acoustic Diffusion Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HornDiffuserMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.hornDiffuserConfig.mode == mode,
                                onClick = { viewModel.setHornDiffuserMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Throat Cutoff Frequency: ${themeConfig.hornDiffuserConfig.throatCutoffHz.toInt()} Hz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "fc: ${hornMetrics.effectiveCutoffFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.hornDiffuserConfig.throatCutoffHz,
                        onValueChange = { viewModel.setHornThroatCutoffHz(it) },
                        valueRange = 200.0f..1000.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Flare Acoustic Loading Intensity: ${(themeConfig.hornDiffuserConfig.flareIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Z: ${String.format(java.util.Locale.US, "%.1fx", hornMetrics.throatAcousticImpedanceRatio)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.hornDiffuserConfig.flareIntensity,
                        onValueChange = { viewModel.setHornFlareIntensity(it) },
                        valueRange = 0.0f..1.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Throat Anti-Resonance Notch: ${String.format(java.util.Locale.US, "%.1f dB", themeConfig.hornDiffuserConfig.throatAntiResonanceDb)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = themeConfig.hornDiffuserConfig.throatAntiResonanceDb,
                        onValueChange = { viewModel.setHornThroatAntiResonanceDb(it) },
                        valueRange = -8.0f..0.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horizontal Coverage Angle: ${themeConfig.hornDiffuserConfig.horizontalCoverageAngle}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${hornMetrics.horizontalCoverageAngle}° QRD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Slider(
                        value = themeConfig.hornDiffuserConfig.horizontalCoverageAngle.toFloat(),
                        onValueChange = { viewModel.setHornHorizontalCoverageAngle(it.toInt()) },
                        valueRange = 45.0f..120.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ACOUSTIC HORN LOADING & DISPERSION HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${hornMetrics.hornLoadingRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Coverage: ${hornMetrics.horizontalCoverageAngle}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Sub-Bass Transient Punch & Attack Reshaper Studio ---
                Text(
                    text = "Sub-Bass Punch & Attack Reshaper",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Sub-Bass Transient Punch & Attack Reshaper") },
                    supportingContent = { Text("Multiband Linkwitz-Riley crossover, transient onset punch amplifier, and resonant sustain damping") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.bassAttackReshaperConfig.isEnabled,
                            onCheckedChange = { viewModel.setBassAttackReshaperEnabled(it) }
                        )
                    }
                )

                if (themeConfig.bassAttackReshaperConfig.isEnabled) {
                    val bassMetrics = remember(themeConfig.bassAttackReshaperConfig) {
                        BassAttackReshaperEngine().estimateMetrics(
                            floatArrayOf(0.7f, -0.5f, 0.3f, 0.05f),
                            themeConfig.bassAttackReshaperConfig
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sub-Bass Attack Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BassAttackPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.bassAttackReshaperConfig.preset == preset,
                                onClick = { viewModel.setBassAttackPreset(preset) },
                                label = { Text(preset.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target Sub-Bass Frequency",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BassAttackTarget.entries.forEach { target ->
                            FilterChip(
                                selected = themeConfig.bassAttackReshaperConfig.target == target,
                                onClick = { viewModel.setBassAttackTarget(target) },
                                label = { Text(target.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reshaper Processing Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BassReshaperMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.bassAttackReshaperConfig.mode == mode,
                                onClick = { viewModel.setBassReshaperMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attack Punch Velocity: ${String.format(java.util.Locale.US, "%.2fx", themeConfig.bassAttackReshaperConfig.attackPunchMultiplier)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ratio: ${bassMetrics.attackVelocityRatio}x",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.bassAttackReshaperConfig.attackPunchMultiplier,
                        onValueChange = { viewModel.setBassAttackPunchMultiplier(it) },
                        valueRange = 1.0f..2.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sustain Tail Resonance Damping: ${themeConfig.bassAttackReshaperConfig.sustainDampingPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-${bassMetrics.sustainDampingDb} dB",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.bassAttackReshaperConfig.sustainDampingPercent.toFloat(),
                        onValueChange = { viewModel.setBassSustainDampingPercent(it.toInt()) },
                        valueRange = 0.0f..100.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sub-Bass Crossover Frequency: ${themeConfig.bassAttackReshaperConfig.crossoverFrequencyHz.toInt()} Hz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = themeConfig.bassAttackReshaperConfig.crossoverFrequencyHz,
                        onValueChange = { viewModel.setBassCrossoverFrequencyHz(it) },
                        valueRange = 30.0f..250.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    ListItem(
                        headlineContent = { Text("Solo Transient Attack Audition") },
                        supportingContent = { Text("Isolate and listen only to the dynamic transient kick impact") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.bassAttackReshaperConfig.soloAttackTransient,
                                onCheckedChange = { viewModel.setBassSoloAttackTransient(it) }
                            )
                        }
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUB-BASS TRANSIENT ATTACK & SUSTAIN HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Impact: ${bassMetrics.transientPunchRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tail Damping: -${bassMetrics.sustainDampingDb} dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                // --- Dynamic Mid-Side Spatial Phase Decorrelator & Ambience Diffuser Studio ---
                Text(
                    text = "Spatial Phase Decorrelator & Ambience Diffuser",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                ListItem(
                    headlineContent = { Text("Mid-Side Spatial Phase Decorrelator") },
                    supportingContent = { Text("All-pass phase decorrelation on stereo side channel with mono-compatibility safeguarding") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.spatialDecorrelatorConfig.isEnabled,
                            onCheckedChange = { viewModel.setSpatialDecorrelatorEnabled(it) }
                        )
                    }
                )

                if (themeConfig.spatialDecorrelatorConfig.isEnabled) {
                    val decorrelatorMetrics = remember(themeConfig.spatialDecorrelatorConfig) {
                        SpatialDecorrelatorEngine().estimateMetrics(
                            floatArrayOf(0.6f, 0.45f, -0.3f, -0.2f, 0.5f, 0.4f),
                            themeConfig.spatialDecorrelatorConfig
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Decorrelator Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DecorrelatorPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = themeConfig.spatialDecorrelatorConfig.preset == preset,
                                onClick = { viewModel.setSpatialDecorrelatorPreset(preset) },
                                label = { Text(preset.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spatial Delay Stage",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DecorrelatorStage.entries.forEach { stage ->
                            FilterChip(
                                selected = themeConfig.spatialDecorrelatorConfig.stage == stage,
                                onClick = { viewModel.setSpatialDecorrelatorStage(stage) },
                                label = { Text(stage.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decorrelator Diffusion Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DecorrelatorMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.spatialDecorrelatorConfig.mode == mode,
                                onClick = { viewModel.setSpatialDecorrelatorMode(mode) },
                                label = { Text(mode.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diffusion Intensity: ${themeConfig.spatialDecorrelatorConfig.diffusionIntensityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Phase Angle: ${decorrelatorMetrics.phaseSpreadAngleDegrees}°",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.spatialDecorrelatorConfig.diffusionIntensityPercent.toFloat(),
                        onValueChange = { viewModel.setSpatialDecorrelatorIntensity(it.toInt()) },
                        valueRange = 0.0f..100.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stereo Soundstage Width: ${String.format(java.util.Locale.US, "%.2fx", themeConfig.spatialDecorrelatorConfig.stereoWidthMultiplier)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ρ: ${decorrelatorMetrics.correlationCoefficient}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = themeConfig.spatialDecorrelatorConfig.stereoWidthMultiplier,
                        onValueChange = { viewModel.setSpatialDecorrelatorWidth(it) },
                        valueRange = 0.5f..2.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    ListItem(
                        headlineContent = { Text("Mono Compatibility Guard") },
                        supportingContent = { Text("Prevent stereo phase cancellation when summed to mono") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.spatialDecorrelatorConfig.monoCompatibilityGuard,
                                onCheckedChange = { viewModel.setSpatialDecorrelatorMonoGuard(it) }
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Solo Decorrelated Side Audition") },
                        supportingContent = { Text("Audition only the diffuse side ambience component") },
                        trailingContent = {
                            Switch(
                                checked = themeConfig.spatialDecorrelatorConfig.soloDecorrelatedSide,
                                onCheckedChange = { viewModel.setSpatialDecorrelatorSoloSide(it) }
                            )
                        }
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SPATIAL PHASE CORRELATION & AMBIENCE HUD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Stage: ${decorrelatorMetrics.soundstageWidthRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Mono Guard: ${if (decorrelatorMetrics.isMonoSafe) "PROTECTED" else "WARNING"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (decorrelatorMetrics.isMonoSafe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
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

                // --- Beat-Synchronized Phrase Crossfade & DJ Cue Studio ---
                Spacer(modifier = Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text("Beat-Sync Phrase Crossfade (DJ)") },
                    supportingContent = { Text("Align crossfade with musical 4/8/16-bar phrases and smart tempo blending") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.beatCrossfadeConfig.isEnabled,
                            onCheckedChange = { viewModel.setBeatCrossfadeEnabled(it) }
                        )
                    }
                )

                if (themeConfig.beatCrossfadeConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Phrase Transition Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BeatCrossfadeMode.entries.filter { it != BeatCrossfadeMode.OFF }.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.beatCrossfadeConfig.mode == mode,
                                onClick = { viewModel.setBeatCrossfadeMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tempo Bend Tolerance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "±${themeConfig.beatCrossfadeConfig.maxTempoBendPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.beatCrossfadeConfig.maxTempoBendPercent.toFloat(),
                        onValueChange = { viewModel.setBeatCrossfadeMaxBend(it.toInt()) },
                        valueRange = 2f..12f,
                        steps = 9
                    )

                    val demoPlan = com.nexus.tune.core.domain.audio.BeatSyncCrossfadeEngine.calculateTransitionPlan(
                        outgoingProfile = com.nexus.tune.core.model.TrackHarmonicProfile("out", com.nexus.tune.core.model.CamelotKey.KEY_8B, 124, 80),
                        incomingProfile = com.nexus.tune.core.model.TrackHarmonicProfile("in", com.nexus.tune.core.model.CamelotKey.KEY_9B, 126, 82),
                        mode = themeConfig.beatCrossfadeConfig.mode,
                        maxBendPercent = themeConfig.beatCrossfadeConfig.maxTempoBendPercent
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.beatCrossfadeConfig.mode.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: ${(demoPlan.targetDurationMs / 1000f)}s (${demoPlan.musicalBars} Bars)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${demoPlan.totalBeats} Beats • Ratio ${(demoPlan.tempoRatio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
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

                // --- Acoustic Silence & Outro Dead-Air Trimmer Studio ---
                Spacer(modifier = Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text("Acoustic Silence & Dead-Air Trimmer") },
                    supportingContent = { Text("Excise long dead air and outro silences with anti-click cosine smoothing") },
                    trailingContent = {
                        Switch(
                            checked = themeConfig.silenceTrimConfig.isEnabled,
                            onCheckedChange = { viewModel.setSilenceTrimEnabled(it) }
                        )
                    }
                )

                if (themeConfig.silenceTrimConfig.isEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trimmer Sensitivity Mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SilenceTrimMode.entries.filter { it != SilenceTrimMode.OFF }.forEach { mode ->
                            FilterChip(
                                selected = themeConfig.silenceTrimConfig.mode == mode,
                                onClick = { viewModel.setSilenceTrimMode(mode) },
                                label = { Text(mode.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RMS Silence Threshold",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${themeConfig.silenceTrimConfig.thresholdDb.toInt()} dBFS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = themeConfig.silenceTrimConfig.thresholdDb,
                        onValueChange = { viewModel.setSilenceTrimThresholdDb(it) },
                        valueRange = -60f..-25f,
                        steps = 34
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = themeConfig.silenceTrimConfig.mode.description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Min Pause: ${(themeConfig.silenceTrimConfig.minDurationMs / 1000f)}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Anti-Click: ${themeConfig.silenceTrimConfig.smoothEnvelopeMs}ms Cos² Ramp",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

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
