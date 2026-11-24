# Phase Improvement Loop 33: A-B Repeat Segment & Audio Practice Engine (v4.3.0 Milestone)

## 1. Goal
Implement an accurate **A-B Repeat Segment & Audio Practice Loop Engine** for music learning, transcription, dancing, and focused study:
1. Core Model & Boundary State (`:core:model`):
   - Add `ABLoopState(isEnabled: Boolean, startMs: Long?, endMs: Long?, loopCount: Int)` to `core:model`.
2. Playback Position Monitoring & Seamless Loop Wrap (`:core:player`):
   - Implement `ABLoopManager` in `:core:player`:
     - `setPointA(posMs: Long? = null)`: Sets start boundary marker A.
     - `setPointB(posMs: Long? = null)`: Sets end boundary marker B.
     - Automatically validates `pointA < pointB`.
     - `toggleLoop(enabled: Boolean? = null)`: Enables/disables segment looping.
     - `clearLoop()`: Resets markers and loop counts.
     - Position monitoring loop: Detects when `positionMs >= pointB` and instantly seeks back to `pointA`, incrementing `loopCount`.
3. Feature UI Integration (`:feature:player`):
   - Expose A-B looping methods in `PlayerViewModel`.
   - Render tactile A-B Loop Controls in `PlayerScreen` (below scrubber):
     - Point A chip indicator with formatted timestamp (`A: 01:15`).
     - Point B chip indicator with formatted timestamp (`B: 01:45`).
     - Active loop badge showing real-time iteration counter (`Loop #4`).
     - Reset / Clear action.
4. Testing & Verification:
   - Comprehensive unit test suite `ABLoopManagerTest`:
     - Test setting point A and point B.
     - Test point validation (B > A).
     - Test automatic wrap-around seek and counter increment.
     - Test loop disable/enable and clearing.
   - Unit tests in `PlayerViewModelTest`.
   - Full gate qualification via `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Tag release: `v4.3-1`.

## 2. Tasks & Owners
- **Core Model**: Add `ABLoopState` model.
- **Core Player**: Implement `ABLoopManager` with playback state collection and automatic seeking.
- **Feature Player**: Wire into `PlayerViewModel` and add A-B loop console in `PlayerScreen`.
- **Testing**: Add `ABLoopManagerTest` and update `PlayerViewModelTest`.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] Setting Point A and Point B correctly establishes loop boundaries.
- [ ] When current position reaches or exceeds Point B, playback seeks back to Point A.
- [ ] Real-time loop count increments upon each completed iteration.
- [ ] Clearing the loop resets all markers and stops automated seeking.
- [ ] `./scripts/gate.sh slice` passes 100%.
- [ ] Production APK builds cleanly via `./scripts/gate.sh final`.

