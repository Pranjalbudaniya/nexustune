# Phase Improvement Plan - Loop 51 (v6.1.0)
## Audio Visualizer Suite Expansion: Stereo VU Meter, Liquid Wave & Color Mesh Palettes

### 1. Context & Motivation
Audio visualizers enhance musical engagement by transforming audio frequencies into dynamic visual rhythms. NexusTune v6.1.0 expands the visualizer suite by adding professional twin Stereo VU meters, multi-layer sinusoidal Liquid Wave curves, and customizable Color Mesh palettes (`Cyberpunk Neon`, `Sunset Ember`, `Aurora Borealis`, `Electric Violet`, `Golden Spark`).

### 2. Architecture & Modules
- **Core Models** (`:core:model`):
  - `VisualizerMode`: Extended with `STEREO_VU` and `LIQUID_WAVE`.
  - `VisualizerPalette`: `DYNAMIC`, `CYBERPUNK`, `SUNSET_EMBER`, `AURORA`, `ELECTRIC_VIOLET`, `GOLDEN_CHAMPAGNE`.
  - `ThemeConfig`: Added `visualizerPalette: VisualizerPalette = VisualizerPalette.DYNAMIC`.
- **Presentation & Canvas Rendering** (`:core:ui`):
  - `StereoVuVisualizer`: Calibrated twin Left/Right peak VU meters with Green (-20dB to -6dB), Yellow (-6dB to -2dB), and Red (0dB peak) segments with needle falloff animation.
  - `LiquidWaveVisualizer`: Multi-layer fluid sinusoidal curves reacting dynamically to low, mid, and high FFT magnitudes.
  - `AudioVisualizerView`: Palette color resolver mapping selected `VisualizerPalette` hex values or falling back to Material 3 dynamic color scheme.
- **Persistence Layer** (`:core:datastore`):
  - `VISUALIZER_PALETTE` preference key in `SettingsDataStore`.
- **Settings & Player Integration** (`:feature:settings`, `:feature:player`, `:app`):
  - `SettingsViewModel.setVisualizerPalette()`.
  - Live visualizer style and color palette selector chips in `SettingsScreen`.
  - Mapped to `PlayerScreen` and `MainActivity`.
- **Verification & Gates**:
  - Unit tests in `SettingsViewModelTest`.
  - Quality gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
  - Release tag: `v6.1-1`.

