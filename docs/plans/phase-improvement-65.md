# Phase Improvement Loop 65: Audiophile Tilt Equalizer & Tonal Balance Shifter Studio (v6.15.0)

## 1. Overview & Motivation
Traditional graphic and parametric equalizers manipulate narrow individual frequency bands, which frequently causes phase smearing, comb filtering, and disjointed acoustic balance when adjusting overall track tonality. 
In mastering and high-end audiophile playback, **Tilt Equalization** (pioneered by Quad and Tonelux) rotates the entire audible audio spectrum around a pivotal center frequency (typically 1 kHz). A single continuous control simultaneously and symmetrically adjusts low and high frequencies:
- Negative tilt: Smoothly elevates bass warmth while gently rolling off treble harshness.
- Positive tilt: Smoothly elevates airy treble sparkle while gently attenuating boomy sub-bass.
- Linear-phase / zero phase-smearing biquad topology preserves micro-dynamics and pristine stereo imaging.
- Equal-energy loudness compensation ensures perceived volume remains constant during tonal rotation.

## 2. Target Deliverables & Modules

### A. Data Models (`:core:model`)
- `TiltMode`: `WARM_TILT`, `NEUTRAL_FLAT`, `BRIGHT_TILT`, `CUSTOM_TILT`
- `TiltPivotFrequency`: `HZ_630`, `HZ_1000`, `HZ_1600`
- `TiltEqualizerConfig`:
  - `enabled: Boolean = false`
  - `tiltDb: Float = 0.0f` (-6.0f to +6.0f)
  - `pivotHz: Float = 1000.0f`
  - `loudnessCompensate: Boolean = true`
- `TiltSpectralMetrics`:
  - `bassGainDb: Float` (at 100 Hz)
  - `trebleGainDb: Float` (at 10 kHz)
  - `pivotHz: Float`
  - `apparentLoudnessOffsetDb: Float`
  - `tonalBalanceDescription: String`

### B. Core Domain Engine (`:core:domain:audio`)
- `TiltEqualizerEngine`:
  - `calculateTiltGainAtFrequency(freqHz: Float, tiltDb: Float, pivotHz: Float): Float`
  - `calculateSpectralMetrics(config: TiltEqualizerConfig): TiltSpectralMetrics`
  - `calculateBiquadCoefficients(tiltDb: Float, pivotHz: Float, sampleRate: Int): TiltBiquadCoefficients`
  - `processFrame(left: Float, right: Float, config: TiltEqualizerConfig, state: TiltFilterState): Pair<Float, Float>`
  - Real-time unit tests in `TiltEqualizerEngineTest`

### C. Persistent Storage (`:core:datastore`)
- `SettingsDataStore`:
  - Keys: `tilt_eq_enabled`, `tilt_eq_db`, `tilt_eq_pivot_hz`, `tilt_eq_loudness_compensate`
  - Mapped inside `ThemeConfig`
  - Dedicated update methods: `updateTiltEqualizerConfig`

### D. Presentation & UI (`:feature:settings`)
- `SettingsViewModel`:
  - `setTiltEqEnabled(enabled: Boolean)`
  - `setTiltEqDb(tiltDb: Float)`
  - `setTiltEqPivotHz(pivotHz: Float)`
  - `setTiltEqLoudnessCompensate(compensate: Boolean)`
- `SettingsScreen`:
  - "Audiophile Tilt EQ & Tonal Balancer" studio card in soundstage settings section
  - Live Warm/Bright slider (-6.0 dB to +6.0 dB) with zero-snap
  - Pivot frequency selection chips (630 Hz, 1.0 kHz, 1.6 kHz)
  - Loudness compensation switch
  - Live tonal balance descriptor badge
- `SettingsViewModelTest`:
  - Unit tests verifying tilt EQ settings persistence and state flow updates

## 3. Acceptance Verification
- `./scripts/gate.sh slice` passes with 100% tests
- `./scripts/gate.sh final` builds release APK and passes all quality checks
- Git commit & release tag `v6.15-1`

