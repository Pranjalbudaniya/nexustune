# Phase Plan: Improvement Loop 19 (v2.9.0)

## Objective
Implement **Smart Safe Listening & Volume Limiter Protection**:
1. Implement `SafeListeningManager` in `core:player` tracking acoustic sound dose based on WHO / ITU-T H.870 standard with real-time dose percentage and safety warnings.
2. Implement configurable hardware Maximum Volume Limiter (70%, 80%, 90%, 100%) in `PlayerController` to cap output amplitude and prevent accidental auditory fatigue.
3. Integrate Safe Listening controls, real-time exposure indicator, and volume limiter chips in Settings.

## Architecture & Modules
- `core:model`:
  - `volumeLimitPercent: Int = 100` in `ThemeConfig`
  - `safeListeningAlertEnabled: Boolean = true` in `ThemeConfig`
  - `SoundDoseInfo(dosePercent: Float, isOverExposed: Boolean, safeHoursRemaining: Float)`
- `core:player`:
  - `SafeListeningManager`: tracks cumulative playback seconds and volume multiplier, calculating weekly acoustic dose and exposure status.
  - `PlayerController`:
    - `setVolumeLimit(percent: Int)`: Clamps ExoPlayer volume to user-defined limit ceiling.
    - Updated `setVolume` respecting `volumeLimitMultiplier`.
- `app`:
  - Instantiate `safeListeningManager` in `NexusTuneApplication`.
  - Wire listening exposure tracking in `MainActivity` when track is actively playing.
- `feature:settings`:
  - `SettingsViewModel`:
    - `setVolumeLimit(percent: Int)`
    - `toggleSafeListeningAlert(enabled: Boolean)`
    - `resetSoundDose()`
  - `SettingsScreen`:
    - "Safe Listening & Hearing Protection" card with live dose progress indicator and volume limit selector chips (70%, 80%, 90%, 100%).
    - Update About banner to `NexusTune v2.9.0` | `Safe Listening Protection & Volume Limiter`.

## Tasks & Owners
1. **Task 19.1: Core Models & Player Volume Limiting**
   - Update `ThemeConfig` with `volumeLimitPercent` and `safeListeningAlertEnabled`.
   - Update `PlayerController` with volume limit capping.
2. **Task 19.2: SafeListeningManager Engine**
   - Implement `SafeListeningManager` in `core:player:hearing`.
   - Wire into `NexusTuneApplication` and `MainActivity`.
3. **Task 19.3: Settings UI & About Update**
   - Wire `SettingsViewModel` and `SettingsScreen` with Safe Listening controls.
   - Update About banner.
4. **Task 19.4: Unit Tests & Release Qualification**
   - Unit tests for `SafeListeningManager` and `PlayerController.setVolumeLimit`.
   - Unit tests in `SettingsViewModelTest`.
   - Run `./scripts/gate.sh slice`.
   - Run `./scripts/gate.sh final` and tag `v2.9-1`.

