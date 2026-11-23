# NexusTune Architecture Specification

## 1. Multi-Module Topology
NexusTune is architected with strict unidirectional dependencies, separating domain logic, platform bindings, data persistence, and UI rendering:

```
                      ┌────────────────┐
                      │      :app      │
                      └───────┬────────┘
                              │
         ┌────────────────────┼────────────────────┐
         ▼                    ▼                    ▼
   :feature:home       :feature:search     :feature:player
   :feature:library    :feature:settings
         │                    │                    │
         └────────────────────┼────────────────────┘
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
          :core:ui                  :core:player
               │                             │
               ▼                             ▼
      :core:designsystem              :core:data
               │                             │
               │               ┌─────────────┴─────────────┐
               │               ▼                           ▼
               │        :core:database             :core:extractor
               │               │                           │
               ▼               ▼                           ▼
          :core:model    :core:datastore             :core:common
               ▲
               │
          :core:domain (Pure Kotlin, Zero Android dependencies)
```

---

## 2. Module Responsibilities and Rules

### `:core:model` (Pure Kotlin / Android Library)
- Domain entities: `Track`, `Album`, `Artist`, `Playlist`, `AudioStream`, `PlaybackQueue`, `SearchResult`.
- Immutable value objects with stable identifiers.

### `:core:common`
- Kotlin Coroutines dispatchers provider (`IoDispatcher`, `DefaultDispatcher`, `MainDispatcher`).
- Typed `MusicResult` and `ExtractorError` hierarchies.

### `:core:domain` (Pure Kotlin Library)
- **Constraint**: Strictly ZERO Android dependencies (`import android.*` forbidden).
- Use cases: `GetHomeFeedUseCase`, `SearchMusicUseCase`, `ResolveStreamUseCase`, `ManageQueueUseCase`, `GetLibraryTracksUseCase`.
- Repository interfaces: `MusicRepository`, `QueueRepository`, `SettingsRepository`, `DownloadRepository`.

### `:core:database` (Android Library)
- Room 2.6+ database: `NexusTuneDatabase`.
- Entities: `TrackEntity`, `PlaylistEntity`, `PlaylistTrackCrossRef`, `HistoryEntity`, `QueueEntity`.
- TypeConverters and DAOs (`TrackDao`, `PlaylistDao`, `QueueDao`, `HistoryDao`).

### `:core:datastore` (Android Library)
- Jetpack DataStore Preferences for user settings: `ThemeConfig`, `AudioConfig`, `CacheConfig`.
- Fully reactive `StateFlow` streams.

### `:core:extractor` (Android Library)
- `MusicSource` interface abstraction.
- `NewPipeMusicSource`: Implements YouTube Music extraction via pinned `NewPipeExtractor v0.27.1`.
- `FakeMusicSource`: Deterministic in-memory fixtures for all tests.

### `:core:player` (Android Library)
- Media3 `MediaSessionService` (`NexusPlaybackService`).
- Audio effect controllers (5-band EQ, BassBoost).
- Media3 `ResolvingDataSource` with exponential backoff on 403/410.

### `:core:designsystem` (Android Library)
- Material 3 Expressive theme (`NexusTuneTheme`).
- Color schemes (Dynamic, AMOLED, Seed palettes).
- Shape presets (Soft Rounded, Sharp, Full Expressive).
- Physics-based motion spring specs.

### `:core:ui` (Android Library)
- Shared composables: `MiniPlayerBar`, `TrackListItem`, `ShimmerSkeleton`, `ErrorBanner`.

### `:core:testing`
- Shared test rules, test dispatchers, fake repositories, and sample track fixtures.

### Feature Modules (`:feature:*`)
- Never depend on each other. Talk via navigation contracts and `:core:*`.
- ViewModels expose a single immutable `StateFlow<UiState>`.

