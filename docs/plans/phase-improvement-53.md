# Phase Improvement Plan - Loop 53 (v6.3.0)
## Smart Offline Cache Auto-Stash & Predictive Storage Budget Optimization Engine (`AutoStashEngine`)

### 1. Context & Motivation
Seamless offline playback is vital for music listeners during commutes, flights, and poor network connectivity. NexusTune v6.3.0 introduces the **Smart Offline Cache Auto-Stash & Predictive Storage Budget Optimization Engine (`AutoStashEngine`)**. This engine predicts tracks the user wants offline by scoring favorites, high-rotation tracks, and recent discoveries, allocating them into a user-configured storage quota (500MB to 5GB) while preserving device storage headroom and prioritizing unmetered Wi-Fi.

### 2. Architecture & Modules
- **Core Domain Models** (`:core:model`):
  - `AutoStashStrategy`: `BALANCED_HYBRID`, `FAVORITES_PRIORITY`, `HEAVY_ROTATION`, `OFFLINE_ESSENTIALS`.
  - `AutoStashNetworkPolicy`: `WIFI_ONLY`, `ANY_NETWORK`.
  - `AutoStashStatus`: `PENDING`, `STASHED`, `EVICTED`, `SKIPPED_BUDGET`.
  - `AutoStashCandidate`: `Track`, `playCount`, `isLiked`, `estimatedSizeBytes`, `priorityScore`, `reasonTag`.
  - `AutoStashConfig`: `enabled: Boolean`, `storageBudgetMb: Int`, `strategy: AutoStashStrategy`, `networkPolicy: AutoStashNetworkPolicy`, `preserveStoragePercent: Int`.
  - `AutoStashReport`: `stashedCount: Int`, `totalUsedBytes: Long`, `budgetBytes: Long`, `estimatedOfflineHours: Float`, `stashedTracks: List<Track>`, `evictionTrackIds: Set<String>`.
- **Pure Kotlin Domain Engine** (`:core:domain:offline`):
  - `AutoStashEvaluator`:
    - Priority scoring combining play frequency, recency, and favorite status based on `AutoStashStrategy`.
    - Knapsack byte budget packing: selects highest-scoring tracks up to `budgetBytes` while enforcing minimum free storage headroom.
    - Eviction engine: calculates tracks previously cached that have fallen out of rotation and should be pruned.
    - Formatters for human-readable bytes (`MB`, `GB`) and estimated offline playback hours.
- **Persistence Layer** (`:core:datastore`):
  - Preference keys: `AUTO_STASH_ENABLED`, `AUTO_STASH_BUDGET_MB`, `AUTO_STASH_STRATEGY`, `AUTO_STASH_WIFI_ONLY`.
  - Stored in `ThemeConfig` / `SettingsDataStore`.
- **Presentation Layer** (`:feature:settings`):
  - `SettingsViewModel`: State flows for `autoStashConfig` and methods `setAutoStashEnabled()`, `setAutoStashBudgetMb()`, `setAutoStashStrategy()`, `setAutoStashWifiOnly()`.
  - `SettingsScreen`: Interactive "⚡ Smart Offline Auto-Stash" dashboard card featuring storage progress bar, budget chips (500MB, 1GB, 2GB, 5GB), strategy selector, and Wi-Fi only toggle.
- **Verification & Gates**:
  - `AutoStashEvaluatorTest` in `:core:domain:offline`.
  - `SettingsViewModelTest` in `:feature:settings`.
  - Quality gates: `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
  - Release tag: `v6.3-1`.

