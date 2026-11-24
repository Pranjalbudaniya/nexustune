# Phase Improvement Loop 32: Bedtime Sound Handoff Engine (v4.2.0 Milestone)

## 1. Goal
Implement a seamless **Bedtime Sound Handoff** engine bridging music playback, sleep timer countdown, and procedural ambient sleep soundscapes:
1. Bedtime Music-to-Ambient Handoff (`:core:player`):
   - When the user falls asleep to music and the sleep timer expires:
     - Instead of abrupt silence, automatically transition and fade in procedural ambient soundscapes (`AmbientSoundSynthesizer`, e.g. Brown Noise or 2.5Hz Delta Sleep Waves).
     - Support configurable handoff sound profile (`AmbientSoundType`), bedtime volume (10% - 60%), and ambient duration (30m, 60m, 120m, or All Night).
   - In `SleepTimerManager`:
     - Update `SleepTimerState` with `bedtimeHandoffEnabled`, `handoffSoundType`, `handoffVolume`, and `handoffDurationMinutes`.
     - In `onTimerFinished()`, trigger `AmbientSoundSynthesizer.instance.start()` with the configured sound profile and schedule ambient auto-off.
2. DataStore Persistence & Configuration (`:core:model`, `:core:datastore`):
   - Add fields to `ThemeConfig`:
     - `bedtimeHandoffEnabled: Boolean = false`
     - `bedtimeHandoffSoundType: AmbientSoundType = AmbientSoundType.BROWN_NOISE`
     - `bedtimeHandoffVolume: Float = 0.3f`
     - `bedtimeHandoffDurationMinutes: Int = 60`
   - Persist in `SettingsDataStore`.
3. Material 3 Expressive UI in Settings (`:feature:settings`):
   - In `SettingsScreen` under "Sleep Timer":
     - Add "Bedtime Sound Handoff" switch item.
     - When enabled, display sound profile chips (Brown Noise, Pink Noise, Delta Sleep, etc.), volume fader, and duration chips.
4. Testing & Verification:
   - Unit tests in `SleepTimerManagerTest` verifying handoff trigger upon timer completion.
   - Unit tests in `SettingsViewModelTest` verifying handoff configuration.
   - Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.
   - Tag release: `v4.2-1`.

## 2. Tasks & Owners
- **Core Model**: Add handoff fields to `ThemeConfig`.
- **Core DataStore**: Persist handoff preferences in `SettingsDataStore`.
- **Core Player**: Add handoff coordination to `SleepTimerManager`.
- **Feature Settings**: Add handoff controls to `SettingsViewModel` and `SettingsScreen`.
- **Testing**: Add tests in `SleepTimerManagerTest` and `SettingsViewModelTest`.
- **Verification**: Run `./scripts/gate.sh slice` and `./scripts/gate.sh final`.

## 3. Acceptance Criteria
- [ ] Sleep timer expiration seamlessly transitions to the configured ambient noise generator when handoff is enabled.
- [ ] Canceling the timer or manual pausing does not trigger handoff.
- [ ] Handoff settings are saved and restored in DataStore.
- [ ] UI provides controls for toggle, sound type, volume, and duration.
- [ ] All unit and Robolectric tests pass (`./scripts/gate.sh slice`).
- [ ] Production APK builds cleanly via `./scripts/gate.sh final`.
