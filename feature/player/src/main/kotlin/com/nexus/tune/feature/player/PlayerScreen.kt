package com.nexus.tune.feature.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexus.tune.core.model.PlaybackState
import com.nexus.tune.core.model.PlaybackStatus
import com.nexus.tune.core.model.PlayerLayout
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.TrackBookmark
import com.nexus.tune.core.model.TrackLyrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    layout: PlayerLayout = PlayerLayout.ART_FORWARD,
    lyrics: TrackLyrics? = null,
    lyricsOffsetMs: Long = 0L,
    audioOutputDevice: com.nexus.tune.core.model.AudioOutputDevice = com.nexus.tune.core.model.AudioOutputDevice(),
    visualizerData: com.nexus.tune.core.model.VisualizerData = com.nexus.tune.core.model.VisualizerData(),
    visualizerMode: com.nexus.tune.core.model.VisualizerMode = com.nexus.tune.core.model.VisualizerMode.SPECTRUM_BARS,
    onAdjustLyricsOffset: (Long) -> Unit = {},
    onResetLyricsOffset: () -> Unit = {},
    onSetPlaybackSpeed: (Float) -> Unit = {},
    onSetVisualizerMode: (com.nexus.tune.core.model.VisualizerMode) -> Unit = {},
    abLoopState: com.nexus.tune.core.model.ABLoopState = com.nexus.tune.core.model.ABLoopState(),
    onSetABPointA: () -> Unit = {},
    onSetABPointB: () -> Unit = {},
    onToggleABLoop: () -> Unit = {},
    onClearABLoop: () -> Unit = {},
    seekbarStyle: com.nexus.tune.core.model.SeekbarStyle = com.nexus.tune.core.model.SeekbarStyle.WAVEFORM,
    onSetSemitones: (Int) -> Unit = {},
    onTransposeUp: () -> Unit = {},
    onTransposeDown: () -> Unit = {},
    onResetPitch: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onCycleRepeatMode: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onImportLrc: (String) -> Unit = {},
    onExportLrc: () -> Unit = {},
    bookmarks: List<TrackBookmark> = emptyList(),
    onAddBookmark: (String) -> Unit = {},
    onDeleteBookmark: (Long) -> Unit = {},
    onSeekToBookmark: (TrackBookmark) -> Unit = {},
    queue: List<Track> = emptyList(),
    queueIndex: Int = 0,
    onJumpToQueueIndex: (Int) -> Unit = {},
    onRemoveFromQueue: (Int) -> Unit = {},
    onClearUpcoming: () -> Unit = {},
    onDeduplicateQueue: () -> Unit = {},
    djFxConfig: com.nexus.tune.core.model.DjFxConfig = com.nexus.tune.core.model.DjFxConfig(),
    onSetDjPreset: (com.nexus.tune.core.model.DjPreset) -> Unit = {},
    onSetDjSpeed: (Float) -> Unit = {},
    onSetDjSemitones: (Int) -> Unit = {},
    onSetDjSoundstageWidth: (Float) -> Unit = {},
    onToggleDjLoFi: () -> Unit = {},
    onResetDjFx: () -> Unit = {}
) {
    val track = playbackState.currentTrack ?: return
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING
    var showImportLrcDialog by remember { mutableStateOf(false) }
    var lrcInputText by remember { mutableStateOf("") }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showDjSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Dismiss")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (layout) {
                            PlayerLayout.ART_FORWARD -> "ART FORWARD"
                            PlayerLayout.VINYL_RECORD -> "VINYL RECORD"
                            PlayerLayout.SYNCED_LYRICS -> "SYNCED LYRICS"
                            PlayerLayout.MINIMALIST -> "MINIMALIST"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = if (audioOutputDevice.isHighRes) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (audioOutputDevice.type) {
                                    com.nexus.tune.core.model.AudioDeviceType.BLUETOOTH_A2DP -> Icons.Rounded.Bluetooth
                                    com.nexus.tune.core.model.AudioDeviceType.USB_DAC -> Icons.Rounded.Headphones
                                    com.nexus.tune.core.model.AudioDeviceType.WIRED_HEADSET -> Icons.Rounded.Headphones
                                    else -> Icons.AutoMirrored.Rounded.VolumeUp
                                },
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (audioOutputDevice.isHighRes) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (audioOutputDevice.isHighRes && audioOutputDevice.type == com.nexus.tune.core.model.AudioDeviceType.USB_DAC) {
                                    "${audioOutputDevice.name} • ${audioOutputDevice.fidelityDescription}"
                                } else if (audioOutputDevice.isHighRes) {
                                    "${audioOutputDevice.name} (Hi-Res)"
                                } else {
                                    audioOutputDevice.name
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (audioOutputDevice.isHighRes) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDjSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "DJ Sound Effects",
                            tint = if (djFxConfig.preset != com.nexus.tune.core.model.DjPreset.NORMAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = "Up Next Queue")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Central Area based on selected layout
            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (layout) {
                    PlayerLayout.ART_FORWARD -> {
                        AsyncImage(
                            model = track.artworkUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(280.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                        )
                    }
                    PlayerLayout.VINYL_RECORD -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = if (isPlaying) 360f else 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(12000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(280.dp)
                                .graphicsLayer { rotationZ = rotation }
                                .clip(CircleShape)
                                .background(Color(0xFF1B1B1B))
                        ) {
                            AsyncImage(
                                model = track.artworkUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    PlayerLayout.SYNCED_LYRICS -> {
                        val currentMs = (playbackState.positionMs + lyricsOffsetMs).coerceAtLeast(0L)
                        val lines = lyrics?.lines ?: emptyList()
                        if (lines.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Lyrics unavailable for this track.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showImportLrcDialog = true }) {
                                    Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import .LRC Lyrics")
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(
                                        onClick = { onAdjustLyricsOffset(-100L) },
                                        label = { Text("-100ms") }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (lyricsOffsetMs >= 0) "+${lyricsOffsetMs}ms" else "${lyricsOffsetMs}ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AssistChip(
                                        onClick = { onAdjustLyricsOffset(100L) },
                                        label = { Text("+100ms") }
                                    )
                                    if (lyricsOffsetMs != 0L) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        AssistChip(
                                            onClick = { onResetLyricsOffset() },
                                            label = { Text("Reset") }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AssistChip(
                                        onClick = { showImportLrcDialog = true },
                                        label = { Text("Import") }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AssistChip(
                                        onClick = onExportLrc,
                                        label = { Text("Export") }
                                    )
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    itemsIndexed(lines) { index, line ->
                                        val isCurrent = currentMs >= line.startTimeMs &&
                                            (index == lines.lastIndex || currentMs < lines[index + 1].startTimeMs)
                                        Text(
                                            text = line.text,
                                            style = if (isCurrent) {
                                                MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                            } else {
                                                MaterialTheme.typography.bodyLarge
                                            },
                                            color = if (isCurrent) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            },
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    PlayerLayout.MINIMALIST -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AsyncImage(
                                model = track.artworkUrl,
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            if (layout != PlayerLayout.MINIMALIST) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Visualizer
            if (visualizerMode != com.nexus.tune.core.model.VisualizerMode.OFF) {
                com.nexus.tune.core.ui.AudioVisualizerView(
                    visualizerData = visualizerData,
                    mode = visualizerMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seekbar
            val progress = if (playbackState.durationMs > 0) {
                (playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            if (seekbarStyle == com.nexus.tune.core.model.SeekbarStyle.WAVEFORM) {
                com.nexus.tune.core.ui.WaveformSeekbar(
                    progress = progress,
                    trackId = track.id,
                    onSeek = { fraction ->
                        onSeek((fraction * playbackState.durationMs).toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                )
            } else {
                Slider(
                    value = progress,
                    onValueChange = { newFrac ->
                        onSeek((newFrac * playbackState.durationMs).toLong())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(playbackState.positionMs), style = MaterialTheme.typography.labelSmall)
                Text(text = formatTime(playbackState.durationMs), style = MaterialTheme.typography.labelSmall)
            }

            // A-B Repeat Loop Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = abLoopState.startMs != null,
                    onClick = onSetABPointA,
                    label = {
                        val textA = abLoopState.startMs?.let { "A: ${formatTime(it)}" } ?: "Set A"
                        Text(textA, style = MaterialTheme.typography.labelSmall)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                FilterChip(
                    selected = abLoopState.endMs != null,
                    onClick = onSetABPointB,
                    label = {
                        val textB = abLoopState.endMs?.let { "B: ${formatTime(it)}" } ?: "Set B"
                        Text(textB, style = MaterialTheme.typography.labelSmall)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                if (abLoopState.isEnabled && abLoopState.startMs != null && abLoopState.endMs != null) {
                    FilterChip(
                        selected = true,
                        onClick = onToggleABLoop,
                        label = {
                            Text("Loop #${abLoopState.loopCount}", style = MaterialTheme.typography.labelSmall)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onClearABLoop,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear A-B Loop",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bookmarks & Chapters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { showAddBookmarkDialog = true },
                    label = { Text("Bookmark", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Rounded.BookmarkAdd, contentDescription = "Add Bookmark", modifier = Modifier.size(16.dp))
                    }
                )
                bookmarks.forEach { bookmark ->
                    val isNear = kotlin.math.abs(playbackState.positionMs - bookmark.positionMs) < 2500L
                    InputChip(
                        selected = isNear,
                        onClick = { onSeekToBookmark(bookmark) },
                        label = { Text("${bookmark.formattedPosition} ${bookmark.title}", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Delete Bookmark",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onDeleteBookmark(bookmark.id) }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ergonomic Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = onSkipNext, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = onCycleRepeatMode) {
                    Icon(
                        imageVector = if (playbackState.isRepeatOne) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.isRepeatOne || playbackState.isRepeatAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Speed Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0.75f to "0.75x", 1.0f to "1.0x", 1.25f to "1.25x", 1.5f to "1.5x", 2.0f to "2.0x").forEach { (speed, label) ->
                    val isSelected = kotlin.math.abs(playbackState.playbackSpeed - speed) < 0.05f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetPlaybackSpeed(speed) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Key & Pitch Transposition Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Tune,
                        contentDescription = "Key Transpose",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Key: ${com.nexus.tune.core.player.PitchTransposer.formatSemitones(playbackState.playbackPitchSemitones)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (playbackState.playbackPitchSemitones != 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${com.nexus.tune.core.player.PitchTransposer.formatPitchMultiplier(playbackState.playbackPitch)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onTransposeDown,
                        modifier = Modifier.size(30.dp),
                        enabled = playbackState.playbackPitchSemitones > com.nexus.tune.core.player.PitchTransposer.MIN_SEMITONES
                    ) {
                        Text("-1", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    FilledTonalIconButton(
                        onClick = onTransposeUp,
                        modifier = Modifier.size(30.dp),
                        enabled = playbackState.playbackPitchSemitones < com.nexus.tune.core.player.PitchTransposer.MAX_SEMITONES
                    ) {
                        Text("+1", style = MaterialTheme.typography.labelSmall)
                    }
                    if (playbackState.playbackPitchSemitones != 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onResetPitch,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset Key", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Quick Transpose Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(-2 to "-2 st", -1 to "-1 st", 0 to "Orig", 1 to "+1 st", 2 to "+2 st").forEach { (st, label) ->
                    val isSelected = playbackState.playbackPitchSemitones == st
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetSemitones(st) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visualizer Mode Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    com.nexus.tune.core.model.VisualizerMode.OFF to "Off",
                    com.nexus.tune.core.model.VisualizerMode.SPECTRUM_BARS to "Spectrum",
                    com.nexus.tune.core.model.VisualizerMode.CIRCULAR_WAVE to "Wave",
                    com.nexus.tune.core.model.VisualizerMode.GLOW_PULSE to "Pulse"
                ).forEach { (mode, label) ->
                    val isSelected = visualizerMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetVisualizerMode(mode) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showImportLrcDialog) {
            AlertDialog(
                onDismissRequest = { showImportLrcDialog = false },
                title = { Text("Import LRC Lyrics") },
                text = {
                    Column {
                        Text(
                            text = "Paste standard timed LRC lines (e.g. [00:15.20] Lyric line):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lrcInputText,
                            onValueChange = { lrcInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = { Text("[00:12.50] Line 1\n[00:18.00] Line 2") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (lrcInputText.isNotBlank()) {
                                onImportLrc(lrcInputText)
                            }
                            showImportLrcDialog = false
                        }
                    ) {
                        Text("Save & Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportLrcDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddBookmarkDialog) {
            var bookmarkTitle by remember { mutableStateOf("") }
            val currentPosMs = playbackState.positionMs
            AlertDialog(
                onDismissRequest = { showAddBookmarkDialog = false },
                title = { Text("Add Bookmark") },
                text = {
                    Column {
                        Text(
                            text = "Timestamp: ${formatTime(currentPosMs)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bookmarkTitle,
                            onValueChange = { bookmarkTitle = it },
                            label = { Text("Bookmark Label") },
                            placeholder = { Text("e.g. Solo, Drop, Breakdown") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddBookmark(bookmarkTitle)
                            showAddBookmarkDialog = false
                        }
                    ) {
                        Text("Save Bookmark")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBookmarkDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val upcomingCount = (queue.size - 1 - queueIndex).coerceAtLeast(0)
                    val upcomingMinutes = queue.drop(queueIndex + 1).sumOf { it.durationSeconds } / 60

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Up Next Queue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$upcomingCount upcoming tracks • ~${upcomingMinutes} min remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = onDeduplicateQueue,
                                enabled = queue.isNotEmpty()
                            ) {
                                Text("Deduplicate")
                            }
                            if (upcomingCount > 0) {
                                TextButton(
                                    onClick = onClearUpcoming
                                ) {
                                    Text("Clear", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (queue.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Queue is empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            itemsIndexed(queue) { index, item ->
                                val isCurrent = index == queueIndex
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else Color.Transparent
                                        )
                                        .clickable { onJumpToQueueIndex(index) }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Currently playing",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(24.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    AsyncImage(
                                        model = item.artworkUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.artist} • ${formatTime(item.durationSeconds * 1000L)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveFromQueue(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove from queue",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDjSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDjSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DJ Sound FX & Transformation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aesthetic tempo, pitch & soundstage modulation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (djFxConfig.preset != com.nexus.tune.core.model.DjPreset.NORMAL) {
                            TextButton(onClick = onResetDjFx) {
                                Text("Reset FX", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.nexus.tune.core.model.DjPreset.values().forEach { preset ->
                            val isSelected = djFxConfig.preset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetDjPreset(preset) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Overview Card
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SPEED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2fx", djFxConfig.speed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PITCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when {
                                        djFxConfig.semitones > 0 -> "+${djFxConfig.semitones} st"
                                        djFxConfig.semitones < 0 -> "${djFxConfig.semitones} st"
                                        else -> "0 st"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "STAGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(djFxConfig.soundstageWidth * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Playback Tempo / Speed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.2fx", djFxConfig.speed),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = djFxConfig.speed,
                        onValueChange = onSetDjSpeed,
                        valueRange = 0.50f..2.00f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch Semitones Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vocal Pitch Transposition",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSetDjSemitones(djFxConfig.semitones - 1) },
                                enabled = djFxConfig.semitones > -6,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Pitch down", modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = when {
                                    djFxConfig.semitones > 0 -> "+${djFxConfig.semitones}"
                                    djFxConfig.semitones < 0 -> "${djFxConfig.semitones}"
                                    else -> "0"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onSetDjSemitones(djFxConfig.semitones + 1) },
                                enabled = djFxConfig.semitones < 6,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Pitch up", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Soundstage Width Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stereo Soundstage Width",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(djFxConfig.soundstageWidth * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Slider(
                        value = djFxConfig.soundstageWidth,
                        onValueChange = onSetDjSoundstageWidth,
                        valueRange = 0.0f..2.0f,
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lo-Fi Warmth Filter Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Vintage Lo-Fi Filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Bandpass warmth and subtle analog texture",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = djFxConfig.loFiFilterEnabled,
                            onCheckedChange = { onToggleDjLoFi() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
