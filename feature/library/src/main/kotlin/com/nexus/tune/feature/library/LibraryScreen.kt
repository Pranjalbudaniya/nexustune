package com.nexus.tune.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.nexus.tune.core.model.EnergyCurve
import com.nexus.tune.core.model.LibrarySortOrder
import com.nexus.tune.core.model.Playlist
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.model.VibeMixRequest
import com.nexus.tune.core.model.VibeMixResult
import com.nexus.tune.core.model.VibeTheme
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
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    onCreateClick: () -> Unit,
    onVibeMixClick: () -> Unit = {},
    onEditArtworkClick: (Playlist) -> Unit,
    onDeleteClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
