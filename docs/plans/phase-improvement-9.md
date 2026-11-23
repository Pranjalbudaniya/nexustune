# Phase Execution Plan: Improvement Loop 9 (v1.9.0)

## Objective
Implement **Offline Storage & Cache Auto-Pruning Engine** in NexusTune, enabling configurable cache size limits (500MB, 1GB, 2GB, 5GB, Unlimited), real-time disk space auditing, and automatic LRU cache pruning.

## Scope & Components
1. **Model & Preferences**:
   - Add `cacheBudgetMb: Int = 1000` (default 1000MB) to `ThemeConfig` in `core:model`.
2. **Storage Management Engine**:
   - Create `StorageManager` in `core:data`:
     - Inspects internal cache and download directories.
     - Calculates disk usage in bytes/megabytes.
     - Prunes oldest files when total cache usage exceeds `cacheBudgetMb`.
     - Supports manual `clearCache()` for immediate space reclamation.
3. **Settings & UI Integration**:
   - In `SettingsViewModel` (`feature:settings`):
     - Expose `storageUsage: StateFlow<StorageUsage>` (cacheBytes, downloadBytes).
     - Method `setCacheBudget(budgetMb: Int)`.
     - Method `clearCache()`.
   - In `SettingsScreen` (`feature:settings`):
     - Dedicated "Storage & Cache Management" section.
     - Real-time disk usage readout.
     - FilterChip selection for Cache Budget (500MB, 1GB, 2GB, 5GB, Unlimited).
     - "Clear Cache" button with instant cleanup.
4. **Unit Tests & Quality Gates**:
   - Unit tests in `core:data:testDebugUnitTest` (`StorageManagerTest`).
   - Unit tests in `feature:settings:testDebugUnitTest` (`SettingsViewModelTest`).
   - `./scripts/gate.sh slice`
   - `./scripts/gate.sh perf` on device
   - `./scripts/gate.sh final` release verification
   - Tag `v1.9-1`

## Acceptance Criteria
- [ ] `ThemeConfig` holds `cacheBudgetMb`.
- [ ] `StorageManager` accurately calculates directory sizes and prunes LRU files.
- [ ] `SettingsViewModel` and `SettingsScreen` show real-time storage info and permit cache clearing.
- [ ] 100% test pass rate across all modules.
- [ ] `./scripts/gate.sh slice` passes.
- [ ] `./scripts/gate.sh perf` passes on device.
- [ ] `./scripts/gate.sh final` passes.
