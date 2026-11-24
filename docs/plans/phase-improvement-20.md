# Phase Improvement 20: Spatial Audio Headphone Virtualization & 3D Stage Simulation (v3.0.0 Milestone)

## Goal
Implement hardware-accelerated **Spatial Audio Virtualization & 3D Stage Simulation** utilizing Android's `Virtualizer` DSP framework (`android.media.audiofx.Virtualizer`). This provides binaural room stage emulation across headphones and audio outputs, transforming stereo playback into an immersive acoustic space.

## Architecture & Design
1. **Core Model (`core:model`)**:
   - `enum class SpatialAudioStage`: `OFF`, `STUDIO` (250mB), `LOUNGE` (500mB), `CONCERT_HALL` (750mB), `WIDE_ARENA` (1000mB).
   - Add fields to `ThemeConfig`:
     - `val spatialAudioEnabled: Boolean = false`
     - `val spatialAudioStage: SpatialAudioStage = SpatialAudioStage.OFF`
     - `val spatialAudioStrength: Int = 500`

2. **Core Player DSP (`core:player`)**:
   - In `NexusAudioEffects`:
     - Instantiate and manage `android.media.audiofx.Virtualizer` on the active audio session.
     - Add `setSpatialAudioEnabled(enabled: Boolean)`.
     - Add `setSpatialAudioStage(stage: SpatialAudioStage)`.
     - Add `stageToStrength(stage: SpatialAudioStage): Short`.
     - Handle null/disabled/safe-lifecycle states and release on session destruction.
   - In `NexusAudioEffectsTest`:
     - Test stage mappings, enabled toggles, and safe unattached behavior.

3. **Core Datastore (`core:datastore`)**:
   - Persist `SPATIAL_AUDIO_ENABLED`, `SPATIAL_AUDIO_STAGE`, and `SPATIAL_AUDIO_STRENGTH`.

4. **Settings & UI (`feature:settings`, `app`)**:
   - In `SettingsViewModel`:
     - Expose `toggleSpatialAudio(enabled: Boolean)` and `setSpatialAudioStage(stage: SpatialAudioStage)`.
   - In `SettingsScreen`:
     - Add "Spatial Audio & 3D Stage Virtualization" card under Sound FX with toggle switch and stage mode selector chips (`Studio`, `Lounge`, `Concert Hall`, `Wide Arena`).
     - Update About card to `NexusTune v3.0.0` | `Spatial Audio & 3D Stage Virtualization`.
   - In `MainActivity`:
     - Collect `themeConfig` and synchronize `NexusAudioEffects.instance` spatial audio state.

5. **Verification**:
   - Unit tests in `NexusAudioEffectsTest` and `SettingsViewModelTest`.
   - Compile and execute `./scripts/gate.sh slice`.
   - Execute full release gate `./scripts/gate.sh final`.
   - Tag release `v3.0-1`.

