package com.nexus.tune.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexus.tune.core.model.*
import com.nexus.tune.core.ui.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val likedTracks by viewModel.likedTracks.collectAsState()
    val historyTracks by viewModel.historyTracks.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val filteredLikedTracks by viewModel.filteredLikedTracks.collectAsState()
    val filteredHistoryTracks by viewModel.filteredHistoryTracks.collectAsState()
    val filteredPlaylists by viewModel.filteredPlaylists.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPlaylistForArtwork by remember { mutableStateOf<Playlist?>(null) }
    var showVibeMixSheet by remember { mutableStateOf(false) }
    var selectedVibeTheme by remember { mutableStateOf(VibeTheme.LATE_NIGHT_CHILL) }
    var targetVibeDuration by remember { mutableIntStateOf(30) }
    var selectedEnergyCurve by remember { mutableStateOf(EnergyCurve.RAMP_UP) }
    val vibeMixPreview by viewModel.vibeMixPreview.collectAsState()
    val smartPlaylists by viewModel.smartPlaylists.collectAsState()
    val activeSmartPreview by viewModel.activeSmartPlaylistPreview.collectAsState()
    var showCreateSmartPlaylistSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(showVibeMixSheet, selectedVibeTheme, targetVibeDuration, selectedEnergyCurve) {
        if (showVibeMixSheet) {
            viewModel.previewVibeMix(
                VibeMixRequest(
                    theme = selectedVibeTheme,
                    targetDurationMinutes = targetVibeDuration,
                    energyCurve = selectedEnergyCurve
                )
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // In-Library Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text("Search your library...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(20.dp))
            },
            trailingIcon = if (searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            } else null,
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        // Sort Order Filter Row (for Liked and History)
        if (selectedTab != 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LibrarySortOrder.values().forEach { order ->
                    FilterChip(
                        selected = sortOrder == order,
                        onClick = { viewModel.setSortOrder(order) },
                        label = { Text(order.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Liked (${if (searchQuery.isNotBlank()) "${filteredLikedTracks.size}/" else ""}${likedTracks.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Playlists (${if (searchQuery.isNotBlank()) "${filteredPlaylists.size}/" else ""}${playlists.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("History (${if (searchQuery.isNotBlank()) "${filteredHistoryTracks.size}/" else ""}${historyTracks.size})") }
            )
        }

        when (selectedTab) {
            0 -> {
                // Liked Tracks
                val dedupStatus by viewModel.deduplicationStatus.collectAsState()
                if (dedupStatus != null) {
                    Text(
                        text = dedupStatus ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "${filteredLikedTracks.size} found",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (filteredLikedTracks.isNotEmpty() && searchQuery.isNotBlank()) {
                            Button(
                                onClick = { viewModel.playAll(filteredLikedTracks) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Matches", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (likedTracks.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.deduplicateLikedTracks() },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clean Duplicates", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                TrackListTab(
                    tracks = filteredLikedTracks,
                    emptyMessage = if (searchQuery.isNotBlank()) "No songs match \"$searchQuery\"." else "No liked songs yet.",
                    onTrackClick = { track ->
                        viewModel.playTrack(track, filteredLikedTracks)
                        onTrackClick(track, filteredLikedTracks)
                    }
                )
            }
            1 -> {
                // Playlists Tab
                PlaylistsTab(
                    playlists = filteredPlaylists,
                    smartPlaylists = smartPlaylists,
                    onSmartPlaylistClick = { viewModel.previewSmartPlaylist(it) },
                    onCreateSmartPlaylistClick = { showCreateSmartPlaylistSheet = true },
                    onCreateClick = { showCreateDialog = true },
                    onVibeMixClick = { showVibeMixSheet = true },
                    onEditArtworkClick = { editingPlaylistForArtwork = it },
                    onDeleteClick = { viewModel.deletePlaylist(it.id) }
                )
            }
            2 -> {
                // History Tab
                if (searchQuery.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredHistoryTracks.size} found in history",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (filteredHistoryTracks.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.playAll(filteredHistoryTracks) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Matches", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                TrackListTab(
                    tracks = filteredHistoryTracks,
                    emptyMessage = if (searchQuery.isNotBlank()) "No history tracks match \"$searchQuery\"." else "Listening history will appear here.",
                    onTrackClick = { track ->
                        viewModel.playTrack(track, filteredHistoryTracks)
                        onTrackClick(track, filteredHistoryTracks)
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, artworkUrl ->
                viewModel.createPlaylist(title, artworkUrl)
                showCreateDialog = false
            }
        )
    }

    editingPlaylistForArtwork?.let { playlist ->
        EditArtworkDialog(
            playlist = playlist,
            onDismiss = { editingPlaylistForArtwork = null },
            onConfirm = { artworkUrl ->
                viewModel.updatePlaylistArtwork(playlist.id, artworkUrl)
                editingPlaylistForArtwork = null
            }
        )
    }

    if (showVibeMixSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVibeMixSheet = false },
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
                            text = "✨ Smart Vibe Mix Generator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Acoustic moods & energy curves tailored to your listening",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showVibeMixSheet = false }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vibe Theme Selector Chips
                Text(
                    text = "Select Vibe Aesthetic",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VibeTheme.values().forEach { theme ->
                        val isSelected = selectedVibeTheme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedVibeTheme = theme },
                            label = { Text("${theme.icon} ${theme.displayName}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Selector Chips
                Text(
                    text = "Target Duration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val isSelected = targetVibeDuration == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetVibeDuration = mins },
                            label = { Text("$mins mins") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Energy Curve Chips
                Text(
                    text = "Energy Flow Curve",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyCurve.values().forEach { curve ->
                        val isSelected = selectedEnergyCurve == curve
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedEnergyCurve = curve },
                            label = { Text(curve.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview Summary Card
                val preview = vibeMixPreview
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preview?.title ?: "Generating Mix...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${preview?.tracks?.size ?: 0} tracks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated length: ${preview?.formattedDuration ?: "0 min"} • Curve: ${selectedEnergyCurve.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            preview?.let { mix ->
                                viewModel.playVibeMixNow(mix)
                                showVibeMixSheet = false
                            }
                        },
                        enabled = preview != null && preview.tracks.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Now")
                    }

                    Button(
                        onClick = {
                            preview?.let { mix ->
                                viewModel.saveVibeMixAsPlaylist(mix) {
                                    android.widget.Toast.makeText(context, "Saved to your playlists!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                showVibeMixSheet = false
                            }
                        },
                        enabled = preview != null && preview.tracks.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Playlist")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    activeSmartPreview?.let { (definition, evaluatedTracks) ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSmartPlaylistPreview() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = definition.icon, style = MaterialTheme.typography.headlineMedium)
                        Column {
                            Text(
                                text = definition.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "${evaluatedTracks.size} matching tracks • Limit ${definition.limit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!definition.isBuiltIn) {
                        IconButton(onClick = { viewModel.deleteCustomSmartPlaylist(definition.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (definition.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = definition.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.playSmartPlaylist(definition, shuffle = false)
                            viewModel.dismissSmartPlaylistPreview()
                        },
                        enabled = evaluatedTracks.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.playSmartPlaylist(definition, shuffle = true)
                            viewModel.dismissSmartPlaylistPreview()
                        },
                        enabled = evaluatedTracks.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🔀 Shuffle")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (evaluatedTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tracks currently match these criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        items(evaluatedTracks, key = { it.id }) { track ->
                            TrackListItem(
                                track = track,
                                onClick = {
                                    viewModel.playTrack(track, evaluatedTracks)
                                    onTrackClick(track, evaluatedTracks)
                                    viewModel.dismissSmartPlaylistPreview()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showCreateSmartPlaylistSheet) {
        CreateSmartPlaylistSheet(
            onDismiss = { showCreateSmartPlaylistSheet = false },
            onConfirm = { title, icon, description, matchType, rules, sortBy, limit ->
                viewModel.createCustomSmartPlaylist(
                    title = title,
                    icon = icon,
                    description = description,
                    matchType = matchType,
                    rules = rules,
                    sortBy = sortBy,
                    limit = limit
                )
                showCreateSmartPlaylistSheet = false
            }
        )
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    smartPlaylists: List<SmartPlaylistDefinition> = emptyList(),
    onSmartPlaylistClick: (SmartPlaylistDefinition) -> Unit = {},
    onCreateSmartPlaylistClick: () -> Unit = {},
    onCreateClick: () -> Unit,
    onVibeMixClick: () -> Unit = {},
    onEditArtworkClick: (Playlist) -> Unit,
    onDeleteClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Dynamic Smart Playlists Section
        Text(
            text = "⚡ Dynamic Smart Playlists",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            smartPlaylists.forEach { pl ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onSmartPlaylistClick(pl) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = pl.icon, style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (pl.isBuiltIn) "Auto" else "Custom",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pl.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pl.description.ifBlank { "${pl.rules.size} rules" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            // Create custom smart playlist card
            OutlinedCard(
                modifier = Modifier
                    .width(130.dp)
                    .height(110.dp)
                    .clickable { onCreateSmartPlaylistClick() },
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Smart Playlist",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New Rule",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Collections",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onVibeMixClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("✨ Vibe Mix", style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(
                    onClick = onCreateClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No custom playlists yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create a playlist with custom artwork to organize your favorite music.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onCreateClick) {
                        Text("Create First Playlist")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onEditArtworkClick = { onEditArtworkClick(playlist) },
                        onDeleteClick = { onDeleteClick(playlist) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: Playlist,
    onEditArtworkClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!playlist.artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = playlist.artworkUrl,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlist.trackCount} tracks • ${playlist.author ?: "Personal"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onEditArtworkClick) {
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = "Edit Artwork",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete Playlist",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun TrackListTab(
    tracks: List<Track>,
    emptyMessage: String,
    onTrackClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(tracks) { track ->
                TrackListItem(
                    track = track,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, artworkUrl: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artworkUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Playlist Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artworkUrl,
                    onValueChange = { artworkUrl = it },
                    label = { Text("Custom Cover Art URI (Optional)") },
                    placeholder = { Text("https://... or content://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, artworkUrl) },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditArtworkDialog(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onConfirm: (artworkUrl: String?) -> Unit
) {
    var artworkUrl by remember { mutableStateOf(playlist.artworkUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cover Art") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Update the artwork URI for \"${playlist.title}\":",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = artworkUrl,
                    onValueChange = { artworkUrl = it },
                    label = { Text("Artwork URL / URI") },
                    placeholder = { Text("Leave empty to use dynamic gradient") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(artworkUrl.ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSmartPlaylistSheet(
    onDismiss: () -> Unit,
    onConfirm: (title: String, icon: String, description: String, matchType: SmartRuleMatch, rules: List<SmartPlaylistRule>, sortBy: SmartRuleSort, limit: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("✨") }
    var description by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf(SmartRuleMatch.ALL) }
    var selectedField by remember { mutableStateOf(SmartRuleField.PLAY_COUNT) }
    var selectedOperator by remember { mutableStateOf(SmartRuleOperator.GREATER_THAN) }
    var ruleValue by remember { mutableStateOf("3") }
    var sortBy by remember { mutableStateOf(SmartRuleSort.PLAY_COUNT_DESC) }
    var limit by remember { mutableIntStateOf(50) }

    val icons = listOf("✨", "🔥", "🔮", "⚡", "🎧", "🌙", "⭐", "💎", "🚀")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "⚡ Create Dynamic Smart Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Icon selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEach { icon ->
                    FilterChip(
                        selected = selectedIcon == icon,
                        onClick = { selectedIcon = icon },
                        label = { Text(icon, style = MaterialTheme.typography.titleMedium) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Playlist Title") },
                placeholder = { Text("e.g. Chill Beats") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("e.g. Tracks with high energy and favorites") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Rule Field:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartRuleField.entries.forEach { field ->
                    FilterChip(
                        selected = selectedField == field,
                        onClick = {
                            selectedField = field
                            ruleValue = when (field) {
                                SmartRuleField.PLAY_COUNT -> "3"
                                SmartRuleField.DURATION_SECONDS -> "180"
                                SmartRuleField.IS_LIKED -> "true"
                                SmartRuleField.IS_DOWNLOADED -> "true"
                                else -> ""
                            }
                        },
                        label = { Text(field.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Comparison Operator:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartRuleOperator.entries.forEach { op ->
                    FilterChip(
                        selected = selectedOperator == op,
                        onClick = { selectedOperator = op },
                        label = { Text(op.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ruleValue,
                onValueChange = { ruleValue = it },
                label = { Text("Criteria Value") },
                placeholder = { Text("Value to match (e.g. Rock, 180, true)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Sort Order:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartRuleSort.entries.forEach { sort ->
                    FilterChip(
                        selected = sortBy == sort,
                        onClick = { sortBy = sort },
                        label = { Text(sort.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Max Track Limit: $limit", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(25, 50, 100).forEach { lim ->
                        FilterChip(
                            selected = limit == lim,
                            onClick = { limit = lim },
                            label = { Text("$lim", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val rule = SmartPlaylistRule(
                        field = selectedField,
                        operator = selectedOperator,
                        value = ruleValue.trim()
                    )
                    onConfirm(title, selectedIcon, description, matchType, listOf(rule), sortBy, limit)
                },
                enabled = title.isNotBlank() && ruleValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Smart Playlist")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
