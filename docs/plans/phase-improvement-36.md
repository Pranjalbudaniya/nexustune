# Phase Improvement Loop 36: Smart Artist-Spaced Diversity Shuffle & Repeat Mode Orchestration Engine

## 1. Overview & Objectives
Standard music player shuffle often suffers from consecutive artist clustering (e.g. playing 3 tracks by the same artist back-to-back from a large playlist). Loop 36 introduces:
1. `SmartShuffleEngine`: An intelligent spacing algorithm in `:core:domain:queue` that spaces tracks by artist/author across the playback queue while preserving the active track at index 0.
2. `PlayerController` & Media3 Repeat / Shuffle Orchestration:
   - `onShuffleModeEnabledChanged` and `onRepeatModeChanged` Media3 callbacks reflected in `PlaybackState` (`isShuffle`, `isRepeatOne`, `isRepeatAll`).
   - `toggleShuffle()`, `cycleRepeatMode()` (OFF -> ALL -> ONE -> OFF), `setRepeatMode()`.
3. Tactile Controls in `PlayerScreen`:
   - Interactive Shuffle button with active tinting.
   - Interactive Repeat button cycling between `Repeat` (off), `Repeat` (all), and `RepeatOne` (single track).
   - Interactive Skip Previous and Skip Next controls.
4. Settings Preference:
   - `smartShuffleEnabled` preference in `ThemeConfig` and `SettingsDataStore`, configurable via `SettingsScreen`.
5. Comprehensive unit test suites in `:core:domain`, `:feature:player`, and `:feature:settings`.

## 2. Technical Scope & Architecture Boundaries
- **`:core:domain`**:
  - `SmartShuffleEngine.kt`: Spaced shuffle distribution algorithm.
  - `ManageQueueUseCase.kt`: `shuffleQueue(smartDiversity: Boolean)` method.
- **`:core:model`**:
  - `ThemeConfig`: Add `val smartShuffleEnabled: Boolean = true`.
- **`:core:datastore`**:
  - `SettingsDataStore`: Add `SMART_SHUFFLE_ENABLED` preference key.
- **`:core:player`**:
  - `PlayerController.kt`: State callbacks and control methods for shuffle and repeat.
- **`:feature:player`**:
  - `PlayerScreen.kt`: Dynamic shuffle/repeat icons, active badges, and skip triggers.
  - `PlayerViewModel.kt`: Shuffle and repeat state flow bridges.
- **`:feature:settings`**:
  - `SettingsViewModel` & `SettingsScreen`: "Smart Artist-Spaced Shuffle" toggle.

## 3. Verification Criteria
- [ ] `./scripts/gate.sh slice` passes 100% of unit tests.
- [ ] `./scripts/gate.sh final` passes with release APK build.
- [ ] Tag build `v4.6-1`.

