# Phase Plan: Improvement Loop 18 (v2.8.0)

## Objective
Implement **Parametric EQ Engine & Auto-Genre Profile Adaptation**:
1. Expand `EqualizerPreset` with `CLASSICAL`, `JAZZ`, `HIP_HOP`, `ACOUSTIC`, and `CUSTOM` DSP profiles.
2. Implement `GenreAdaptiveEqManager` in `core:player` to detect genre keywords and automatically adjust hardware audio equalizer DSP curves.
3. Integrate auto-genre preference toggle into `ThemeConfig` and `SettingsScreen`.

## Architecture & Modules
- `core:model`:
  - Extend `EqualizerPreset` with `CLASSICAL`, `JAZZ`, `HIP_HOP`, `ACOUSTIC`, `CUSTOM`.
  - Add `autoGenreEqEnabled: Boolean = false` to `ThemeConfig`.
- `core:player`:
  - Enhance `NexusAudioEffects` with DSP band curves for all presets and `applyCustomBands`.
  - Create `GenreAdaptiveEqManager` with keyword-based genre heuristic and auto-preset application.
- `app`:
  - Instantiate `genreAdaptiveEqManager` in `NexusTuneApplication`.
  - Wire reactive track observation in `MainActivity` when `autoGenreEqEnabled` is on.
- `feature:settings`:
  - Add `toggleAutoGenreEq(enabled: Boolean)` to `SettingsViewModel`.
  - Update `SettingsScreen` with the auto-genre toggle switch and new preset chips.
  - Update About banner to `NexusTune v2.8.0` | `Parametric EQ & Auto-Genre DSP Profiles`.

## Tasks & Owners
1. **Task 18.1: Core Models & Equalizer DSP Expansion**
   - Update `EqualizerPreset` and `ThemeConfig`.
   - Update `NexusAudioEffects` with extended presets and custom band levels.
2. **Task 18.2: GenreAdaptiveEqManager**
   - Create `GenreAdaptiveEqManager` in `core:player:effects`.
   - Wire into `NexusTuneApplication` and `MainActivity`.
3. **Task 18.3: Settings UI & About Update**
   - Add auto-genre toggle in `SettingsViewModel` and `SettingsScreen`.
   - Update About banner in `SettingsScreen`.
4. **Task 18.4: Unit Tests & Release Qualification**
   - Unit tests for `GenreAdaptiveEqManager` and `NexusAudioEffects`.
   - Unit tests in `SettingsViewModelTest`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` and tag `v2.8-1`.

