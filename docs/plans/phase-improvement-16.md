# Phase Plan: Improvement Loop 16 (v2.6.0)

## Objective
Implement **Gapless Playback Buffer Pre-Loading & Audio Pipeline Warm-up**:
Pre-resolve upcoming stream URLs and prime Media3 ExoPlayer's playlist buffer 5 seconds before the current track finishes to eliminate playback transition latency and achieve zero-millisecond gapless playback across track boundaries.

## Architecture & Modules
- `core:model`:
  - `gaplessPreloadEnabled: Boolean = true` in `ThemeConfig`
  - `gaplessPreloadSeconds: Int = 5` in `ThemeConfig`
- `core:player`:
  - `PlayerController`:
    - `preloadNextMediaItem(track: Track, streamUrl: String)`: Appends upcoming track MediaItem to ExoPlayer playlist without interrupting current playback.
    - `clearUpcomingPreload()`: Clears primed media items on queue alteration or explicit track play.
    - `onMediaItemTransition`: Auto-updates active playback track state when player naturally transitions to preloaded item.
  - `GaplessPreloadManager`:
    - Inspects playback progress (`durationMs - positionMs <= thresholdMs`).
    - Discovers upcoming track from `QueueRepository`.
    - Proactively calls `ResolveStreamUseCase`.
    - Primes `PlayerController` with resolved stream.
- `app`:
  - Instantiate `gaplessPreloadManager` in `NexusTuneApplication`.
  - Wire reactive loop in `MainActivity` with `playbackState` and `themeConfig`.
- `feature:settings`:
  - `SettingsViewModel`:
    - `toggleGaplessPreload(enabled: Boolean)`
    - `setGaplessPreloadSeconds(seconds: Int)`
  - `SettingsScreen`:
    - UI card under Audio Optimization with switch and threshold selection chips (3s, 5s, 10s).
    - Update About banner to `NexusTune v2.6.0` | `Gapless Buffer Pipeline Warm-Up`.

## Tasks & Owners
1. **Task 16.1: Core Models & DataStore**
   - Update `ThemeConfig` with `gaplessPreloadEnabled` and `gaplessPreloadSeconds`.
2. **Task 16.2: Core Player & Preload Engine**
   - Update `PlayerController` with `preloadNextMediaItem` and playlist auto-transition handling.
   - Implement `GaplessPreloadManager` in `core:player`.
3. **Task 16.3: Presentation & Wiring**
   - Wire `GaplessPreloadManager` into `NexusTuneApplication` and `MainActivity`.
   - Update `SettingsViewModel` and `SettingsScreen`.
4. **Task 16.4: Unit Tests & Hardware Gate Verification**
   - Unit tests for `GaplessPreloadManager` and `PlayerController`.
   - Unit tests for `SettingsViewModel`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh perf` on attached device Vivo V2518 (`10BF7Q150G000LE`).
   - Run `./scripts/gate.sh final` and tag `v2.6-1`.

