# Phase Improvement Loop 28: Sleep Timer Shake-to-Extend & Motion Watcher

## 1. Goal
Implement intelligent accelerometer-based Shake-to-Extend functionality for NexusTune's Sleep Timer:
1. Dynamic Timer Extension:
   - Provide `extendTimer(extraMinutes: Int)` in `SleepTimerManager`.
   - When extended, dynamically add extra seconds to remaining time, cancel volume fade attenuation, and restore player volume to 100%.
2. Hardware Accelerometer Sensor Watcher:
   - Integrate with Android `SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)`.
   - Calculate g-force magnitude vector $\sqrt{g_x^2 + g_y^2 + g_z^2}$.
   - Detect gentle shake gesture ($g > 2.2$) with 3-second debouncing to prevent multi-triggering.
   - Register listener only while sleep timer is active; immediately unregister on timer completion or cancellation to ensure zero background battery drain.
3. Settings UI & Persistence:
   - Add `shakeToExtendSleepTimerEnabled` and `shakeExtendMinutes` in `ThemeConfig` and `SettingsDataStore`.
   - Render "Shake to Extend" switch and extension duration chips (+5m, +10m, +15m) in `SettingsScreen`.
   - Show live prompt when sleep timer is active.
4. Testing & Qualification:
   - Add unit tests in `SleepTimerManagerTest` for `extendTimer` and state transitions.
   - Verify with `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Release tagging: `v3.8-1`.

## 2. Tasks & Owners
- **Core Player**: Add `extendTimer` and `SensorEventListener` shake detection in `SleepTimerManager`.
- **Core Model & DataStore**: Add shake settings to `ThemeConfig` and `SettingsDataStore`.
- **Feature Settings**: Expose shake toggle and duration chips in `SettingsViewModel` and `SettingsScreen`.
- **Application**: Wire `SensorManager` to `SleepTimerManager` in `NexusTuneApplication`.
- **Testing & Release**: Unit tests and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] `extendTimer` successfully adds time and restores full volume during fade-out.
- [ ] Accelerometer listener registers on active timer and unregisters on cancel/expire.
- [ ] Settings screen allows configuring shake-to-extend toggle and duration.
- [ ] All unit and Robolectric tests pass (`./scripts/gate.sh slice`).
- [ ] `./scripts/gate.sh final` passes with release APK build.
