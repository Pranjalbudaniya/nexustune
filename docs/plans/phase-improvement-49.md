# Phase Improvement Plan - Loop 49 (v5.9.0)
## Dynamic Rule-Based Smart Playlists & Algorithmic Query Engine

### 1. Context & User Motivation
Standard music playlists require tedious manual curation. Power users on desktop music managers (such as iTunes, Apple Music, and MusicBee) rely heavily on "Smart Playlists"—auto-updating collections defined by logical rules and query criteria. NexusTune v5.9.0 introduces a full **Dynamic Rule-Based Smart Playlists & Algorithmic Query Engine** enabling instant dynamic query synthesis over user libraries and history.

### 2. Architecture & Design

#### 2.1 Core Models (`:core:model`)
- `SmartRuleField`:
  - `TITLE`: string match on track title
  - `ARTIST`: string match on artist name
  - `ALBUM`: string match on album title
  - `DURATION_SECONDS`: integer comparison on duration
  - `IS_LIKED`: boolean comparison on favorite status
  - `IS_DOWNLOADED`: boolean comparison on offline status
  - `PLAY_COUNT`: integer comparison on playback count
- `SmartRuleOperator`:
  - `EQUALS`, `CONTAINS`, `NOT_CONTAINS`, `GREATER_THAN`, `LESS_THAN`, `STARTS_WITH`
- `SmartRuleMatch`:
  - `ALL` (AND conjunction)
  - `ANY` (OR disjunction)
- `SmartRuleSort`:
  - `DEFAULT`, `TITLE_ASC`, `ARTIST_ASC`, `DURATION_ASC`, `DURATION_DESC`, `PLAY_COUNT_DESC`
- `SmartPlaylistRule(id: String, field: SmartRuleField, operator: SmartRuleOperator, value: String)`
- `SmartPlaylistDefinition`:
  - `id: String`, `title: String`, `icon: String`, `matchType: SmartRuleMatch`, `rules: List<SmartPlaylistRule>`, `sortBy: SmartRuleSort`, `limit: Int`, `isBuiltIn: Boolean`

#### 2.2 Computation Engine (`:core:domain:playlist`)
- `SmartPlaylistEvaluator`:
  - Evaluates track collections against `SmartPlaylistDefinition` criteria.
  - Supports string matching (case-insensitive substring, exact, start), numeric comparisons, boolean matching.
  - Applies sorting algorithms and slicing to limit maximum tracks.
  - Provides built-in smart playlist presets:
    1. **🔥 Heavy Rotation**: Play count >= 5, sorted by Play Count DESC.
    2. **✨ Recent Favorites**: Liked tracks with positive play count.
    3. **⚡ Quick Jams**: Tracks with duration <= 180 seconds, sorted by Duration ASC.
    4. **🎧 Deep Listening**: Tracks with duration >= 300 seconds (5+ minutes).
    5. **💾 Offline Vault**: Downloaded tracks ready for airplane mode.
  - Provides JSON serialization and deserialization for user custom smart playlists.

#### 2.3 DataStore & Repository (`:core:datastore` & `:core:data`)
- `SettingsDataStore`:
  - `CUSTOM_SMART_PLAYLISTS` string preference key.
  - Methods to read, save, and delete custom smart playlist definitions.
- `SettingsRepository` & `SettingsRepositoryImpl`:
  - `getCustomSmartPlaylists(): Flow<List<SmartPlaylistDefinition>>`
  - `saveCustomSmartPlaylist(definition: SmartPlaylistDefinition)`
  - `deleteCustomSmartPlaylist(id: String)`

#### 2.4 Presentation Layer (`:feature:library`)
- `LibraryViewModel`:
  - Combines built-in presets with user-defined smart playlists.
  - `evaluateSmartPlaylist(definition: SmartPlaylistDefinition): List<Track>`
  - `playSmartPlaylist(definition: SmartPlaylistDefinition, shuffle: Boolean = false)`
  - `createSmartPlaylist(title: String, icon: String, matchType: SmartRuleMatch, rules: List<SmartPlaylistRule>, sortBy: SmartRuleSort, limit: Int)`
  - `deleteSmartPlaylist(id: String)`
- `LibraryScreen`:
  - "Smart Playlists" horizontal scroll section with stylish cards and badges.
  - Interactive Smart Playlist Preview sheet displaying matching track count, duration, matching tracks, and "Play / Shuffle All" actions.
  - "Create Smart Playlist" builder bottom sheet allowing custom title, rule selection, and limits.

#### 2.5 Verification & Release Gates
- Unit test suite:
  - `SmartPlaylistEvaluatorTest` in `core:domain`.
  - `LibraryViewModelTest` in `feature:library`.
- Quality gates:
  - `./scripts/gate.sh slice`
  - `./scripts/gate.sh final`
  - Release tag: `v5.9-1`
