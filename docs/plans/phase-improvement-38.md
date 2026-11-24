# Phase Improvement Loop 38: Gentle Morning Wake-Up Alarm & Audio Fade-In Engine

## 1. Overview & Objectives
Complementing the Bedtime Sleep Timer and Ambient Soundscape Synthesizer, Loop 38 introduces a complete morning routine wake-up solution:
1. `WakeAlarmConfig`:
   - Configurable alarm schedule (`hour`, `minute`), audio source (`LIKED_SONGS`, `CURRENT_QUEUE`, `AMBIENT_SOUND`), fade-in duration (30s to 10m), and target volume.
2. `WakeAlarmManager` in `:core:player:alarm`:
   - Schedules exact alarms via Android `AlarmManager` (with fallback to `setAndAllowWhileIdle`).
   - Implements smooth gradual volume ramping (starting at 0.0f and gently ramping to target volume over configured fade duration).
   - Dismiss, Stop, and Snooze (+10m) functionality.
   - Test/Preview mode for immediate verification of gentle fade-in audio dynamics.
3. `SettingsDataStore` & `ThemeConfig` Integration:
   - Persistent alarm configuration across launches.
4. Material 3 Expressive UI Console in `SettingsScreen`:
   - Wake-Up Alarm card with toggle, time chips, fade duration selector (30s, 1m, 2m, 5m), source chips, target volume slider, and instant preview button.
5. Comprehensive unit test suites across `:core:player` and `:feature:settings`.

## 2. Technical Scope & Architecture Boundaries
- **`:core:model`**:
  - `WakeAlarmSource`: `LIKED_SONGS`, `CURRENT_QUEUE`, `AMBIENT_SOUND`.
  - `WakeAlarmConfig`: Data class for alarm configuration.
  - `ThemeConfig`: Add `val wakeAlarmConfig: WakeAlarmConfig = WakeAlarmConfig()`.
- **`:core:datastore`**:
  - `SettingsDataStore`: Preference keys and serialization for `WakeAlarmConfig`.
- **`:core:player`**:
  - `WakeAlarmManager.kt`: Timing, calculation, scheduling, and smooth volume ramping.
  - `WakeAlarmReceiver.kt`: Broadcast receiver for system alarm intents.
- **`:feature:settings`**:
  - `SettingsViewModel`: State flow and action methods for `WakeAlarmConfig`.
  - `SettingsScreen`: Dedicated "Gentle Wake-Up Alarm" card.
- **`app`**:
  - `AndroidManifest.xml`: Add `SCHEDULE_EXACT_ALARM` and register `WakeAlarmReceiver`.
  - `NexusTuneApplication.kt`: Instantiate `WakeAlarmManager`.

## 3. Verification Criteria
- [ ] `./scripts/gate.sh slice` passes 100% of unit tests.
- [ ] `./scripts/gate.sh final` passes with release APK build.
- [ ] Tag build `v4.8-1`.

