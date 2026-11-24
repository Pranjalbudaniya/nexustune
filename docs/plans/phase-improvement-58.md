# Post-v1.0 Improvement Loop 58: Dynamic Sleep Drift & Psychoacoustic Bedtime Induction Engine

## 1. Objectives & Overview
Implement an acoustic sleep induction and progressive frequency de-escalation engine in NexusTune (`SleepDriftEngine`):
1. **Psychoacoustic High-Frequency Roll-Off**:
   - High-frequency sounds (>3kHz) stimulate vigilance and cerebral cortical alertness. As the sleep timer progresses, `SleepDriftEngine` progressively shelves down high frequencies (3.6kHz and 14kHz by up to -12dB) without affecting sub-bass, mimicking the natural acoustic attenuation of falling asleep under a warm blanket.
2. **Harmonic Tempo Deceleration**:
   - During extended sleep sessions, decelerate playback speed gradually from 1.0x to 0.90x across the final window to slow heart rate and respiration into circadian synchronization.
3. **Binaural Undertone Tuning**:
   - Calculate Theta (6Hz) and Delta (3Hz) hemispheric phase differentials for hypnagogic sleep induction.
4. **Sleep Timer Integration**:
   - Enhance `SleepTimerManager` and `NexusAudioEffects` to dynamically calculate and modulate drift progress ($0.0 \dots 1.0$), frequency offsets, and tempo without corrupting standard playback equalizer settings.
5. **Interactive Controls**:
   - Selector chips in `SettingsScreen` and sleep timer dialog for `SleepDriftProfile` (`OFF`, `WARM_MELLOW`, `DEEP_DRIFT`, `BINAURAL_THETA`, `BINAURAL_DELTA`).

## 2. Architecture & Components
- `:core:model`:
  - `SleepDriftProfile` enum (`OFF`, `WARM_MELLOW`, `DEEP_DRIFT`, `BINAURAL_THETA`, `BINAURAL_DELTA`)
  - `SleepDriftConfig` data class
- `:core:domain`:
  - `SleepDriftEngine` in `com.nexus.tune.core.domain.audio`:
    - `calculateDriftProgress(elapsedSeconds: Long, totalSeconds: Long): Float`
    - `calculateEqRollOffBands(profile: SleepDriftProfile, progress: Float): List<Int>`
    - `calculateDriftSpeed(profile: SleepDriftProfile, progress: Float): Float`
    - `calculateBinauralFrequencies(profile: SleepDriftProfile): Pair<Float, Float>?`
- `:core:player`:
  - `SleepTimerState`: add `driftProfile: SleepDriftProfile` and `driftProgress: Float`.
  - `SleepTimerManager`: `setSleepDriftProfile(profile: SleepDriftProfile)`, dynamic loop roll-off updates.
  - `NexusAudioEffects`: `applySleepDriftOffsets(offsets: List<Int>)` and `clearSleepDriftOffsets()`.
- `:core:datastore`:
  - Preference key: `SLEEP_DRIFT_PROFILE`.
  - Mapped to `ThemeConfig`.
- `:feature:settings`:
  - `SettingsViewModel`: `setSleepDriftProfile(profile: SleepDriftProfile)`.
  - Sleep Drift Studio card in `SettingsScreen`.
- Unit Tests:
  - `SleepDriftEngineTest` in `:core:domain`
  - `SleepTimerManagerTest` in `:core:player`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.8-1`.

