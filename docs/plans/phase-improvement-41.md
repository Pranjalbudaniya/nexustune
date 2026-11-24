# Loop 41: In-Library Instant Search, Multi-Criteria Sort & Smart Filter Engine (v5.1.0 Milestone)

## Objective
Implement an in-library real-time search, multi-criteria sorting, and smart filtering engine in NexusTune, enabling listeners to instantly find, sort, and batch-play tracks and playlists across their saved library without leaving the Library tab or depending on an active internet connection.

## Scope of Work

### 1. Data & Domain Layer
- **Model**: Add `LibrarySortOrder` enum in `:core:model`:
  - `RECENT` ("Recently Added")
  - `TITLE_ASC` ("Title (A-Z)")
  - `ARTIST_ASC` ("Artist (A-Z)")
  - `DURATION_DESC` ("Duration (Longest)")
- **LibraryFilterEngine** in `:core:domain:library`:
  - `filterTracks(tracks: List<Track>, query: String, sortOrder: LibrarySortOrder): List<Track>`
  - `filterPlaylists(playlists: List<Playlist>, query: String): List<Playlist>`
  - Case-insensitive, multi-field matching (title, artist, album).

### 2. Feature & Presentation Layer
- **LibraryViewModel**:
  - `searchQuery: StateFlow<String>`
  - `sortOrder: StateFlow<LibrarySortOrder>`
  - `filteredLikedTracks: StateFlow<List<Track>>` (combined flow of likedTracks + query + sortOrder)
  - `filteredHistoryTracks: StateFlow<List<Track>>`
  - `filteredPlaylists: StateFlow<List<Playlist>>`
  - Actions:
    - `setSearchQuery(query: String)`
    - `setSortOrder(order: LibrarySortOrder)`
    - `clearSearch()`
    - `playFilteredQueue(tracks: List<Track>)`
- **LibraryScreen**:
  - Compact search text field with clear icon button.
  - Sort order chip selector (`Recently Added`, `Title A-Z`, `Artist A-Z`, `Longest`).
  - Match count indicator when search is active.
  - "Play All" button for filtered results.

### 3. Verification & Testing
- Unit tests:
  - `LibraryFilterEngineTest` in `:core:domain` verifying search queries and sort orders.
  - `LibraryViewModelTest` in `:feature:library` verifying reactive filtering, sorting, and playback.
- Gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag `v5.1-1`.

