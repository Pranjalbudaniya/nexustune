# NexusTune Phase Plan: Improvement Loop 14 (v2.4.0)
## Smart Dynamic Queue & Auto-Play Infinite Mix

### 1. Objectives & Scope
- **Feature**: Smart Dynamic Queue & Auto-Play Infinite Mix.
- **Components**:
  1. `core:model`: `infiniteAutoPlayEnabled: Boolean = true` in `ThemeConfig`.
  2. `core:domain`: `GetAutoPlayRecommendationsUseCase` querying related tracks and artist context for endless discovery.
  3. `core:player`: `AutoPlayQueueManager` observing queue depth and proactively appending recommended tracks as the queue reaches exhaustion.
  4. `feature:settings`: "Infinite Auto-Play" toggle under Playback & Audio Optimization.
- **Target Release**: `v2.4.0` (Git tag: `v2.4-1`).

### 2. Implementation Tasks & Owners
- **Task 14.1 (Domain & Model)**:
  - Add `infiniteAutoPlayEnabled` to `ThemeConfig` in `core:model`.
  - Implement `GetAutoPlayRecommendationsUseCase` in `core:domain`.
- **Task 14.2 (Player & Queue Integration)**:
  - Implement `AutoPlayQueueManager` in `core:player` with proactive replenishment when queue length <= 2.
  - Wire to `PlayerController` and `ManageQueueUseCase`.
- **Task 14.3 (UI & Presentation)**:
  - Add "Infinite Auto-Play" switch in `SettingsScreen.kt` under Audio Optimization.
- **Task 14.4 (Verification & Testing)**:
  - Unit tests for `GetAutoPlayRecommendationsUseCase` and `AutoPlayQueueManager`.
  - Gate verification (`slice`, `perf`, `final`).

### 3. Acceptance Criteria
- [ ] Active queue never runs dry when Infinite Auto-Play is enabled.
- [ ] Duplicate tracks already in history or current queue are pruned from recommendations.
- [ ] Disabling Infinite Auto-Play respects strict queue end-of-playback.
- [ ] 100% test pass rate across all modules.

