# Phase Execution Plan: Improvement Loop 8 (v1.8.0)

## Objective
Implement **Adaptive Playback Pitch & Speed Controller** in NexusTune, enabling real-time playback speed adjustments (0.5x to 2.0x) and pitch control via Media3's native `PlaybackParameters` pipeline.

## Scope & Components
1. **Core Models & Player Engine**:
   - Update `PlaybackState` in `core:model` to include `playbackSpeed: Float = 1.0f` and `playbackPitch: Float = 1.0f`.
   - In `PlayerController` (`core:player`):
     - Implement `setPlaybackSpeed(speed: Float)` and `setPlaybackPitch(pitch: Float)` using `controller?.setPlaybackParameters(PlaybackParameters(speed, pitch))`.
     - Update `updateState()` to sync `playbackSpeed` and `playbackPitch` from active `PlaybackParameters`.
2. **Player Feature & UI**:
   - In `PlayerViewModel` (`feature:player`):
     - Add `setPlaybackSpeed(speed: Float)` and `setPlaybackPitch(pitch: Float)`.
   - In `PlayerScreen` (`feature:player`):
     - Add speed indicator and selection chip group (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x) accessible via player controls or modal sheet.
3. **Unit Tests & Verification**:
   - Unit tests in `feature:player:testDebugUnitTest`.
   - Verify all unit tests via `./scripts/gate.sh slice`.
   - Macrobenchmark on attached device via `./scripts/gate.sh perf`.
   - Full release build via `./scripts/gate.sh final`.
   - Tag `v1.8-1`.

## Acceptance Criteria
- [ ] `PlaybackState` exposes `playbackSpeed` and `playbackPitch`.
- [ ] `PlayerController` modifies ExoPlayer `PlaybackParameters` cleanly.
- [ ] `PlayerViewModel` and `PlayerScreen` support speed changes with real-time feedback.
- [ ] 100% test pass rate across all modules.
- [ ] `./scripts/gate.sh slice` passes.
- [ ] `./scripts/gate.sh perf` passes on device V2518.
- [ ] `./scripts/gate.sh final` passes.

