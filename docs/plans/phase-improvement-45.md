# Phase Improvement Loop 45: Custom User EQ Presets & Bézier Frequency Curve Engine (v5.5.0)

## Overview
NexusTune provides a 5-band hardware parametric equalizer (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz) with standard presets (`Flat`, `Bass Boost`, `Treble Boost`, etc.).
Loop 45 introduces the **Custom User EQ Presets & Bézier Frequency Curve Engine**, enabling users to:
1. Save and name unlimited custom equalizer profiles (e.g. "Sony WH-1000XM5 Headphone Tune", "Car Bluetooth Highs", "Vocal Warmth").
2. Visualize the active frequency response curve with a smooth cubic Bézier spline in Compose.
3. Automatically match slider values to the nearest matching sound profile.
4. Export and import custom EQ profiles for easy sharing and backup.

## Deliverables

### 1. `:core:model`
- `CustomEqProfile`:
  - `id: String = UUID.randomUUID().toString()`
  - `name: String`
  - `bands: List<Int>` (5 band levels in millibels, e.g. `[-300, 150, 0, 450, 600]`)
  - `createdAtTimestamp: Long = System.currentTimeMillis()`

### 2. `:core:domain:audio`
- `EqCurveCalculator`:
  - Computes normalized control points and cubic Bézier curve anchors for rendering smooth frequency response graphs on Compose Canvas.
  - Computes Euclidean distance / cosine similarity between custom band gains and standard presets to suggest the closest audio signature match.
  - Serializes and deserializes custom EQ presets to/from URL-safe or JSON strings.

### 3. `:core:datastore` & `:core:data`
- Persist custom EQ profiles in `SettingsDataStore` as JSON list.
- Add `customEqProfiles: Flow<List<CustomEqProfile>>`, `saveCustomEqProfile(profile: CustomEqProfile)`, and `deleteCustomEqProfile(id: String)`.

### 4. `:core:ui`
- `EqCurveVisualizer`:
  - High-performance Compose Canvas component drawing the smooth gradient Bézier frequency curve with interactive band nodes and decibel gridlines (-12dB to +12dB).

### 5. `:feature:settings`
- Dedicated "Custom Sound Profiles & Frequency Response" card in `SettingsScreen`:
  - Bézier curve visualizer showing real-time frequency curve.
  - "Save As Custom Preset" dialog with name input.
  - Chips for user-saved profiles with one-tap application and delete actions.
  - Auto-matched closest sound signature badge (e.g. `Signature: Bass Warmth (92% Match)`).

### 6. Test Suites & Release Qualification
- Unit tests in `EqCurveCalculatorTest` (`:core:domain`).
- `SettingsViewModelTest` custom EQ profile tests (`:feature:settings`).
- Verification via `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
- Tag `v5.5-1`.

