# Post-v1.0 Improvement Loop 62: Acoustic Silence & Outro Dead-Air Trimmer Studio

## 1. Objectives & Overview
Implement an intelligent acoustic silence and dead-air trimmer engine (`SilenceTrimmerEngine`):
1. **Audio Frame RMS Energy & Silence Classification**:
   - Evaluate real-time root-mean-square (RMS) signal energy in dBFS across audio buffers.
   - Accurately distinguish true dead air / silent pauses from quiet musical passages (decaying reverb tails, delicate acoustic intros).
2. **Anti-Click Cosine-Squared Transient Smoothing**:
   - Apply micro-fade (10ms - 20ms) cosine-squared ramps before and after silence excision to guarantee click-free, pop-free transitions.
3. **Silence Trim Modes**:
   - `OFF`: Unmodified playback.
   - `NATURAL_MUSIC`: Preserves musical pauses up to 1.5s; trims only dead air at track boundaries and extended silence.
   - `FAST_TALK`: Aggressive trimming (skips silences >350ms, threshold -36 dBFS) ideal for podcasts, audiobooks, and talk recordings.
   - `SMART_DYNAMIC`: Adaptive RMS sensitivity tracking ambient floor noise.
   - `EXTENDED_OUTRO`: Specifically strips long hidden-track pauses and excessive track end silence.
4. **DataStore Persistence & UI**:
   - Key preferences in `SettingsDataStore` mapped to `ThemeConfig`.
   - Interactive studio card in `SettingsScreen` with mode filter chips, threshold slider, and live time-saved metric.

## 2. Architecture & Components
- `:core:model`:
  - `SilenceTrimMode` enum
  - `SilenceTrimConfig` data class
  - `SilenceDetectionResult` data class
- `:core:domain`:
  - `SilenceTrimmerEngine` in `com.nexus.tune.core.domain.audio`:
    - `calculateRmsDb(samples: FloatArray): Float`
    - `detectSilence(rmsDb: Float, currentSilenceMs: Long, config: SilenceTrimConfig): SilenceDetectionResult`
    - `applyEnvelopeSmoothing(samples: FloatArray, isFadeIn: Boolean, rampLength: Int): FloatArray`
    - `estimateTimeSavingsPercent(silentFramesCount: Long, totalFramesCount: Long): Float`
- `:core:datastore`:
  - `SettingsDataStore`: keys `SILENCE_TRIM_ENABLED`, `SILENCE_TRIM_MODE`, `SILENCE_TRIM_THRESHOLD_DB`, `SILENCE_TRIM_MIN_MS`.
- `:feature:settings`:
  - `SettingsViewModel` control methods.
  - Interactive "Acoustic Silence & Dead-Air Trimmer" studio card in `SettingsScreen`.
- Unit tests:
  - `SilenceTrimmerEngineTest` in `:core:domain`
  - `SettingsViewModelTest` in `:feature:settings`

## 3. Verification Criteria
- `./scripts/gate.sh slice` passes with 100% test pass rate.
- `./scripts/gate.sh final` passes with release APK build.
- Tagged `v6.12-1`.

