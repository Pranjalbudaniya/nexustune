# NexusTune Phase Plan: Improvement Loop 12 (v2.2.0)
## Audio Visualizer & Waveform Animation Engine

### 1. Objectives & Scope
- **Feature**: Real-Time Audio Visualizer & Waveform Animation Engine.
- **Components**:
  1. `core:model`: `VisualizerMode` (`OFF`, `SPECTRUM_BARS`, `CIRCULAR_WAVE`, `GLOW_PULSE`) and `visualizerMode` field in `ThemeConfig`.
  2. `core:player`: `VisualizerManager` with Android `Visualizer` DSP integration and smooth frequency normalization (16-32 bins) with robust fallback for OEM device compatibility.
  3. `feature:player` / `core:ui`: `AudioVisualizer` Composable rendering real-time spectrum bars, circular audio waves, and ambient glow pulses in `PlayerScreen`.
  4. `feature:settings`: Visualizer mode configuration chips under "Playback & Interface".
- **Target Release**: `v2.2.0` (Git tag: `v2.2-1`).

### 2. Implementation Tasks & Owners
- **Task 12.1 (Domain & Model)**:
  - Add `VisualizerMode` enum to `core:model/src/main/kotlin/com/nexus/tune/core/model/Models.kt`.
  - Add `visualizerMode` to `ThemeConfig`.
- **Task 12.2 (DSP & Player Integration)**:
  - Create `VisualizerManager` in `core:player/src/main/kotlin/com/nexus/tune/core/player/visualizer/VisualizerManager.kt`.
  - Wire audio session lifecycle and playback state in `PlayerController`.
  - Implement smooth exponential moving average (EMA) frequency bin smoothing.
- **Task 12.3 (UI & Presentation)**:
  - Implement `AudioVisualizer` Composable in `core:ui/src/main/kotlin/com/nexus/tune/core/ui/AudioVisualizer.kt`.
  - Integrate visualizer modes into `PlayerScreen` (above/below transport controls or around artwork).
  - Add Visualizer Mode selector chips in `SettingsScreen.kt`.
- **Task 12.4 (Verification & Testing)**:
  - Unit tests for `VisualizerManager` and `ThemeConfig` updates.
  - Verification on physical device `Vivo V2518` (Android 16 @ 120Hz).
  - Gate verification (`slice`, `perf`, `final`).

### 3. Acceptance Criteria
- [ ] Visualizer accurately changes animation based on playback state (active animation during playing, smooth decay to idle on pause).
- [ ] All 3 visualizer modes (`SPECTRUM_BARS`, `CIRCULAR_WAVE`, `GLOW_PULSE`) render at 120Hz without dropped frames.
- [ ] Safe fallback handling ensures zero crashes if native `Visualizer` is unsupported on target audio hardware.
- [ ] 100% test pass rate across all modules.
