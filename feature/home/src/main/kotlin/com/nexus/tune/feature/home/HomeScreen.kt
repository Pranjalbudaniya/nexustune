package com.nexus.tune.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

