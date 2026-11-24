# Loop 40: Smart Listening Mood & Energy Radio Matrix Engine (v5.0.0 Milestone)

## Objective
Celebrate the **NexusTune v5.0.0** major milestone by introducing the **Smart Listening Mood & Energy Radio Matrix Engine**. This feature provides an interactive mood carousel on the Home screen and Library (All, Energize, Deep Focus, Relax & Chill, Workout, Commute, Bedtime), dynamically filtering feeds, categorizing tracks by audio energy/tempo heuristics, and generating instant continuous smart mood radios.

## Scope of Work

### 1. Data & Domain Layer
- **Model**: Add `MoodCategory` enum to `:core:model`:
  - `ALL`, `ENERGIZE`, `FOCUS`, `CHILL`, `WORKOUT`, `COMMUTE`, `SLEEP`
  - Display names, descriptions, and iconography.
- **MoodRadioEngine** in `:core:domain:mood`:
  - Algorithmic classification: Matches track titles, artists, and genre metadata against energy curves and mood keywords.
  - `filterTracksByMood(tracks: List<Track>, mood: MoodCategory): List<Track>`
  - `filterPlaylistsByMood(playlists: List<Playlist>, mood: MoodCategory): List<Playlist>`
  - `createMoodRadio(pool: List<Track>, mood: MoodCategory, count: Int = 25): List<Track>`

### 2. Feature & Presentation Layer
- **HomeViewModel**:
  - `selectedMood: StateFlow<MoodCategory>`
  - `fun selectMood(mood: MoodCategory)`
  - Reactive `uiState` providing filtered playlists and mood radio generation.
  - `fun startMoodRadio(mood: MoodCategory)` -> primers queue with 20+ matching tracks and initiates playback.
- **HomeScreen**:
  - Interactive top horizontal carousel of `FilterChip` items for all mood categories.
  - Featured "Mood Radio" card with quick play action when a mood is selected.
  - Filtered playlists matching selected mood.

### 3. Verification & Testing
- Unit tests:
  - `MoodRadioEngineTest` in `:core:domain` verifying track classification, playlist filtering, and radio generation.
  - `HomeViewModelTest` in `:feature:home` verifying mood selection and radio queue generation.
- Gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag `v5.0-1`.
