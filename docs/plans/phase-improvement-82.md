# Phase Improvement Plan - Loop 82 (v6.32.0)
## Audiophile Dynamic Sub-Bass Phase Alignment & Mono-Sum Studio

### 1. Context & Motivation
Out-of-phase low-frequency components (20-140 Hz) in stereo recordings cause destructive acoustic interference, dynamic amplifier headroom waste, and weak bass punch on headphones, subwoofers, and mobile DACs.
Loop 82 introduces an Elliptical Equalizer and Dynamic Phase Alignment engine that locks sub-bass into coherent mono below a configurable crossover (60-150 Hz) while preserving 100% of stereo width in the midrange and treble, restoring visceral bass slam and eliminating low-end phase smear.

### 2. Architecture & Design
- **`:core:model`**:
  - `MonoBassCrossover`: `HZ_60`, `HZ_80`, `HZ_100`, `HZ_120`, `HZ_150`
  - `MonoBassFilterSlope`: `SLOPE_12DB_OCT` (2nd Order), `SLOPE_24DB_OCT` (4th Order Linkwitz-Riley)
  - `MonoBassAlignPreset`: `OFF`, `SUBWOOFER_MONO_LOCK_80HZ`, `CLUB_PA_TIGHT_100HZ`, `VINYL_ELLIPTICAL_60HZ`, `HEADPHONE_PUNCH_120HZ`, `CUSTOM`
  - `MonoBassAlignConfig`:
    - `isEnabled: Boolean = false`
    - `preset: MonoBassAlignPreset = MonoBassAlignPreset.OFF`
    - `crossover: MonoBassCrossover = MonoBassCrossover.HZ_80`
    - `slope: MonoBassFilterSlope = MonoBassFilterSlope.SLOPE_24DB_OCT`
    - `autoPhaseAlign: Boolean = true`
    - `subPunchGainDb: Float = 1.5f` (0.0f .. 4.0f)
    - `sideSoloAudition: Boolean = false`
  - `MonoBassAlignMetrics`:
    - `subBassCorrelation: Float` (-1.0f .. 1.0f)
    - `restoredEnergyDb: Float`
    - `phaseAlignmentDegrees: Float`
    - `crossoverFrequencyHz: Float`
    - `coherenceRating: String`
  - Integrate `monoBassAlignConfig` into `ThemeConfig`.

- **`:core:domain:audio`**:
  - `MonoBassAlignEngine`:
    - Mid/Side matrix decomposition: $M = (L+R)/\sqrt{2}$, $S = (L-R)/\sqrt{2}$
    - High-pass filtering on the Side channel ($S$) using cascaded 2nd/4th order Linkwitz-Riley biquads
    - Low-frequency inter-channel phase alignment cross-correlation and fractional-delay compensation
    - Low-end punch makeup gain on the Mid channel ($M$)
    - Re-matrixing back to stereo $L, R$
    - Static `calculateMetrics` helper

- **`:core:player`**:
  - `NexusAudioEffects`:
    - Hardware equalizer integration (`applyMonoBassPunchOffset`, `clearMonoBassPunchOffset`, `getMonoBassPunchOffset`) targeting Bands 0 & 1

- **`:core:datastore`**:
  - Preferences keys for sub-bass alignment
  - Flow updates in `themeConfig` and `updateThemeConfig`

- **`:feature:settings`**:
  - `SettingsViewModel`: Methods for sub-bass mono alignment
  - `SettingsScreen`: Interactive "Dynamic Sub-Bass Phase Alignment & Mono Studio" Card in its own `item { ... }` block with crossover chips, slope chips, preset chips, punch boost slider, phase align switch, and real-time phase coherence HUD

- **Tests & Verification**:
  - Domain tests: `MonoBassAlignEngineTest`
  - Player tests: `NexusAudioEffectsTest`
  - ViewModel tests: `SettingsViewModelTest`
  - Gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`

