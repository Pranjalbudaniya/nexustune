# Architectural Decision Records (ADRs)

## Summary of Decisions
- **ADR-001**: Application Name, Identity, and Package Structure
- **ADR-002**: Clean Multi-Module Architecture and Boundaries
- **ADR-003**: Media3 Playback Service, ResolvingDataSource & Gapless Strategy
- **ADR-004**: Offline Download Strategy (WorkManager vs FGS)
- **ADR-005**: Synced Lyrics Integration (LRCLIB Open Provider)
- **ADR-006**: 120Hz High-Refresh Negotiation & Frame Budgeting
- **ADR-007**: Live Dynamic Theming & Process Death Recovery

---

## ADR-001: Application Name, Identity, and Package Structure
- **Context**: Fixed project identity specified in project brief.
- **Decision**:
  - Name: `NexusTune`
  - Application ID: `com.nexus.tune`
  - Module namespaces: `com.nexus.tune.<module>`
  - Launcher label: `NexusTune`
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

---

## ADR-002: Clean Multi-Module Architecture and Boundaries
- **Context**: Need strict compile-time architectural separation to maintain 120Hz performance, testability, and clean code hygiene.
- **Decision**: Multi-module clean architecture:
  - `:core:model`: Pure Kotlin data models (`Track`, `Album`, `Artist`, `Playlist`, `PlaybackState`).
  - `:core:common`: Shared utilities, Coroutine dispatchers, Result monads.
  - `:core:domain`: Use cases and repository contracts. Strictly zero Android dependencies (`no import android.*`).
  - `:core:data`: Repository implementations, local/remote synchronization, mappers.
  - `:core:database`: Room database, DAOs, schema migrations.
  - `:core:datastore`: Jetpack DataStore preferences (theme, audio, cache settings).
  - `:core:extractor`: `MusicSource` interface with `NewPipeMusicSource` and `FakeMusicSource`.
  - `:core:player`: Media3 `MediaSessionService`, ExoPlayer wrapper, audio effects.
  - `:core:designsystem`: M3 Expressive theme, typography, shapes, dynamic colors, spring tokens.
  - `:core:ui`: Shared composables (mini-player, error banners, skeletons).
  - `:core:testing`: Test fakes, rules, and fixtures.
  - Feature modules: `:feature:home`, `:feature:search`, `:feature:library`, `:feature:player`, `:feature:settings`.
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

---

## ADR-003: Media3 Playback Service, ResolvingDataSource & Gapless Strategy
- **Context**: Playback must be continuous, survive backgrounding, support lockscreen controls, and handle expiring YouTube stream URLs.
- **Decision**:
  - Implement `NexusPlaybackService` extending `MediaSessionService` with `foregroundServiceType="mediaPlayback"`.
  - Use Media3 `ResolvingDataSource` to resolve audio URLs on-the-fly. Cache resolved URLs with 3-hour TTL; on HTTP 403/410, auto-invalidate and re-resolve.
  - v1 implements true gapless playback via ExoPlayer playlist buffering. Dual-player crossfade is deferred to v1.1 to avoid dual-decoder memory pressure.
  - Debounce Room queue persistence by 500ms on `Dispatchers.IO` to eliminate SQLite thrashing.
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

---

## ADR-004: Offline Download Strategy (WorkManager vs FGS)
- **Context**: Android 14 (API 34) and Android 15 (API 35) impose strict runtime limits and requirements on foreground service types (`dataSync` timeouts).
- **Decision**:
  - Implement downloads using `androidx.work.WorkManager` with `OneTimeWorkRequestBuilder<DownloadTrackWorker>()`.
  - Enforce `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()`.
  - Display non-intrusive notification progress using ForegroundInfo.
  - Store encrypted/private audio in app-specific storage (`context.getExternalFilesDir("downloads")`).
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

---

## ADR-005: Synced Lyrics Integration
- **Context**: Lyrics are essential for user delight, but commercial services require authenticated keys or violate copyright.
- **Decision**:
  - Integrate `LRCLIB` (open-source synced lyrics API: `https://lrclib.net/api/get`).
  - Query by track title, artist name, and duration.
  - Completely non-blocking: if lyrics are unavailable or network times out (1.5s timeout), the player seamlessly falls back to normal art view.
- **Votes**: 4-1 (Power-User Skeptic consented with non-blocking fail-safe).
- **Status**: Accepted.

---

## ADR-006: 120Hz High-Refresh Negotiation & Frame Budgeting
- **Context**: Ensuring buttery 120Hz animations requires explicit window configuration and strict Compose recomposition boundaries.
- **Decision**:
  - Enumerate `display.supportedModes` in `MainActivity`. Select the highest available refresh rate (e.g. 120Hz) and set `window.attributes.preferredDisplayModeId`.
  - Re-register on `DisplayManager.DisplayListener` for fold/unfold or external display changes.
  - Progress ticks must never recompose siblings: encapsulate time-dependent draw operations in `drawWithCache` or lambda-based offset modifiers.
  - On API 35+, apply `View.setRequestedFrameRate` when animating.
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

---

## ADR-007: Live Dynamic Theming & Process Death Recovery
- **Context**: Customization must apply instantly without restarting the Activity.
- **Decision**:
  - `ThemeRepository` emits `StateFlow<ThemeConfig>` collected by the root `NexusTuneTheme`.
  - Supports 5 seed modes: Dynamic (Material You wallpaper), Custom Seed Color, Palette Style, AMOLED Pure Black, and Light/Dark/System.
  - Backed by Jetpack DataStore; automatically restored on cold boot and configuration change.
- **Votes**: 5-0 Unanimous.
- **Status**: Accepted.

