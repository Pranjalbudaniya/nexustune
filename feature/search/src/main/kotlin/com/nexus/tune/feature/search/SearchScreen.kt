package com.nexus.tune.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.tune.core.model.Track
import com.nexus.tune.core.ui.ErrorBanner
import com.nexus.tune.core.ui.ShimmerSkeleton
import com.nexus.tune.core.ui.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = { viewModel.performSearch(it) },
            active = false,
            onActiveChange = {},
            placeholder = { Text("Search songs, artists, albums...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {}

        if (suggestions.isNotEmpty() && uiState is SearchUiState.Idle) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(suggestions) { suggestion ->
                    ListItem(
                        headlineContent = { Text(suggestion) },
                        leadingContent = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.onQueryChange(suggestion)
                            viewModel.performSearch(suggestion)
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Find your favorite music on NexusTune",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                is SearchUiState.Loading -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        repeat(6) {
                            ShimmerSkeleton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                is SearchUiState.Error -> {
                    ErrorBanner(
                        message = state.message,
                        onRetry = { viewModel.performSearch(query) }
                    )
                }
                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.result.tracks) { track ->
                            TrackListItem(
                                track = track,
                                onClick = {
                                    viewModel.playTrack(track)
                                    onTrackClick(track)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

