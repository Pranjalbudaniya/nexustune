# Phase Improvement Loop 35: Vocal Pitch Shift & Semitone Transposition DSP Engine

## 1. Overview & Objectives
Musicians, vocalists, karaoke singers, and audio transcribers require the ability to transpose audio in discrete musical semitones (-6 to +6 st) and fine pitch adjustments independently from playback speed. Loop 35 introduces:
1. `PitchTransposer`: Pure mathematical semitone $\leftrightarrow$ pitch ratio converter ($2^{\Delta s / 12}$) with semitone and ratio formatting.
2. `PlayerController` & `PlayerViewModel` integration: Exposing reactive semitone and pitch ratio flows, with `transposeUp()`, `transposeDown()`, `setSemitones(st)`, and `resetPitch()`.
3. Auto-reset configuration: DataStore preference `resetPitchOnTrackChange` (default true) ensuring tracks reset to original pitch on change unless locked.
4. Interactive Key Transposition Console in `PlayerScreen`: Stepper controls (`-`, `+`), quick-jump chips (`-2`, `-1`, `0`, `+1`, `+2`), and reset action.
5. Comprehensive unit test suites across `:core:player`, `:feature:player`, and `:feature:settings`.

## 2. Technical Scope & Architecture Boundaries
- **`:core:player`**:
  - `PitchTransposer.kt`: Math engine for $2^{s/12}$ conversions, formatting, and clamping [-6..+6] semitones.
  - `PlayerController.kt`: Enhanced pitch control with semitone synchronization and optional track-change reset.
- **`:core:model`**:
  - `ThemeConfig`: Add `val resetPitchOnTrackChange: Boolean = true`.
- **`:core:datastore`**:
  - `SettingsDataStore`: Add `RESET_PITCH_ON_TRACK_CHANGE` key and preference flow.
- **`:feature:player`**:
  - `PlayerViewModel`: Reactive pitch state, `transposeUp()`, `transposeDown()`, `setSemitones()`, `resetPitch()`.
  - `PlayerScreen`: Dedicated Key Transpose UI console with chips and stepper.
- **`:feature:settings`**:
  - `SettingsViewModel` & `SettingsScreen`: Toggle for "Reset Pitch on Track Change".

## 3. Verification Criteria
- [ ] `./scripts/gate.sh slice` passes 100% of unit tests.
- [ ] `./scripts/gate.sh final` passes with release APK build.
- [ ] Git commit and tag `v4.5-1`.
