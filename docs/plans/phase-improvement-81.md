# Phase Improvement Plan - Loop 81 (v6.31.0)
## Audiophile Psychoacoustic Soundstage Elevation & Height Virtualizer Studio

### 1. Context & Motivation
Standard stereo playback over headphones localizes sound strictly along a flat lateral line through the ears, creating an unnatural "inside-the-head" sensation. The human brain decodes vertical elevation cues using outer ear (pinna) spectral notches around 7.5 - 9.5 kHz, overhead acoustic air boosts (>11 kHz), and subtle micro-reflections from ceilings.
Loop 81 implements a real-time pinna HRTF elevation engine with an early ceiling reflection ring buffer, elevating the perceived soundstage from chin/chest level up to natural eye and forehead heights.

### 2. Architecture & Design
- **`:core:model`**:
  - `ElevationAngle`: `EYE_LEVEL_0`, `STAGE_LIFT_15`, `BALCONY_30`, `CATHEDRAL_DOME_45`
  - `SoundstageElevationPreset`: `OFF`, `VOCAL_FOREHEAD_LIFT`, `CONCERT_HALL_BALCONY`, `ATMOS_HEIGHT_GLOW`, `ACOUSTIC_STAGE_LIFT`, `CUSTOM`
  - `SoundstageElevationConfig`:
    - `isEnabled: Boolean = false`
    - `preset: SoundstageElevationPreset = SoundstageElevationPreset.OFF`
    - `elevationAngle: ElevationAngle = ElevationAngle.STAGE_LIFT_15`
    - `elevationIntensity: Float = 0.50f` (0.0f .. 1.0f)
    - `ceilingReflectionMs: Float = 4.2f` (1.5f .. 8.0f)
    - `pinnaNotchDepthDb: Float = -4.5f` (-1.5f .. -9.0f)
    - `diffuseCeilingSpread: Boolean = true`
  - `SoundstageElevationMetrics`:
    - `virtualElevationDegrees: Float`
    - `pinnaNotchFrequencyHz: Float`
    - `ceilingDelaySamples: Int`
    - `spatialHeightScore: Int`
    - `elevationRating: String`
  - Integrate `soundstageElevationConfig` into `ThemeConfig`.

- **`:core:domain:audio`**:
  - `SoundstageElevationEngine`:
    - Pinna spectral notch biquad targeting 7.8 kHz .. 9.2 kHz modulated by elevation angle
    - High-shelf ceiling air boost at 12 kHz
    - Micro-delay reflection buffer (1.5 ms to 8.0 ms) with cross-channel high-frequency absorption
    - Real-time elevation metrics and static `calculateMetrics` helper

- **`:core:player`**:
  - `NexusAudioEffects`:
    - Hardware equalizer integration (`applySoundstageElevationOffset`, `clearSoundstageElevationOffset`, `getSoundstageElevationOffset`) targeting Band 4 (air)

- **`:core:datastore`**:
  - Preferences keys for soundstage elevation
  - Flow updates in `themeConfig` and `updateThemeConfig`

- **`:feature:settings`**:
  - `SettingsViewModel`: Methods for soundstage elevation
  - `SettingsScreen`: Interactive "Psychoacoustic Soundstage Elevation & Height Studio" Card in its own `item { ... }` block with angle selector chips, intensity slider, ceiling reflection slider, pinna depth slider, diffuse spread switch, and live 3D elevation HUD

- **Tests & Verification**:
  - Domain tests: `SoundstageElevationEngineTest`
  - Player tests: `NexusAudioEffectsTest`
  - ViewModel tests: `SettingsViewModelTest`
  - Gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`

