# Phase Execution Plan: Improvement Loop 11 (v2.1.0)

## Objective
Implement **Per-Track Loudness Analysis & Dynamic Gain Level Normalization** in NexusTune, storing estimated track loudness LUFS / gain offset and automatically tuning playback output gain to ensure broadcast-quality loudness consistency across varied YouTube stream sources.

## Scope & Components
1. **Model & Database**:
   - Add `loudnessGainDb: Float = 0.0f` to `Track` in `core:model`.
   - Add `loudnessGainDb: Float = 0.0f` to `TrackEntity` in `core:database`.
   - Update mapping functions `Track.toEntity()` and `TrackEntity.toModel()`.
2. **Loudness Normalizer Engine**:
   - In `core:player`:
     - Extend `NexusAudioEffects` / `PlayerController` to accept per-track loudness gain offsets.
     - Convert `loudnessGainDb` into millibels (`gainmB = (gainDb * 100).toInt()`) and adjust `LoudnessEnhancer` target gain smoothly during track transitions.
3. **Settings & UI Integration**:
   - In `SettingsScreen` (`feature:settings`):
     - Add Per-Track Loudness Auto-Calibration toggle under Audio Optimization.
     - Update version display to `NexusTune v2.1.0`.
4. **Unit Tests & Quality Gates**:
   - Unit tests in `core:player:testDebugUnitTest`.
   - Unit tests in `core:data:testDebugUnitTest`.
   - `./scripts/gate.sh slice`
   - `./scripts/gate.sh perf` on attached device
   - `./scripts/gate.sh final` release verification
   - Tag `v2.1-1`

## Acceptance Criteria
- [ ] `Track` and `TrackEntity` include `loudnessGainDb`.
- [ ] `NexusAudioEffects` dynamically scales target gain based on track loudness metadata.
- [ ] 100% test pass rate across all modules.
- [ ] `./scripts/gate.sh slice` passes.
- [ ] `./scripts/gate.sh perf` passes on device.
- [ ] `./scripts/gate.sh final` passes.

