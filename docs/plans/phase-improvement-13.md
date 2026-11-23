# NexusTune Phase Plan: Improvement Loop 13 (v2.3.0)
## Sleep Timer Gentle Audio Fade-Out & Dynamic Attenuation

### 1. Objectives & Scope
- **Feature**: Sleep Timer Gentle Audio Fade-Out & Dynamic Attenuation.
- **Components**:
  1. `core:model`: `sleepTimerFadeSeconds` (0s, 10s, 30s, 60s) in `ThemeConfig`, plus `isFadingOut` and `fadeVolumeMultiplier` in `SleepTimerState`.
  2. `core:player`: `SleepTimerManager` progressive attenuation engine scaling master volume over the final countdown seconds using an exponential perceptual audio curve.
  3. `feature:settings`: Sleep timer fade duration chips (Instant, 10s, 30s, 60s) in `SettingsScreen`.
- **Target Release**: `v2.3.0` (Git tag: `v2.3-1`).

### 2. Implementation Tasks & Owners
- **Task 13.1 (Domain & Model)**:
  - Add `sleepTimerFadeSeconds` to `ThemeConfig` in `core:model`.
  - Add `isFadingOut: Boolean` and `fadeVolumeMultiplier: Float` to `SleepTimerState`.
- **Task 13.2 (Player Engine)**:
  - Enhance `SleepTimerManager` to dynamically calculate and apply `fadeVolumeMultiplier` during the final countdown phase.
  - Wire callback / state observation in `PlayerController` to scale volume smoothly down to 0 at timer expiry.
- **Task 13.3 (UI & Presentation)**:
  - Add Fade-Out Duration chips (Off, 10s, 30s, 60s) under Sleep Timer in `SettingsScreen`.
  - Add visual "Fading Out..." indicator to the active Sleep Timer card.
- **Task 13.4 (Verification & Testing)**:
  - Unit tests in `SleepTimerManagerTest` validating fade-out curve, cancellation restore, and end-of-track pause.
  - Gate verification (`slice`, `perf`, `final`).

### 3. Acceptance Criteria
- [ ] During the final countdown seconds, audio volume scales down smoothly without clicks or pops.
- [ ] Cancelling an active sleep timer during fade-out restores the user's previous volume immediately.
- [ ] End-of-track timer mode cleanly pauses upon track completion.
- [ ] 100% test pass rate across all modules.

