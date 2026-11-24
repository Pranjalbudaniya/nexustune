# Phase Improvement Loop 66: Audiophile Transient De-Clicker & Micro-Discontinuity Suppressor Studio (v6.16.0)

## 1. Overview & Motivation
Digital streaming audio and analog vinyl rips frequently suffer from impulsive audio artifacts:
- Micro-buffer dropouts and stream packet discontinuities that create audible tick transients.
- Surface dust, electrostatic pops, and scratch clicks on vinyl and legacy cassette rips.
- Abrupt non-zero-crossing audio slices.

The **Audiophile Transient De-Clicker & Pop Suppressor Studio (`DeClickerEngine`)** detects and repairs these impulsive anomalies in real time without smearing genuine musical transients (such as drum snares, guitar picks, and high hats):
1. **Discrete 2nd-Derivative Impulsive Detector**: Measures $\Delta^2 x[n] = x[n] - 2x[n-1] + x[n-2]$ against the localized running RMS noise floor.
2. **Transient Duration Discrimination**: Validates anomaly width ($1\dots 8$ samples, $\le 0.18\text{ ms}$), distinguishing clicks from genuine musical attack envelopes.
3. **Cubic Hermite Spline Reconstruction**: Interpolates through corrupted samples with $C^1$ continuity, perfectly preserving audio waveform curvature.
4. **Interactive Studio Controls**: Configurable sensitivity, operation modes, and real-time repair metrics.

## 2. Target Deliverables & Modules

### A. Data Models (`:core:model`)
- `DeClickerMode`: `OFF`, `TRANSPARENT_STREAM`, `VINYL_RESTORATION`, `HIGH_SENSITIVITY`
- `DeClickerConfig`:
  - `isEnabled: Boolean = false`
  - `mode: DeClickerMode = DeClickerMode.TRANSPARENT_STREAM`
  - `sensitivityMultiplier: Float = 3.5f`
  - `maxClickWidthSamples: Int = 8`
- `DeClickerMetrics`:
  - `clicksRepaired: Int`
  - `peakClickReductionDb: Float`
  - `currentNoiseFloorRms: Float`
  - `statusSummary: String`
- Integration in `ThemeConfig`

### B. Core Domain Engine (`:core:domain:audio`)
- `DeClickerEngine`:
  - `detectClick(samples: FloatArray, index: Int, rmsFloor: Float, thresholdMultiplier: Float): Boolean`
  - `interpolateHermite(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float`
  - `repairSamples(samples: FloatArray, startIndex: Int, count: Int): Unit`
  - `processBuffer(buffer: FloatArray, config: DeClickerConfig): DeClickerMetrics`
  - Unit tests in `DeClickerEngineTest`

### C. Persistent Storage (`:core:datastore`)
- `SettingsDataStore`:
  - Keys: `declicker_enabled`, `declicker_mode`, `declicker_sensitivity`, `declicker_max_width`
  - Mapped inside `ThemeConfig`
  - Update method: `updateDeClickerConfig`

### D. Presentation & UI (`:feature:settings`)
- `SettingsViewModel`:
  - `setDeClickerEnabled(Boolean)`
  - `setDeClickerMode(DeClickerMode)`
  - `setDeClickerSensitivity(Float)`
  - `setDeClickerMaxWidth(Int)`
- `SettingsScreen`:
  - "Audiophile De-Clicker & Pop Suppressor" studio card in Hi-Res Audio section
  - Mode selection chips, sensitivity slider, max click width selector
  - Real-time repair stats badge
- `SettingsViewModelTest`:
  - Unit tests verifying de-clicker settings persistence

## 3. Acceptance Verification
- `./scripts/gate.sh slice` passes with 100% tests
- `./scripts/gate.sh final` builds release APK and passes all quality checks
- Git commit & release tag `v6.16-1`

