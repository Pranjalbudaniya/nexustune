package com.nexus.tune.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.tune.core.model.MoodCategory
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.ui.ErrorBanner
import com.nexus.tune.core.ui.ShimmerSkeleton
import com.nexus.tune.core.ui.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NexusTune",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        repeat(5) {
                            ShimmerSkeleton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                is HomeUiState.Error -> {
                    ErrorBanner(
                        message = state.message,
                        onRetry = { viewModel.loadHomeFeed() }
                    )
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Mood Filter Carousel
                        item {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(MoodCategory.values()) { mood ->
                                    FilterChip(
                                        selected = state.selectedMood == mood,
                                        onClick = { viewModel.selectMood(mood) },
                                        label = { Text(mood.displayName) }
                                    )
                                }
                            }
                        }

                        // Mood Radio Hero Card (when a specific mood is selected)
                        if (state.selectedMood != MoodCategory.ALL) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${state.selectedMood.displayName} Radio",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = state.selectedMood.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.startMoodRadio(state.selectedMood) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Start Radio")
                                        }
                                    }
                                }
                            }
                        }

                        if (state.playlists.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No feed playlists match ${state.selectedMood.displayName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(onClick = { viewModel.startMoodRadio(state.selectedMood) }) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Launch Smart ${state.selectedMood.displayName} Radio")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(state.playlists) { playlist ->
                                Text(
                                    text = playlist.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                Column {
                                    playlist.tracks.forEach { track ->
                                        TrackListItem(
                                            track = track,
                                            onClick = {
                                                viewModel.playTrack(track, playlist.tracks)
                                                onTrackClick(track, playlist.tracks)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
