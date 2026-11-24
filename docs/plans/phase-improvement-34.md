# Phase Improvement Loop 34: Interactive Waveform Seekbar & Peak Profile Engine (v4.4.0 Milestone)

## 1. Goal
Implement a dynamic **Interactive Waveform Seekbar & Peak Profile Engine** in the full player, replacing/supplementing standard sliders with tactile audio amplitude scrubbing:
1. Deterministic Track Peak Profile Engine (`:core:ui`):
   - `WaveformProfileGenerator.generateProfile(trackId: String, barCount: Int = 48)`:
     - Generates normalized amplitude peaks (0.18f - 0.98f) per track.
     - Deterministic and fast (sub-millisecond execution, zero runtime memory leaks).
2. Canvas Waveform Audio Progress Scrubber (`:core:ui`):
   - `WaveformSeekbar`:
     - Renders vertical rounded amplitude bars across player width.
     - Differentiates played bars (`primary`) and unplayed bars (`surfaceVariant`).
     - Real-time playhead thumb indicator.
     - Handles tactile gesture scrubbing via Compose `detectTapGestures` and `detectDragGestures`.
3. SeekbarStyle Configuration & Persistence (`:core:model`, `:core:datastore`, `:feature:settings`):
   - Add `SeekbarStyle { CLASSIC_SLIDER, WAVEFORM }` to `core:model`.
   - Add `seekbarStyle: SeekbarStyle = SeekbarStyle.WAVEFORM` to `ThemeConfig`.
   - Persist in `SettingsDataStore`.
   - In `SettingsScreen`, provide a Seekbar Style selector under Appearance/Player.
4. PlayerScreen Integration (`:feature:player`):
   - In `PlayerScreen.kt`, switch between `WaveformSeekbar` and classic `Slider` based on `themeConfig.seekbarStyle` (or pass `seekbarStyle: SeekbarStyle`).
5. Testing & Verification:
   - Unit tests for `WaveformProfileGeneratorTest` verifying profile bounds and determinism.
   - Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Tag release: `v4.4-1`.

## 2. Tasks & Owners
- **Core Model**: Add `SeekbarStyle` enum and field in `ThemeConfig`.
- **Core DataStore**: Persist `SEEKBAR_STYLE` in `SettingsDataStore`.
- **Core UI**: Build `WaveformSeekbar` and `WaveformProfileGenerator`.
- **Feature Player**: Integrate into `PlayerScreen`.
- **Feature Settings**: Add `SeekbarStyle` selector chips in `SettingsScreen`.
- **Testing**: Add tests in `WaveformProfileGeneratorTest` and `SettingsViewModelTest`.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] Waveform generates distinct, deterministic amplitude bars for any track ID.
- [ ] Tapping or dragging on waveform seekbar triggers `onSeek(fraction)`.
- [ ] `SeekbarStyle` persists across app restarts.
- [ ] `./scripts/gate.sh slice` passes 100%.
- [ ] Production APK builds cleanly via `./scripts/gate.sh final`.
