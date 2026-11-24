# Phase Improvement Loop 70: Audiophile High-Frequency "Air Band" & Silk Sheen Exciter Studio

## 1. Goal
Implement the **Audiophile High-Frequency "Air Band" & Silk Sheen Exciter Studio** (`AirBandExciterEngine`) for NexusTune.
Lossy streaming compression (such as YouTube Music, AAC, Opus, MP3) routinely rolls off or abruptly truncates high-frequency spectral content above 16 kHz. Furthermore, standard playback often lacks the delicate spatial bloom, acoustic instrument transient sparkle, and vocal breath present in high-end analog tape and studio mastering consoles (e.g. Pultec EQ and Maag Audio AIR BAND®).
This studio provides:
1. High-shelf biquad filtering with selectable center frequencies (12 kHz, 16 kHz, 20 kHz, 24 kHz) and smooth, continuous boost (0.0 dB to +6.0 dB).
2. Psychoacoustic even-order harmonic saturation ($x^2$) over treble bands to reconstruct open, silky air lost to lossy compression.
3. Hardware equalizer pipeline integration on Band 4 (high treble) in `NexusAudioEffects`.
4. Reactive DataStore persistence, ViewModel controls, and interactive Compose Studio Card in `SettingsScreen`.

## 2. Architecture & Components
- **`:core:model`**:
  - `AirBandFrequency` (`HZ_12000`, `HZ_16000`, `HZ_20000`, `HZ_24000`)
  - `AirBandMode` (`OFF`, `SUBTLE_SILK`, `VOCAL_BREATH`, `MASTERING_AIR`, `LOSSLESS_SPARKLE`)
  - `AirBandExciterConfig`
  - `AirBandBiquadCoefficients`
  - `AirBandMetrics`
  - `ThemeConfig.airBandExciterConfig`
- **`:core:domain`**:
  - `AirBandExciterEngine` in `com.nexus.tune.core.domain.audio` with high-shelf biquad coefficient generation, harmonic exciter calculation, hardware offset derivation, and live sheen metrics evaluation.
  - `AirBandExciterEngineTest` unit tests.
- **`:core:player`**:
  - `NexusAudioEffects` integration on Band 4 with `applyAirBandOffset`, `clearAirBandOffset`, and `getAirBandOffset`.
  - `NexusAudioEffectsTest` unit tests.
- **`:core:datastore`**:
  - `SettingsDataStore` keys: `AIR_BAND_ENABLED`, `AIR_BAND_MODE`, `AIR_BAND_FREQ_HZ`, `AIR_BAND_BOOST_DB`, `AIR_BAND_HARMONICS`.
- **`:feature:settings`**:
  - `SettingsViewModel`: State methods for Air Band excitation.
  - `SettingsViewModelTest`: Unit test coverage.
  - `SettingsScreen`: Studio card with frequency selectors, mode chips, boost slider, harmonic saturation slider, and live sheen descriptor badge.

## 3. Verification & Acceptance Criteria
- Full mathematical unit tests in `:core:domain` for high-shelf biquad stability, hardware millibel offsets, and harmonic saturation.
- Full unit tests in `:core:player` for audio effects Band 4 routing.
- Full unit tests in `:feature:settings` for ViewModel reactivity.
- `./scripts/gate.sh slice` passes without error.
- `./scripts/gate.sh final` builds release APK and passes all quality gates.
- Tag and commit `v6.20-1`.

